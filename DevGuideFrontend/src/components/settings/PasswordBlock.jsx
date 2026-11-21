import { useState } from "react";
import { toast } from "sonner";
import { changePassword } from "../../api/user";
import { Button } from "../ui/button";
import { Input } from "../ui/input";
import { Block, Row } from "./Block";

export default function PasswordBlock() {
  const [open, setOpen] = useState(false);
  const [current, setCurrent] = useState("");
  const [next, setNext] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const close = () => {
    setOpen(false);
    setCurrent("");
    setNext("");
    setError("");
  };

  const save = async () => {
    setSaving(true);
    setError("");
    try {
      await changePassword(current, next);
      close();
      toast.success("Password changed");
    } catch (e) {
      if (e.response?.status === 400) {
        setError(e.response.data?.message || "Check your current password");
      } else {
        toast.error("Could not change your password", { description: "Try again" });
      }
    } finally {
      setSaving(false);
    }
  };

  const help = (
    <p className="mt-0.5 text-[0.8125rem] lg:text-sm text-muted-foreground">
      Signed up with Google or GitHub? Then you have no password here.
    </p>
  );

  return (
    <Block title="Password" lead="Use at least 8 characters. You stay logged in after changing it.">
      <Row help={help}>
        {!open && (
          <Button variant="outline" onClick={() => setOpen(true)}>
            Change password
          </Button>
        )}
      </Row>

      {open && (
        <div className="grid gap-3 rounded-b-xl border-t bg-background px-5 pt-4 pb-5">
          <label className="grid max-w-80 gap-1.5 text-[0.8125rem] lg:text-sm text-muted-foreground">
            Current password
            <Input type="password" value={current} onChange={(e) => setCurrent(e.target.value)} autoComplete="current-password" />
          </label>
          <label className="grid max-w-80 gap-1.5 text-[0.8125rem] lg:text-sm text-muted-foreground">
            New password
            <Input type="password" value={next} onChange={(e) => setNext(e.target.value)} autoComplete="new-password" />
          </label>
          {error && <p className="text-xs text-destructive">{error}</p>}
          <div className="flex gap-2">
            <Button onClick={save} disabled={!current || next.length < 8 || saving}>
              {saving ? "Saving…" : "Change password"}
            </Button>
            <Button variant="ghost" onClick={close}>Cancel</Button>
          </div>
        </div>
      )}
    </Block>
  );
}
