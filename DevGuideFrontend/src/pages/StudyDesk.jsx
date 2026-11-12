import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import ChatView from "../components/chat/ChatView";
import LearningRail from "../components/learning/LearningRail";
import { topicName } from "../components/learning/topicName";
import Sidebar from "../components/sidebar/Sidebar";
import * as chatsApi from "../api/chats";
import Settings from "./Settings";

export default function StudyDesk() {
  const navigate = useNavigate();
  const onSettings = useLocation().pathname === "/settings";
  const [chats, setChats] = useState([]);
  const [activeId, setActiveId] = useState(null);
  const [railOpen, setRailOpen] = useState(true);
  const [firstQuestion, setFirstQuestion] = useState(null);
  const [learningVersion, setLearningVersion] = useState(0);

  useEffect(() => {
    chatsApi.getChats().then(setChats);
  }, []);

  const openChat = (id) => {
    setActiveId(id);
    navigate("/");
  };

  const newChat = async () => {
    const chat = await chatsApi.createChat("New chat");
    setChats([chat, ...chats]);
    openChat(chat.id);
  };

  const reviewTopic = async (concept) => {
    const chat = await chatsApi.createChat(`Review: ${topicName(concept)}`);
    setChats([chat, ...chats]);
    setFirstQuestion(`Quiz me on ${topicName(concept)}`);
    setActiveId(chat.id);
  };

  const renameChat = async (id, title) => {
    const updated = await chatsApi.renameChat(id, title);
    setChats(chats.map((chat) => (chat.id === id ? updated : chat)));
  };

  const deleteChat = async (id) => {
    await chatsApi.deleteChat(id);
    setChats(chats.filter((chat) => chat.id !== id));
    if (activeId === id) setActiveId(null);
  };

  const activeChat = chats.find((chat) => chat.id === activeId);
  const showRail = railOpen && !onSettings;

  return (
    <div
      className={`grid h-screen grid-cols-[256px_minmax(0,1fr)] ${
        showRail ? "min-[68.75rem]:grid-cols-[16rem_minmax(0,1fr)_19.5rem]" : ""
      }`}
    >
      <Sidebar
        chats={chats}
        activeId={onSettings ? null : activeId}
        onSettings={onSettings}
        onSelect={openChat}
        onNew={newChat}
        onRename={renameChat}
        onDelete={deleteChat}
      />
      {onSettings ? (
        <Settings />
      ) : activeChat ? (
        <ChatView
          key={activeChat.id}
          chat={activeChat}
          railOpen={railOpen}
          onToggleRail={() => setRailOpen(!railOpen)}
          firstQuestion={firstQuestion}
          onFirstQuestionSent={() => setFirstQuestion(null)}
          onScoreSaved={() => setLearningVersion((v) => v + 1)}
        />
      ) : (
        <main className="grid place-items-center p-6 text-center">
          <div>
            <h2 className="text-2xl font-semibold tracking-tight">What do you want to learn today?</h2>
            <p className="mt-1.5 text-muted-foreground">Pick a chat on the left or start a new one.</p>
          </div>
        </main>
      )}
      {showRail && <LearningRail key={learningVersion} onReview={reviewTopic} />}
    </div>
  );
}
