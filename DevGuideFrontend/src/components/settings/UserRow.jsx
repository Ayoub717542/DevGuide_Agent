import { useState } from "react";
import { toast } from "sonner";
import { updateRole } from "../../api/admin";
import { Button } from "../ui/button";
import { Row } from "./Block";

export default function UserRow({ user, isMe, onChanged, onDelete }) {
  const [saving, setSaving] = useState(false);
  const name = user.name || user.email;

  const changeRole = async (role) => {
    setSaving(true);
    try {
      const updated = await updateRole(user.id, role);
      onChanged(updated);
      toast.success("Role changed", {
        description: `${updated.email} is now ${updated.role === "ADMIN" ? "an admin" : "a user"}`,
      });
    } catch (e) {
      toast.error("Could not change the role", { description: e.response?.data?.message || "Try again" });
    } finally {
      setSaving(false);
    }
  };

  const title = (
    <span className="[overflow-wrap:anywhere]">
      {name}
      {isMe && <span className="ml-1.5 font-normal text-muted-foreground">(you)</span>}
    </span>
  );

  const help = (
    <p className="mt-0.5 text-[0.8125rem] lg:text-sm text-muted-foreground [overflow-wrap:anywhere]">{user.email}</p>
  );

  return (
    <Row title={title} help={help}>
      <select
        value={user.role}
        onChange={(e) => changeRole(e.target.value)}
        disabled={isMe || saving}
        aria-label={`Role of ${name}`}
        className="h-9 rounded-lg border border-input bg-background px-2.5 text-sm transition-colors outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 disabled:cursor-not-allowed disabled:opacity-50"
      >
        <option value="USER">User</option>
        <option value="ADMIN">Admin</option>
      </select>
      <Button
        variant="destructive"
        onClick={() => onDelete(user)}
        disabled={isMe}
        aria-label={`Delete ${name}`}
        className="h-9"
      >
        Delete
      </Button>
    </Row>
  );
}
