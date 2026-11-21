import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { Menu, PanelRight } from "lucide-react";
import { toast } from "sonner";
import AuthBackground from "../components/AuthBackground";
import ChatView from "../components/chat/ChatView";
import Composer from "../components/chat/Composer";
import LearningRail from "../components/learning/LearningRail";
import { topicName } from "../components/learning/topicName";
import Sidebar from "../components/sidebar/Sidebar";
import * as chatsApi from "../api/chats";
import Settings from "./Settings";

const isWide = () => window.matchMedia("(min-width: 68.75rem)").matches;

export default function StudyDesk() {
  const navigate = useNavigate();
  const onSettings = useLocation().pathname === "/settings";
  const [chats, setChats] = useState([]);
  const [activeId, setActiveId] = useState(null);
  const [railOpen, setRailOpen] = useState(isWide);
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [firstQuestion, setFirstQuestion] = useState(null);
  const [learningVersion, setLearningVersion] = useState(0);
  const [chatsFailed, setChatsFailed] = useState(false);
  const [chatsAttempt, setChatsAttempt] = useState(0);
  const [chatError, setChatError] = useState("");
  const [chatsLoading, setChatsLoading] = useState(true);

  useEffect(() => {
    let ignore = false;
    chatsApi
      .getChats()
      .then((list) => !ignore && setChats(list))
      .catch(() => !ignore && setChatsFailed(true))
      .finally(() => !ignore && setChatsLoading(false));
    return () => {
      ignore = true;
    };
  }, [chatsAttempt]);

  useEffect(() => {
    const closeOnEscape = (e) => {
      if (e.key !== "Escape") return;
      setSidebarOpen(false);
      if (!isWide()) setRailOpen(false);
    };
    window.addEventListener("keydown", closeOnEscape);
    return () => window.removeEventListener("keydown", closeOnEscape);
  }, []);

  const retryChats = () => {
    setChatsFailed(false);
    setChatsLoading(true);
    setChatsAttempt((n) => n + 1);
  };

  const openChat = (id) => {
    setActiveId(id);
    setSidebarOpen(false);
    navigate("/");
  };

  const newChat = async () => {
    setChatError("");
    try {
      const chat = await chatsApi.createChat("New chat");
      setChats([chat, ...chats]);
      openChat(chat.id);
    } catch {
      setChatError("Could not create a new chat. Try again.");
    }
  };

  const startChat = async ({ content, hintMode, devMode }) => {
    setChatError("");
    try {
      const chat = await chatsApi.createChat(content.slice(0, 40));
      setChats([chat, ...chats]);
      setFirstQuestion({ content, hintMode, devMode });
      openChat(chat.id);
      return true;
    } catch {
      setChatError("Could not start a new chat. Try again.");
      return false;
    }
  };

  const reviewTopic = async (concept) => {
    const chat = await chatsApi.createChat(`Review: ${topicName(concept)}`);
    setChats([chat, ...chats]);
    setFirstQuestion({ content: `Quiz me on ${topicName(concept)}` });
    setActiveId(chat.id);
    if (!isWide()) setRailOpen(false);
  };

  const renameChat = async (id, title) => {
    setChatError("");
    try {
      const updated = await chatsApi.renameChat(id, title);
      setChats(chats.map((chat) => (chat.id === id ? updated : chat)));
    } catch {
      setChatError("Could not rename this chat. Try again.");
    }
  };

  const deleteChat = async (id) => {
    const request = chatsApi.deleteChat(id);
    toast.promise(request, {
      loading: "Deleting chat…",
      success: "Chat deleted",
      error: "Could not delete this chat",
    });
    try {
      await request;
      setChats((list) => list.filter((chat) => chat.id !== id));
      if (activeId === id) setActiveId(null);
    } catch {
      return;
    }
  };

  const activeChat = chats.find((chat) => chat.id === activeId);
  const showRail = railOpen && !onSettings;
  const isEmpty = !onSettings && !activeChat;

  return (
    <div
      className={`relative grid h-dvh grid-cols-1 grid-rows-[minmax(0,1fr)] md:grid-cols-[16rem_minmax(0,1fr)] ${
        showRail ? "min-[68.75rem]:grid-cols-[16rem_minmax(0,1fr)_19.5rem]" : ""
      }`}
    >
      {isEmpty && (
        <div className="pointer-events-none absolute inset-x-0 top-0 z-20 flex h-12 items-center px-2 min-[68.75rem]:hidden">
          <button
            onClick={() => setSidebarOpen(true)}
            aria-label="Open chat list"
            aria-expanded={sidebarOpen}
            className="pointer-events-auto grid size-10 place-items-center rounded-lg text-muted-foreground transition-colors hover:bg-card hover:text-foreground md:hidden"
          >
            <Menu className="size-5" />
          </button>
          <button
            onClick={() => setRailOpen(true)}
            aria-label="Open My learning"
            aria-expanded={showRail}
            className="pointer-events-auto ml-auto grid size-10 place-items-center rounded-lg text-muted-foreground transition-colors hover:bg-card hover:text-foreground"
          >
            <PanelRight className="size-5" />
          </button>
        </div>
      )}
      <div
        onClick={() => setSidebarOpen(false)}
        aria-hidden="true"
        className={`fixed inset-0 z-30 bg-black/50 transition-opacity duration-200 motion-reduce:transition-none md:hidden ${
          sidebarOpen ? "opacity-100" : "pointer-events-none opacity-0"
        }`}
      />
      <div
        onClick={() => setRailOpen(false)}
        aria-hidden="true"
        className={`fixed inset-0 z-30 bg-black/50 transition-opacity duration-200 motion-reduce:transition-none min-[68.75rem]:hidden ${
          showRail ? "opacity-100" : "pointer-events-none opacity-0"
        }`}
      />
      <Sidebar
        open={sidebarOpen}
        onClose={() => setSidebarOpen(false)}
        chats={chats}
        chatsFailed={chatsFailed}
        chatsLoading={chatsLoading}
        onRetryChats={retryChats}
        chatError={chatError}
        activeId={onSettings ? null : activeId}
        onSettings={onSettings}
        onSelect={openChat}
        onNew={newChat}
        onRename={renameChat}
        onDelete={deleteChat}
      />
      {onSettings ? (
        <Settings menuOpen={sidebarOpen} onOpenMenu={() => setSidebarOpen(true)} />
      ) : activeChat ? (
        <ChatView
          key={activeChat.id}
          chat={activeChat}
          railOpen={railOpen}
          onToggleRail={() => setRailOpen(!railOpen)}
          menuOpen={sidebarOpen}
          onOpenMenu={() => setSidebarOpen(true)}
          firstQuestion={firstQuestion}
          onFirstQuestionSent={() => setFirstQuestion(null)}
          onScoreSaved={() => setLearningVersion((v) => v + 1)}
        />
      ) : (
        <main className="relative grid place-items-center overflow-hidden px-2 py-6">
          <AuthBackground className="absolute opacity-30 lg:opacity-45" />
          <div className="relative w-full">
            <div className="px-4 text-center">
              <h2 className="text-2xl font-medium">What do you want to learn today?</h2>
              <p className="mt-1.5 text-muted-foreground">Ask a question or pick a chat</p>
            </div>
            <div className="mt-4">
              <Composer onSend={startChat} />
            </div>
          </div>
        </main>
      )}
      {!onSettings && (
        <LearningRail key={learningVersion} open={showRail} onReview={reviewTopic} onClose={() => setRailOpen(false)} />
      )}
    </div>
  );
}
