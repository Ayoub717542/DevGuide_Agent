import { useState } from "react";
import { BookMarked, LogOut, Plus, Search, SquareKanban } from "lucide-react";
import { useAuth } from "../../context/AuthContext";
import ChatList from "./ChatList";

export default function Sidebar({ chats, activeId, onSelect, onNew, onRename, onDelete }) {
  const { user, logout } = useAuth();
  const [search, setSearch] = useState("");

  const shownChats = chats.filter((chat) =>
    chat.title.toLowerCase().includes(search.toLowerCase())
  );
  const name = user.name || user.email;

  return (
    <aside className="flex min-h-0 flex-col border-r bg-secondary">
      <div className="flex items-center gap-2.5 px-4.5 pt-4.5 pb-3.5 text-[15px] font-semibold">
        <img src="/devguide-logo.svg" alt="" className="size-7" />
        DevGuide AI
      </div>

      <button
        onClick={onNew}
        className="mx-3.5 flex h-9 items-center justify-center gap-2 rounded-lg bg-primary font-semibold text-primary-foreground hover:bg-primary/90"
      >
        <Plus className="size-4" /> New chat
      </button>

      <label className="mx-3.5 mt-3 mb-1.5 flex h-8.5 items-center gap-2 rounded-lg border px-2.5 text-muted-foreground focus-within:border-ring">
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

      <div className="border-t p-2">
        <Connection icon={SquareKanban} name="Trello" connected={user.trelloConnected} />
        <Connection icon={BookMarked} name="GitHub" connected={user.githubConnected} />
      </div>

      <div className="flex items-center gap-2.5 border-t px-4 py-3">
        <div className="grid size-7.5 shrink-0 place-items-center rounded-full border bg-card text-[13px] font-semibold">
          {name[0].toUpperCase()}
        </div>
        <div className="min-w-0 text-sm">
          <div className="truncate">{name}</div>
          <div className="truncate text-xs text-muted-foreground">{user.email}</div>
        </div>
        <button
          onClick={logout}
          aria-label="Log out"
          className="ml-auto rounded-md p-1 text-muted-foreground hover:text-foreground"
        >
          <LogOut className="size-4" />
        </button>
      </div>
    </aside>
  );
}

function Connection({ icon: Icon, name, connected }) {
  return (
    <div className="flex items-center gap-2.5 rounded-md px-2.5 py-1.5 text-sm text-muted-foreground">
      <Icon className="size-4" />
      {name}
      <span className={`ml-auto text-xs ${connected ? "text-success" : ""}`}>
        {connected ? "Connected" : "Not connected"}
      </span>
    </div>
  );
}
