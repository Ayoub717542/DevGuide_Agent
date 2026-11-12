import { useState } from "react";
import { Pencil, Trash2 } from "lucide-react";

export default function ChatRow({ chat, active, onSelect, onRename, onDelete }) {
  const [editing, setEditing] = useState(false);
  const [title, setTitle] = useState("");

  const startEditing = () => {
    setTitle(chat.title);
    setEditing(true);
  };

  const save = (e) => {
    e.preventDefault();
    setEditing(false);
    const newTitle = title.trim();
    if (newTitle && newTitle !== chat.title) onRename(chat.id, newTitle);
  };

  const remove = () => {
    if (window.confirm(`Delete "${chat.title}" with all its messages and files?`)) {
      onDelete(chat.id);
    }
  };

  if (editing) {
    return (
      <form onSubmit={save}>
        <input
          autoFocus
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          onBlur={() => setEditing(false)}
          onKeyDown={(e) => e.key === "Escape" && setEditing(false)}
          aria-label="Chat title"
          className="w-full rounded-md border border-ring bg-card px-2.5 py-1.5 text-sm outline-none"
        />
      </form>
    );
  }

  return (
    <div className="group relative">
      <button
        onClick={() => onSelect(chat.id)}
        className={`relative block w-full truncate rounded-md py-1.75 pr-14 pl-2.5 text-left text-sm ${
          active
            ? "bg-card text-foreground before:absolute before:inset-y-2 before:left-0 before:w-0.5 before:rounded before:bg-success"
            : "text-muted-foreground hover:bg-card hover:text-foreground"
        }`}
      >
        {chat.title}
      </button>
      <span className="absolute top-1/2 right-1.5 flex -translate-y-1/2 gap-0.5 opacity-0 group-hover:opacity-100 focus-within:opacity-100">
        <button onClick={startEditing} aria-label="Rename chat" className="rounded p-1 text-muted-foreground hover:bg-border hover:text-foreground">
          <Pencil className="size-3.5" />
        </button>
        <button onClick={remove} aria-label="Delete chat" className="rounded p-1 text-muted-foreground hover:bg-border hover:text-destructive">
          <Trash2 className="size-3.5" />
        </button>
      </span>
    </div>
  );
}
