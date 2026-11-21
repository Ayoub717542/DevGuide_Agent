import { useState } from "react";
import { toast } from "sonner";
import { updateName } from "../../api/user";
import { useAuth } from "../../context/AuthContext";
import { Button } from "../ui/button";
import { Input } from "../ui/input";
import { Row } from "./Block";

export default function NameRow() {
  const { user, setUser } = useAuth();
  const [name, setName] = useState(user.name || "");
  const [saving, setSaving] = useState(false);

  const tooShort = name.trim().length < 2;
  const changed = name.trim() !== (user.name || "");

  const save = async () => {
    setSaving(true);
    try {
      setUser(await updateName(name.trim()));
      toast.success("Name saved");
    } catch {
      toast.error("Could not save your name", { description: "Try again" });
    } finally {
      setSaving(false);
    }
  };

  const help = (
    <>
      <p className="mt-0.5 text-[0.8125rem] lg:text-sm text-muted-foreground">2 to 100 characters</p>
      {changed && tooShort && <p className="mt-1.5 text-xs text-destructive">Enter at least 2 characters</p>}
    </>
  );

  return (
    <Row title="Name" help={help}>
      <Input
        value={name}
        onChange={(e) => setName(e.target.value)}
        maxLength={100}
        aria-label="Name"
        aria-invalid={changed && tooShort}
        className="w-full sm:w-65"
      />
      {changed && (
        <Button onClick={save} disabled={tooShort || saving}>
          {saving ? "Saving…" : "Save"}
        </Button>
      )}
    </Row>
  );
}
