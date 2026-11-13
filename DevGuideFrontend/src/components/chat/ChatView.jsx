import { useEffect, useEffectEvent, useRef, useState } from "react";
import * as chatsApi from "../../api/chats";
import * as documentsApi from "../../api/documents";
import { streamReply } from "../../api/stream";
import { getPendingActions } from "../../api/trello";
import ChatHeader from "./ChatHeader";
import Composer from "./Composer";
import MessageList from "./MessageList";

const newestFirst = (pdfs) => [...pdfs].sort((a, b) => (b.createdAt || "").localeCompare(a.createdAt || ""));

export default function ChatView({
  chat,
  railOpen,
  onToggleRail,
  firstQuestion,
  onFirstQuestionSent,
  onScoreSaved,
}) {
  const [messages, setMessages] = useState([]);
  const [pdfs, setPdfs] = useState([]);
  const [answer, setAnswer] = useState(null);
  const [error, setError] = useState("");
  const [uploading, setUploading] = useState(false);
  const [loading, setLoading] = useState(null);
  const [pending, setPending] = useState([]);
  const stopRef = useRef(null);

  const loadPending = () => getPendingActions().then(setPending).catch(() => setPending([]));

  const loadPdfs = () => documentsApi.getChatPdfs(chat.id).then((list) => setPdfs(newestFirst(list)));

  const send = async ({ content, hintMode, devMode, file }) => {
    const question = { id: crypto.randomUUID(), role: "USER", content, fileName: file?.name };
    const removeQuestion = () => setMessages((list) => list.filter((m) => m !== question));
    setMessages((list) => [...list, question]);
    setAnswer("");
    setError("");

    const isPdf = file?.type === "application/pdf";
    const controller = new AbortController();
    stopRef.current = controller;
    let failed = false;
    try {
      await streamReply(
        { conversationId: chat.id, content, hintMode, devMode, file: isPdf ? null : file },
        {
          onDelta: (text) => setAnswer((current) => current + text),
          onDone: (saved) => {
            setMessages((list) => [...list, saved]);
            loadPending();
          },
          onError: (message) => {
            failed = true;
            setError(message);
            removeQuestion();
          },
        },
        controller.signal
      );
    } catch (e) {
      if (e.name !== "AbortError") {
        failed = true;
        setError("Connection lost. Please try again.");
      }
      removeQuestion();
    } finally {
      setAnswer(null);
    }
    return !failed;
  };

  const askFirstQuestion = useEffectEvent(() => {
    if (!firstQuestion) return;
    onFirstQuestionSent();
    send({ content: firstQuestion });
  });

  useEffect(() => {
    let active = true;
    chatsApi.getMessages(chat.id).then((list) => {
      if (!active) return;
      setMessages(list);
      askFirstQuestion();
    });
    documentsApi.getChatPdfs(chat.id).then((list) => setPdfs(newestFirst(list)));
    getPendingActions().then(setPending).catch(() => setPending([]));
    return () => {
      active = false;
      stopRef.current?.abort();
    };
  }, [chat.id]);

  const stop = () => stopRef.current?.abort();

  const uploadPdf = async (file) => {
    setUploading(true);
    setError("");
    try {
      await documentsApi.uploadPdf(chat.id, file);
      await loadPdfs();
      return true;
    } catch (e) {
      setError(e.response?.data?.message || "Could not upload this PDF.");
      return false;
    } finally {
      setUploading(false);
    }
  };

  const addCard = async (type, create) => {
    setLoading(type);
    setError("");
    try {
      const data = await create(chat.id);
      setMessages((list) => [...list, { id: crypto.randomUUID(), type, data }]);
    } catch (e) {
      setError(e.response?.data?.message || "Could not create it. Please try again.");
    } finally {
      setLoading(null);
    }
  };

  const trelloFinished = (text) => {
    setMessages((list) => [...list, { id: crypto.randomUUID(), type: "note", data: text }]);
    loadPending();
  };

  const deletePdf = async (documentId) => {
    await documentsApi.deletePdf(documentId);
    await loadPdfs();
  };

  return (
    <main className="flex min-h-0 min-w-0 flex-col">
      <ChatHeader
        chat={chat}
        pdfs={pdfs}
        uploading={uploading}
        loading={loading}
        onUpload={uploadPdf}
        onDeletePdf={deletePdf}
        onQuiz={() => addCard("quiz", chatsApi.createQuiz)}
        onChallenge={() => addCard("challenge", chatsApi.createChallenge)}
        railOpen={railOpen}
        onToggleRail={onToggleRail}
      />
      <MessageList
        chatId={chat.id}
        messages={messages}
        pdfs={pdfs}
        pending={pending}
        answer={answer}
        error={error}
        onAsk={(content) => send({ content })}
        onTrelloFinished={trelloFinished}
        onScoreSaved={onScoreSaved}
      />
      <Composer busy={answer !== null} onSend={send} onStop={stop} onUploadPdf={uploadPdf} />
    </main>
  );
}
