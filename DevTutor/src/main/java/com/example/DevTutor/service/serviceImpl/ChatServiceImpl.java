package com.example.DevTutor.service.serviceImpl;

import com.example.DevTutor.dto.*;
import com.example.DevTutor.entity.Conversation;
import com.example.DevTutor.entity.Message;
import com.example.DevTutor.entity.LearnerNote;
import com.example.DevTutor.entity.QuizResult;
import com.example.DevTutor.enums.MessageRole;
import com.example.DevTutor.mapper.MessageMapper;
import com.example.DevTutor.repository.ConversationRepository;
import com.example.DevTutor.repository.GeneratedDocumentRepository;
import com.example.DevTutor.repository.MessageRepository;
import com.example.DevTutor.repository.LearnerNoteRepository;
import com.example.DevTutor.dto.ReviewResponse;
import com.example.DevTutor.service.ReviewScheduler;
import com.example.DevTutor.service.TrelloMcpService;
import java.util.stream.Collectors;
import com.example.DevTutor.repository.QuizResultRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.DevTutor.service.serviceInterface.ChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.web.multipart.MultipartFile;
import com.example.DevTutor.util.MimeTypes;
import org.springframework.ai.content.Media;
import org.springframework.ai.chat.prompt.PromptTemplate;
import com.cloudinary.Cloudinary;
import java.util.Map;
import com.example.DevTutor.tools.CodeRunnerTools;
import com.example.DevTutor.tools.DocumentTools;
import com.example.DevTutor.tools.LearnerNoteTools;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.tool.ToolCallbackProvider;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final Cloudinary cloudinary;
    private final GeneratedDocumentRepository generatedDocumentRepository;
    private final QuizResultRepository quizResultRepository;
    private final LearnerNoteRepository learnerNoteRepository;
    private final ReviewScheduler reviewScheduler;
    private final TrelloMcpService trelloMcpService;
    private final String guide;

    private static final String HINT_MODE = """


            Hint mode is on: do not give the full answer or the full code.
            Give one small hint or ask a guiding question so the learner finds the answer.
            """;

    private static final String DEV_MODE = """


            Dev mode is on: the user is an experienced developer.
            Answer directly and show the code first. Skip beginner explanations and step-by-step teaching.
            Any language or framework is fine, not only Java.
            """;

    public ChatServiceImpl(
            ToolCallbackProvider mcpTools,
            DocumentTools documentTools,
            CodeRunnerTools codeRunnerTools,
            LearnerNoteTools learnerNoteTools,
            ChatClient.Builder builder,
            ChatMemory chatMemory,
            VectorStore vectorStore,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            MessageMapper messageMapper, Cloudinary cloudinary,
            GeneratedDocumentRepository generatedDocumentRepository,
            QuizResultRepository quizResultRepository,
            LearnerNoteRepository learnerNoteRepository,
            ReviewScheduler reviewScheduler,
            TrelloMcpService trelloMcpService
    ) throws IOException {

        this.generatedDocumentRepository = generatedDocumentRepository;
        this.quizResultRepository = quizResultRepository;
        this.learnerNoteRepository = learnerNoteRepository;
        this.reviewScheduler = reviewScheduler;
        this.trelloMcpService = trelloMcpService;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.messageMapper = messageMapper;
        this.cloudinary = cloudinary;

        this.guide = new ClassPathResource("devtutor-guide.txt")
                .getContentAsString(StandardCharsets.UTF_8);

        this.chatClient = builder
                .defaultSystem(guide)
                .defaultTools(documentTools, codeRunnerTools, learnerNoteTools).defaultTools(mcpTools)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),

                        QuestionAnswerAdvisor.builder(vectorStore)
                                .promptTemplate(new PromptTemplate("""
                                Answer the user's question using the conversation
                                and any attached image.

                                Retrieved documents:
                                ---------------------
                                {question_answer_context}
                                ---------------------

                                Use the documents when relevant.
                                If they are empty or irrelevant, answer using
                                the conversation, attached image and your knowledge.
                                Do not invent details about documents or images.
                                If something is unclear, ask for clarification.

                                User question:
                                {query}
                                """))
                                .build()
                )
                .build();
    }

    @Override
    public MessageResponse reply(MessageRequest request, MultipartFile file) {
        if (request.getConversationId() == null
                || request.getContent() == null
                || request.getContent().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Conversation ID and content are required");
        }
        // Only the creator can use a conversation (admins included).
        Conversation conversation = conversationRepository
                .findByIdAndCreatedByEmail(request.getConversationId(), currentUserEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Conversation not found"));

        Media media = toMedia(file);

        Message userMessage = messageMapper.toEntity(request);
        userMessage.setRole(MessageRole.USER);
        userMessage.setConversation(conversation);
// Upload the image only if one was attached.
        if (media != null) {
            Map<?, ?> uploaded = uploadImage(file);

            userMessage.setImageUrl(
                    uploaded.get("secure_url").toString()
            );

            userMessage.setImagePublicId(
                    uploaded.get("public_id").toString()
            );
        }

        messageRepository.save(userMessage);
        boolean firstMessage = messageRepository.countByConversationId(conversation.getId()) == 1;

        ChatClientResponse aiResponse = chatClient.prompt()
                .system(guide + quizScores() + learnerNotes() + (firstMessage ? dueReviews() : "")
                        + (request.isHintMode() ? HINT_MODE : "")
                        + (request.isDevMode() ? DEV_MODE : ""))
                .user(u -> {
                    u.text(request.getContent());
                    if (media != null) {
                        u.media(media);
                    }
                })
                .advisors(a -> a
                        .param(
                                ChatMemory.CONVERSATION_ID,
                                conversation.getId().toString()
                        )
                        .param(
                                QuestionAnswerAdvisor.FILTER_EXPRESSION,
                                "conversationId == '" + conversation.getId() + "'"
                        )
                )
                .toolCallbacks(trelloMcpService.toolsFor(currentUserEmail()))
                // Lets DocumentTools save documents to this conversation.
                .toolContext(Map.of(
                        "conversationId", conversation.getId(),
                        "userEmail", currentUserEmail()))
                .call()
                .chatClientResponse();

        String answer = aiResponse.chatResponse() == null
                ? null
                : aiResponse.chatResponse().getResult().getOutput().getText();

        List<SourceResponse> sources = toSources(aiResponse.context()
                .get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS));

        if (answer == null || answer.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "The AI returned an empty answer. Please try again.");
        }

        Message assistantMessage = new Message();
        assistantMessage.setContent(answer);
        assistantMessage.setRole(MessageRole.ASSISTANT);
        assistantMessage.setConversation(conversation);

        Message savedAnswer = messageRepository.save(assistantMessage);

        MessageResponse response = messageMapper.toResponse(savedAnswer);
        response.setSources(sources);
        response.setImageUrl(userMessage.getImageUrl());
        response.setDocuments(generatedDocumentRepository
                .findByConversationIdAndCreatedAtGreaterThanEqual(
                        conversation.getId(), userMessage.getCreatedAt())
                .stream()
                .map(GeneratedDocumentResponse::from)
                .toList());
        return response;
    }

    private String dueReviews() {
        String due = reviewScheduler.schedule(
                        quizResultRepository.findByUserEmailOrderByCreatedAtDesc(currentUserEmail()))
                .stream()
                .filter(ReviewResponse::due)
                .map(ReviewResponse::concept)
                .collect(Collectors.joining(", "));
        if (due.isEmpty()) {
            return "";
        }

        return "\n\nConcepts due for review: " + due + ".\n"
                + "At the end of your answer, offer a short review quiz on them in one sentence.\n"
                + "Do not ask the quiz questions yourself.";
    }

    private String learnerNotes() {
        List<LearnerNote> notes = learnerNoteRepository
                .findTop10ByUserEmailOrderByCreatedAtDesc(currentUserEmail());
        if (notes.isEmpty()) {
            return "";
        }

        StringBuilder text = new StringBuilder("\n\nWhat you know about this learner:\n");
        for (LearnerNote note : notes) {
            text.append("- ").append(note.getNote()).append("\n");
        }
        text.append("Use these to adapt your explanations. Only mention them when they relate to the user's question.");
        return text.toString();
    }

    private String quizScores() {
        List<QuizResult> results = quizResultRepository
                .findTop5ByUserEmailOrderByCreatedAtDesc(currentUserEmail());
        if (results.isEmpty()) {
            return "";
        }

        StringBuilder text = new StringBuilder("\n\nThe learner's recent quiz scores:\n");
        for (QuizResult result : results) {
            text.append("- ").append(result.getTopic()).append(": ")
                    .append(result.getScore()).append("/").append(result.getTotal())
                    .append("\n");
        }
        text.append("Use these to adapt your explanations. Only mention them when they relate to the user's question.");
        return text.toString();
    }

    private static List<SourceResponse> toSources(Object retrieved) {
        if (!(retrieved instanceof List<?> documents)) {
            return List.of();
        }

        Map<String, SourceResponse> unique = new LinkedHashMap<>();

        for (Object item : documents) {
            if (item instanceof Document document) {
                Map<String, Object> metadata = document.getMetadata();

                Object filename = metadata.get("filename");
                String name = filename == null ? "Unknown file" : filename.toString();

                Integer startPage = toPage(metadata.get(
                        PagePdfDocumentReader.METADATA_START_PAGE_NUMBER));
                Integer endPage = toPage(metadata.get(
                        PagePdfDocumentReader.METADATA_END_PAGE_NUMBER));
                if (endPage == null) {
                    endPage = startPage;
                }

                unique.putIfAbsent(name + "|" + startPage + "|" + endPage,
                        new SourceResponse(name, startPage, endPage));
            }
        }

        return unique.values().stream()
                .sorted(Comparator.comparing(SourceResponse::getFilename)
                        .thenComparing(SourceResponse::getStartPage,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private static Integer toPage(Object value) {
        return value instanceof Number number ? number.intValue() : null;
    }

    private Map<?, ?> uploadImage(MultipartFile file) {
        try {
            return cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of("resource_type", "image")
            );
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Could not upload the image",
                    e
            );
        }
    }

    @Override
    public List<MessageResponse> getMessages(Long conversationId) {
        Conversation conversation = conversationRepository
                .findByIdAndCreatedByEmail(conversationId, currentUserEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Conversation not found"));

        return messageMapper.toResponses(
                messageRepository.findByConversationIdOrderByCreatedAtAscIdAsc(conversation.getId()));
    }

    private static String currentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private static Media toMedia(MultipartFile file) {
        if (file == null) {
            return null;
        }
        if (file.isEmpty()) {
            throw new IllegalArgumentException(
                    "The attached image is empty.");
        }
        String filename = file.getOriginalFilename();

        if (!MimeTypes.isImage(filename)) {
            throw new IllegalArgumentException(
                    "Only PNG, JPEG and WebP images are allowed.");
        }
        try {
            return Media.builder()
                    .mimeType(MimeTypes.ofFilename(filename))
                    .data(file.getBytes())
                    .build();
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Could not read the attached image.");
        }
    }
}