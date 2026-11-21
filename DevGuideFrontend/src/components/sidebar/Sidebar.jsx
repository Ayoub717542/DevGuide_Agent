import { useState } from "react";
import { Link } from "react-router-dom";
import { LogOut, Plus, Search, Settings } from "lucide-react";
import { useAuth } from "../../context/AuthContext";
import { ConfirmDialog } from "../ui/confirm-dialog";
import ChatList from "./ChatList";

export default function Sidebar({ open, onClose, chats, chatsFailed, chatsLoading, onRetryChats, chatError, activeId, onSettings, onSelect, onNew, onRename, onDelete }) {
  const { user, logout } = useAuth();
  const [search, setSearch] = useState("");
  const [confirmingLogout, setConfirmingLogout] = useState(false);

  const shownChats = chats.filter((chat) =>
    chat.title.toLowerCase().includes(search.toLowerCase())
  );
  const name = user.name || user.email;

  return (
    <aside
      className={`fixed top-0 left-0 z-40 flex h-dvh min-h-0 w-64 flex-col bg-secondary pb-[env(safe-area-inset-bottom)] transition-[translate,visibility] duration-200 ease-out motion-reduce:transition-none md:visible md:static md:h-auto md:w-auto md:translate-x-0 ${
        open ? "translate-x-0" : "invisible -translate-x-full"
      }`}
    >
      <div className="flex items-center gap-2.5 px-4.5 pt-4.5 pb-3.5 text-[0.9375rem] font-semibold">
        <img src="/devguide-logo.svg" alt="" className="size-7" />
        DevGuide
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
          className="w-full bg-transparent text-[0.8125rem] text-foreground outline-none placeholder:text-muted-foreground"
        />
      </label>

      {chatError && <p className="mx-4.5 py-1 text-[0.8125rem] text-destructive">{chatError}</p>}

      <ChatList
        chats={shownChats}
        failed={chatsFailed}
        loading={chatsLoading}
        onRetry={onRetryChats}
        activeId={activeId}
        onSelect={onSelect}
        onRename={onRename}
        onDelete={onDelete}
      />

      <div className="flex items-center gap-2.5 border-t px-3 py-3">
        <div className="grid size-8 shrink-0 place-items-center rounded-full bg-card text-[0.8125rem] font-semibold">
          {name[0].toUpperCase()}
        </div>
        <div className="min-w-0 text-sm">
          <div className="truncate font-medium">{name}</div>
          <div className="truncate text-xs text-muted-foreground">{user.email}</div>
        </div>
        <div className="ml-auto flex shrink-0 gap-1">
          <Link
            to="/settings"
            onClick={onClose}
            aria-label="Settings"
            aria-current={onSettings ? "page" : undefined}
            className={`grid size-8 place-items-center rounded-md hover:bg-card hover:text-foreground ${
              onSettings ? "bg-card text-foreground" : "text-muted-foreground"
            }`}
          >
            <Settings className="size-4" />
          </Link>
          <button
            onClick={() => setConfirmingLogout(true)}
            aria-label="Log out"
            className="grid size-8 place-items-center rounded-md text-muted-foreground hover:bg-card hover:text-foreground"
          >
            <LogOut className="size-4" />
          </button>
        </div>
      </div>
      <ConfirmDialog
        open={confirmingLogout}
        onOpenChange={setConfirmingLogout}
        title="Log out?"
        description="You'll need to log in again to see your chats."
        confirmLabel="Log out"
        danger={false}
        onConfirm={logout}
      />
    </aside>
  );
}
