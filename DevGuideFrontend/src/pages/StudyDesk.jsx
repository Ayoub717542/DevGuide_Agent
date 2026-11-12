import { useEffect, useState } from "react";
import ChatView from "../components/chat/ChatView";
import LearningRail from "../components/learning/LearningRail";
import Sidebar from "../components/sidebar/Sidebar";
import * as chatsApi from "../api/chats";

export default function StudyDesk() {
  const [chats, setChats] = useState([]);
  const [activeId, setActiveId] = useState(null);
  const [railOpen, setRailOpen] = useState(true);

  useEffect(() => {
    chatsApi.getChats().then(setChats);
  }, []);

  const newChat = async () => {
    const chat = await chatsApi.createChat("New chat");
    setChats([chat, ...chats]);
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

  return (
    <div
      className={`grid h-screen grid-cols-[256px_minmax(0,1fr)] ${
        railOpen ? "min-[1100px]:grid-cols-[256px_minmax(0,1fr)_312px]" : ""
      }`}
    >
      <Sidebar
        chats={chats}
        activeId={activeId}
        onSelect={setActiveId}
        onNew={newChat}
        onRename={renameChat}
        onDelete={deleteChat}
      />
      {activeChat ? (
        <ChatView
          key={activeChat.id}
          chat={activeChat}
          railOpen={railOpen}
          onToggleRail={() => setRailOpen(!railOpen)}
        />
      ) : (
        <main className="grid place-items-center p-6 text-center">
          <div>
            <h2 className="text-2xl font-semibold tracking-tight">What do you want to learn today?</h2>
            <p className="mt-1.5 text-muted-foreground">Pick a chat on the left or start a new one.</p>
          </div>
        </main>
      )}
      {railOpen && <LearningRail />}
    </div>
  );
}
