import { useEffect, useRef, useState } from "react";
import { getMessages } from "../../api/chats";
import * as documentsApi from "../../api/documents";
import { streamReply } from "../../api/stream";
import ChatHeader from "./ChatHeader";
import Composer from "./Composer";
import MessageList from "./MessageList";

const newestFirst = (pdfs) => [...pdfs].sort((a, b) => (b.createdAt || "").localeCompare(a.createdAt || ""));

export default function ChatView({ chat }) {
  const [messages, setMessages] = useState([]);
  const [pdfs, setPdfs] = useState([]);
  const [answer, setAnswer] = useState(null);
  const [error, setError] = useState("");
  const [uploading, setUploading] = useState(false);
  const stopRef = useRef(null);

  const loadPdfs = () => documentsApi.getChatPdfs(chat.id).then((list) => setPdfs(newestFirst(list)));

  useEffect(() => {
    getMessages(chat.id).then(setMessages);
    documentsApi.getChatPdfs(chat.id).then((list) => setPdfs(newestFirst(list)));
    return () => stopRef.current?.abort();
  }, [chat.id]);

  const send = async ({ content, hintMode, devMode, file }) => {
    const question = { id: crypto.randomUUID(), role: "USER", content, fileName: file?.name };
    const removeQuestion = () => setMessages((list) => list.filter((m) => m !== question));
    setMessages((list) => [...list, question]);
    setAnswer("");
    setError("");

    const isPdf = file?.type === "application/pdf";
    const controller = new AbortController();
    stopRef.current = controller;
    try {
      await streamReply(
        { conversationId: chat.id, content, hintMode, devMode, file: isPdf ? null : file },
        {
          onDelta: (text) => setAnswer((current) => current + text),
          onDone: (saved) => setMessages((list) => [...list, saved]),
          onError: (message) => {
            setError(message);
            removeQuestion();
          },
        },
        controller.signal
      );
    } catch (e) {
      if (e.name !== "AbortError") setError("Connection lost. Please try again.");
      removeQuestion();
    } finally {
      setAnswer(null);
    }
  };

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

  const deletePdf = async (documentId) => {
    await documentsApi.deletePdf(documentId);
    await loadPdfs();
  };

  return (
    <main className="flex min-h-0 min-w-0 flex-col">
      <ChatHeader chat={chat} pdfs={pdfs} uploading={uploading} onUpload={uploadPdf} onDeletePdf={deletePdf} />
      <MessageList messages={messages} pdfs={pdfs} answer={answer} error={error} onAsk={(content) => send({ content })} />
      <Composer busy={answer !== null} onSend={send} onStop={stop} onUploadPdf={uploadPdf} />
    </main>
  );
}
