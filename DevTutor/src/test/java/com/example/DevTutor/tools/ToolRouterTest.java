package com.example.DevTutor.tools;

import com.example.DevTutor.tools.ToolRouter.Group;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolRouterTest {

    private static final Set<Group> ALWAYS = EnumSet.of(Group.LOCAL, Group.CONTEXT7);

    private static Set<Group> select(String... userMessages) {
        return ToolRouter.select(Arrays.asList(userMessages));
    }

    private static Set<Group> always(Group... extra) {
        Set<Group> groups = EnumSet.copyOf(ALWAYS);
        groups.addAll(Arrays.asList(extra));
        return groups;
    }

    @Test
    void plainTutoringGetsOnlyLocalAndContext7() {
        assertEquals(ALWAYS, select("Hi"));
        assertEquals(ALWAYS, select("What is the difference between an interface and an abstract class in Java?"));
        assertEquals(ALWAYS, select("How do I create a ChatClient in Spring AI? Use the documentation."));
    }

    @Test
    void javaListIsNotTrello() {
        assertEquals(ALWAYS, select("What is a Java List?"));
        assertEquals(ALWAYS, select("Explain ArrayList vs LinkedList and how to list files"));
    }

    @Test
    void pathsAndFractionsAreNotGitHub() {
        assertEquals(ALWAYS, select("Where do I put src/main/java/App.java?"));
        assertEquals(ALWAYS, select("Why is 1/2 equal to 0 in Java?"));
    }

    @Test
    void ordinaryProgrammingWordsAreNotGitHub() {
        assertEquals(ALWAYS, select("I have an issue with my loop"));
        assertEquals(ALWAYS, select("There are issues when my code runs"));
        assertEquals(ALWAYS, select("How does try/catch work?"));
        assertEquals(ALWAYS, select("Explain input/output streams and read/write files"));
        assertEquals(ALWAYS, select("Is Java/Kotlin better for a client/server app? and/or both?"));
        assertEquals(ALWAYS, select("How do I commit a transaction in JDBC?"));
        assertEquals(ALWAYS, select("What is a branch in an if statement?"));
        assertEquals(ALWAYS, select("Does close() release the lock?"));
        assertEquals(ALWAYS, select("Explain A/B testing and I/O"));
    }

    @Test
    void ordinaryProgrammingWordsAreNotTrello() {
        assertEquals(ALWAYS, select("Write a card game with a deck of cards"));
        assertEquals(ALWAYS, select("How do I validate a credit card number?"));
        assertEquals(ALWAYS, select("Print the tic-tac-toe board"));
        assertFalse(select("Draw a chess board with a 2D array").contains(Group.TRELLO));
        assertEquals(ALWAYS, select("My list contains duplicates"));
    }

    @Test
    void naturalGitHubQuestionsWithoutTheWordGitHub() {
        assertEquals(always(Group.GITHUB), select("List the open issues"));
        assertEquals(always(Group.GITHUB), select("Show me issue #12"));
        assertEquals(always(Group.GITHUB), select("What was the last commit?"));
        assertEquals(always(Group.GITHUB), select("Show me the commits"));
        assertEquals(always(Group.GITHUB), select("Is there a newer pull-request?"));
        assertEquals(always(Group.GITHUB), select("What is in the README of facebook/react?"));
        assertEquals(always(Group.GITHUB), select("Explain jetbrains/kotlin"));
        assertEquals(always(Group.GITHUB), select("What is the latest release?"));
        assertEquals(always(Group.GITHUB), select("Who are the contributors?"));
        assertEquals(always(Group.GITHUB), select("Read the main branch"));
    }

    @Test
    void naturalTrelloQuestionsWithoutTheWordTrello() {
        assertEquals(always(Group.TRELLO), select("Show my kanban"));
        assertEquals(always(Group.TRELLO), select("What is on my to-do list?"));
        assertEquals(always(Group.TRELLO), select("Show all my lists"));
        assertEquals(always(Group.TRELLO), select("Add a card for tomorrow"));
    }

    @Test
    void githubWordsSelectGitHub() {
        assertEquals(always(Group.GITHUB), select("What is the spring-projects/spring-ai repository about? Check GitHub."));
        assertEquals(always(Group.GITHUB), select("Explain torvalds/linux"));
        assertEquals(always(Group.GITHUB), select("Read https://github.com/spring-projects/spring-ai/blob/main/README.md"));
        assertEquals(always(Group.GITHUB), select("Show the open pull requests"));
        assertEquals(always(Group.GITHUB), select("Is there an issue about this in my repo?"));
        assertEquals(always(Group.GITHUB), select("What changed in the last commit on that branch?"));
    }

    @Test
    void trelloWordsSelectTrello() {
        assertEquals(always(Group.TRELLO), select("Use the Trello tools to list my boards."));
        assertEquals(always(Group.TRELLO), select("What is on my board?"));
        assertEquals(always(Group.TRELLO), select("Add an item to my checklist"));
        assertEquals(always(Group.TRELLO), select("Show my INBOX"));
        assertTrue(select("Create a Trello card for the main topic of my PDF in the first list of my first Trello board.")
                .contains(Group.TRELLO));
    }

    @Test
    void oneMessageCanSelectBothGroups() {
        assertEquals(always(Group.GITHUB, Group.TRELLO),
                select("Create a Trello card for each open issue in spring-projects/spring-ai"));
    }

    @Test
    void followUpKeepsTheGroupFromRecentMessages() {
        assertEquals(always(Group.TRELLO),
                select("Use the Trello tools to list my boards.", "Which one did I change most recently?"));
        assertEquals(always(Group.GITHUB),
                select("What is the spring-projects/spring-ai repository about? Check GitHub.", "What license does it use?"));
        assertEquals(always(Group.TRELLO),
                select("Create a card in my Trello board", "now rename it", "and move it to Done"));
    }

    @Test
    void importantFollowUpsKeepTheirTools() {
        assertEquals(always(Group.TRELLO),
                select("Create a card for Java loops in my Trello board", "Yes, go ahead"));
        assertEquals(always(Group.TRELLO),
                select("Show my Trello boards", "the second one", "what lists does it have?"));
        assertEquals(always(Group.GITHUB),
                select("Check this GitHub project for me", "spring-projects/spring-ai"));
        assertEquals(always(Group.GITHUB),
                select("Read the README of spring-projects/spring-ai", "Explain it simply", "Show me the code example"));
        assertEquals(always(Group.GITHUB, Group.TRELLO),
                select("Read spring-projects/spring-ai", "Now make a Trello card about it", "rename it"));
    }

    @Test
    void groupIsDroppedAfterThreeMessagesWithoutIt() {
        assertEquals(ALWAYS,
                select("Create a card in my Trello board", "Thanks", "What is a loop?", "Show me an example"));
    }

    @Test
    void nullMessagesAndEmptyHistoryAreSafe() {
        assertEquals(ALWAYS, ToolRouter.select(List.of()));
        List<String> messages = new ArrayList<>();
        messages.add(null);
        messages.add("Show my Trello boards");
        assertEquals(always(Group.TRELLO), ToolRouter.select(messages));
    }

    @Test
    void recognizesContext7ToolNames() {
        assertTrue(ToolRouter.isContext7Tool("resolve_library_id"));
        assertTrue(ToolRouter.isContext7Tool("query_docs"));
        assertFalse(ToolRouter.isContext7Tool("resolve-library-id"));
        assertFalse(ToolRouter.isContext7Tool("get_file_contents"));
    }

    @Test
    void documentRequestsSelectDocuments() {
        assertEquals(always(Group.DOCUMENTS), select("Create a PDF about Java loops"));
        assertEquals(always(Group.DOCUMENTS), select("Make me a Word document about OOP"));
        assertEquals(always(Group.DOCUMENTS), select("Can you give me a docx version?"));
        assertEquals(always(Group.DOCUMENTS), select("I want a cheat sheet for Git commands"));
        assertEquals(always(Group.DOCUMENTS), select("Make a printable worksheet on arrays"));
        assertEquals(always(Group.DOCUMENTS), select("Save it as a file please"));
        assertEquals(always(Group.DOCUMENTS), select("Export this lesson so I can download it"));
        assertEquals(always(Group.DOCUMENTS), select("Write study notes on recursion that I can keep"));
        assertEquals(always(Group.DOCUMENTS), select("Turn this into a document"));
        assertEquals(always(Group.DOCUMENTS), select("Make a handout for my class"));
    }

    @Test
    void imageRequestsSelectImage() {
        assertEquals(always(Group.IMAGE), select("Draw a cat programming in Java"));
        assertEquals(always(Group.IMAGE), select("Generate a picture of a binary tree"));
        assertEquals(always(Group.IMAGE), select("Create an illustration of the OSI model"));
        assertEquals(always(Group.IMAGE), select("Make a logo for my app"));
        assertEquals(always(Group.IMAGE), select("Sketch a robot"));
        assertEquals(always(Group.IMAGE), select("Can you make an image for my blog banner?"));
    }

    @Test
    void plainTutoringGetsNoDocumentOrImageTools() {
        assertEquals(ALWAYS, select("What is an interface in Java?"));
        assertEquals(ALWAYS, select("Explain recursion with an example"));
        assertEquals(ALWAYS, select("How do I read files line by line in Python?"));
        assertEquals(ALWAYS, select("Why does my for loop never stop?"));
    }

    @Test
    void documentAndImageFollowUpsKeepTheirTools() {
        assertEquals(always(Group.DOCUMENTS),
                select("Create a PDF about Java loops", "make it shorter", "add more examples"));
        assertEquals(always(Group.IMAGE),
                select("Draw a cat programming in Java", "make it blue"));
        assertEquals(ALWAYS,
                select("Create a PDF about Java loops", "thanks", "what is a loop?", "show me an example"));
    }

    @Test
    void uploadedPdfAttachesPdfToolsWithoutKeywords() {
        assertEquals(always(Group.UPLOADED_PDF, Group.DOCUMENTS),
                ToolRouter.select(List.of("What is on page 3?"), true));
        assertEquals(always(Group.UPLOADED_PDF, Group.DOCUMENTS),
                ToolRouter.select(List.of(), true));
        assertEquals(ALWAYS, ToolRouter.select(List.of("What is on page 3?"), false));
    }

    @Test
    void oneMessageCanSelectDocumentsAndTrello() {
        assertEquals(always(Group.TRELLO, Group.DOCUMENTS),
                select("Make a PDF of my Trello board"));
    }
}
