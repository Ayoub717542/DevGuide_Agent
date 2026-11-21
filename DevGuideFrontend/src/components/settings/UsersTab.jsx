import { useEffect, useState } from "react";
import { getUsers } from "../../api/admin";
import { useAuth } from "../../context/AuthContext";
import { Button } from "../ui/button";
import { Block } from "./Block";
import DeleteUserDialog from "./DeleteUserDialog";
import UserRow from "./UserRow";

export default function UsersTab() {
  const { user: me } = useAuth();
  const [users, setUsers] = useState(null);
  const [error, setError] = useState("");
  const [toDelete, setToDelete] = useState(null);
  const [reload, setReload] = useState(0);

  useEffect(() => {
    let ignore = false;
    getUsers()
      .then((list) => !ignore && setUsers(list))
      .catch((e) => !ignore && setError(e.response?.data?.message || "Could not load users."));
    return () => {
      ignore = true;
    };
  }, [reload]);

  const retry = () => {
    setError("");
    setUsers(null);
    setReload((n) => n + 1);
  };

  const replaceUser = (updated) => {
    setUsers((list) => list.map((u) => (u.id === updated.id ? updated : u)));
  };

  const removeUser = (id) => {
    setUsers((list) => list.filter((u) => u.id !== id));
    setToDelete(null);
  };

  const lead = "Change who is an admin or remove an account. You can't change or delete your own account here";

  return (
    <Block title="Users" lead={lead}>
      {error && (
        <div role="alert" className="flex flex-wrap items-center justify-between gap-3 px-4 py-4 sm:px-5">
          <p className="text-sm text-destructive">{error}</p>
          <Button variant="outline" onClick={retry}>Try again</Button>
        </div>
      )}

      {!error && !users && (
        <p aria-busy="true" className="px-4 py-4 text-sm text-muted-foreground sm:px-5">Loading users…</p>
      )}

      {users?.length === 0 && (
        <p className="px-4 py-4 text-sm text-muted-foreground sm:px-5">No users yet</p>
      )}

      {users?.map((u) => (
        <UserRow key={u.id} user={u} isMe={u.email === me.email} onChanged={replaceUser} onDelete={setToDelete} />
      ))}

      {toDelete && (
        <DeleteUserDialog user={toDelete} onClose={() => setToDelete(null)} onDeleted={removeUser} />
      )}
    </Block>
  );
}
