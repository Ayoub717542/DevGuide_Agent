import { useState } from "react";
import { toast } from "sonner";
import { deleteUser } from "../../api/admin";
import { Button } from "../ui/button";
import { Input } from "../ui/input";

export default function DeleteUserDialog({ user, onClose, onDeleted }) {
  const [typed, setTyped] = useState("");
  const [deleting, setDeleting] = useState(false);
  const [error, setError] = useState("");

  const confirm = async () => {
    setDeleting(true);
    setError("");
    try {
      await deleteUser(user.id);
      onDeleted(user.id);
      toast.success("User deleted", { description: `${user.email} and all their data were removed` });
    } catch (e) {
      setError(e.response?.data?.message || "Could not delete this user. Try again.");
      setDeleting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-black/60 p-4" onClick={onClose}>
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="delete-user-title"
        onClick={(e) => e.stopPropagation()}
        onKeyDown={(e) => e.key === "Escape" && onClose()}
        className="w-full max-w-110 rounded-xl border bg-secondary shadow-2xl"
      >
        <h2 id="delete-user-title" className="px-5 pt-5 pb-2 text-base font-medium [overflow-wrap:anywhere]">
          Delete {user.name || user.email}?
        </h2>
        <div className="grid gap-3 px-5 pb-4 text-[0.8125rem] lg:text-sm text-muted-foreground">
          <p>This permanently deletes their chats, PDFs, files, quiz results, notes and Trello connection.</p>
          <label className="grid gap-1.5">
            <span className="[overflow-wrap:anywhere]">
              To confirm, type <code className="text-foreground">{user.email}</code>
            </span>
            <Input value={typed} onChange={(e) => setTyped(e.target.value)} autoComplete="off" spellCheck={false} autoFocus />
          </label>
          {error && <p role="alert" className="text-xs text-destructive">{error}</p>}
        </div>
        <footer className="flex justify-end gap-2 border-t px-5 py-4">
          <Button variant="ghost" onClick={onClose}>Cancel</Button>
          <Button
            onClick={confirm}
            disabled={typed.trim() !== user.email || deleting}
            className="bg-destructive text-white hover:bg-destructive/90"
          >
            {deleting ? "Deleting…" : "Delete user"}
          </Button>
        </footer>
      </div>
    </div>
  );
}
