import { useState } from "react";
import { Link } from "react-router-dom";
import { LogOut, Plus, Search, Settings } from "lucide-react";
import { useAuth } from "../../context/AuthContext";
import ChatList from "./ChatList";

export default function Sidebar({ chats, activeId, onSettings, onSelect, onNew, onRename, onDelete }) {
  const { user, logout } = useAuth();
  const [search, setSearch] = useState("");

  const shownChats = chats.filter((chat) =>
    chat.title.toLowerCase().includes(search.toLowerCase())
  );
  const name = user.name || user.email;

  return (
    <aside className="flex min-h-0 flex-col bg-secondary">
      <div className="flex items-center gap-2.5 px-4.5 pt-4.5 pb-3.5 text-[15px] font-semibold">
        <img src="/devguide-logo.svg" alt="" className="size-7" />
        DevGuide AI
      </div>

      <button
        onClick={onNew}
        className="mx-3 flex h-9 items-center gap-2 rounded-lg border bg-card px-3 text-sm font-medium hover:border-muted-foreground/40"
      >
        <Plus className="size-4 text-muted-foreground" /> New chat
      </button>

      <label className="mx-3 mt-2 mb-1 flex h-9 items-center gap-2 rounded-lg border px-3 text-muted-foreground focus-within:border-ring">
        <Search className="size-4" />
        <input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search chats"
          className="w-full bg-transparent text-[13px] text-foreground outline-none placeholder:text-muted-foreground"
        />
      </label>

      <ChatList
        chats={shownChats}
        activeId={activeId}
        onSelect={onSelect}
        onRename={onRename}
        onDelete={onDelete}
      />

      <div className="flex items-center gap-2.5 border-t px-3 py-3">
        <div className="grid size-8 shrink-0 place-items-center rounded-full bg-card text-[13px] font-semibold">
          {name[0].toUpperCase()}
        </div>
        <div className="min-w-0 text-sm">
          <div className="truncate font-medium">{name}</div>
          <div className="truncate text-xs text-muted-foreground">{user.email}</div>
        </div>
        <div className="ml-auto flex shrink-0 gap-1">
          <Link
            to="/settings"
            aria-label="Settings"
            aria-current={onSettings ? "page" : undefined}
            className={`grid size-8 place-items-center rounded-md hover:bg-card hover:text-foreground ${
              onSettings ? "bg-card text-foreground" : "text-muted-foreground"
            }`}
          >
            <Settings className="size-4" />
          </Link>
          <button
            onClick={logout}
            aria-label="Log out"
            className="grid size-8 place-items-center rounded-md text-muted-foreground hover:bg-card hover:text-foreground"
          >
            <LogOut className="size-4" />
          </button>
        </div>
      </div>
    </aside>
  );
}
