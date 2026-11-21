import { useState } from "react";
import { Pencil, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { ConfirmDialog } from "@/components/ui/confirm-dialog";

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

  const [confirming, setConfirming] = useState(false);

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
          className="h-8 w-full rounded-md border border-ring bg-card px-2.5 text-sm outline-none"
        />
      </form>
    );
  }

  return (
    <div className="group relative">
      <button
        onClick={() => onSelect(chat.id)}
        className={`relative block h-8 w-full truncate rounded-md pr-14 pl-2.5 text-left text-sm leading-8 pointer-coarse:pr-18 ${
          active
            ? "bg-card text-foreground before:absolute before:inset-y-2 before:left-0 before:w-0.5 before:rounded before:bg-success"
            : "text-muted-foreground hover:bg-card hover:text-foreground"
        }`}
      >
        {chat.title}
      </button>
      <span className="absolute top-1/2 right-1.5 flex -translate-y-1/2 gap-0.5 opacity-0 group-hover:opacity-100 focus-within:opacity-100 pointer-coarse:opacity-100">
        <Button variant="ghost" size="icon-xs" onClick={startEditing} aria-label={`Rename "${chat.title}"`} className="text-muted-foreground hover:bg-border pointer-coarse:size-8">
          <Pencil className="size-3.5" aria-hidden="true" />
        </Button>
        <Button variant="ghost" size="icon-xs" onClick={() => setConfirming(true)} aria-label={`Delete "${chat.title}"`} className="text-muted-foreground hover:bg-border hover:text-destructive pointer-coarse:size-8">
          <Trash2 className="size-3.5" aria-hidden="true" />
        </Button>
      </span>
      <ConfirmDialog
        open={confirming}
        onOpenChange={setConfirming}
        title={`Delete "${chat.title}"?`}
        description="All its messages and files are deleted. This can't be undone."
        confirmLabel="Delete chat"
        onConfirm={() => onDelete(chat.id)}
      />
    </div>
  );
}
