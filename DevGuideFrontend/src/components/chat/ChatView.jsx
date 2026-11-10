import { useEffect, useRef, useState } from "react";
import { getMessages } from "../../api/chats";
import { streamReply } from "../../api/stream";
import ChatHeader from "./ChatHeader";
import Composer from "./Composer";
import MessageList from "./MessageList";

export default function ChatView({ chat }) {
  const [messages, setMessages] = useState([]);
  const [answer, setAnswer] = useState(null);
  const [error, setError] = useState("");
  const stopRef = useRef(null);

  useEffect(() => {
    getMessages(chat.id).then(setMessages);
    return () => stopRef.current?.abort();
  }, [chat.id]);

  const send = async ({ content, hintMode, devMode }) => {
    const question = { id: `question-${Date.now()}`, role: "USER", content };
    const removeQuestion = () => setMessages((list) => list.filter((m) => m !== question));
    setMessages((list) => [...list, question]);
    setAnswer("");
    setError("");

    const controller = new AbortController();
    stopRef.current = controller;
    try {
      await streamReply(
        { conversationId: chat.id, content, hintMode, devMode },
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

  return (
    <main className="flex min-h-0 min-w-0 flex-col">
      <ChatHeader chat={chat} />
      <MessageList messages={messages} answer={answer} error={error} onAsk={(content) => send({ content })} />
      <Composer busy={answer !== null} onSend={send} onStop={stop} />
    </main>
  );
}
