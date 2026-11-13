import { useState } from "react";
import { deleteAccount } from "../../api/user";
import { useAuth } from "../../context/AuthContext";
import { Button } from "../ui/button";
import { Input } from "../ui/input";
import { Block, Row } from "./Block";

export default function DeleteAccount() {
  const { user, logout } = useAuth();
  const [open, setOpen] = useState(false);
  const [typed, setTyped] = useState("");
  const [deleting, setDeleting] = useState(false);
  const [error, setError] = useState("");

  const close = () => {
    setOpen(false);
    setTyped("");
    setError("");
  };

  const confirm = async () => {
    setDeleting(true);
    try {
      await deleteAccount();
      logout();
    } catch {
      setError("Could not delete your account. Try again.");
      setDeleting(false);
    }
  };

  const help = (
    <p className="mt-0.5 text-[0.8125rem] text-muted-foreground">
      All chats, PDFs, files made by your tutor, quiz results and notes are deleted. Trello is disconnected.
    </p>
  );

  return (
    <Block title="Delete account" lead="Removes your account and everything in it. This can't be undone." danger>
      <Row title="Delete my account" help={help}>
        <Button variant="destructive" onClick={() => setOpen(true)}>Delete account</Button>
      </Row>

      {open && (
        <div className="fixed inset-0 z-50 grid place-items-center bg-black/60 p-4" onClick={close}>
          <div
            role="dialog"
            aria-modal="true"
            aria-labelledby="delete-title"
            onClick={(e) => e.stopPropagation()}
            className="w-full max-w-110 rounded-xl border bg-secondary shadow-2xl"
          >
            <h2 id="delete-title" className="px-5.5 pt-5 pb-1.5 text-base font-semibold">Delete your account?</h2>
            <div className="grid gap-3 px-5.5 pb-4 text-[0.8125rem] text-muted-foreground">
              <p>This permanently deletes your chats, PDFs, files, quiz results, notes and your Trello connection.</p>
              <label className="grid gap-1.5">
                <span>
                  To confirm, type <code className="text-foreground">{user.email}</code>
                </span>
                <Input value={typed} onChange={(e) => setTyped(e.target.value)} autoComplete="off" spellCheck={false} autoFocus />
              </label>
              {error && <p className="text-xs text-destructive">{error}</p>}
            </div>
            <footer className="flex justify-end gap-2 border-t px-5.5 py-3.5">
              <Button variant="ghost" onClick={close}>Cancel</Button>
              <Button
                onClick={confirm}
                disabled={typed.trim() !== user.email || deleting}
                className="bg-destructive text-white hover:bg-destructive/90"
              >
                {deleting ? "Deleting…" : "Delete account"}
              </Button>
            </footer>
          </div>
        </div>
      )}
    </Block>
  );
}
