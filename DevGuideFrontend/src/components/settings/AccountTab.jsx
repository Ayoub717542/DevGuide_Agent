import { useAuth } from "../../context/AuthContext";
import { Block } from "./Block";
import DeleteAccount from "./DeleteAccount";
import EmailRow from "./EmailRow";
import NameRow from "./NameRow";
import PasswordBlock from "./PasswordBlock";

export default function AccountTab() {
  const { user } = useAuth();
  const name = user.name || user.email;

  return (
    <>
      <div className="mb-6 flex items-center gap-4">
        <div className="grid size-14 place-items-center rounded-full border bg-card text-xl font-medium">
          {name[0].toUpperCase()}
        </div>
        <div>
          <b className="block text-base font-medium">{name}</b>
          <span className="text-sm text-muted-foreground">{user.email}</span>
        </div>
      </div>

      <Block title="Profile" lead="Your tutor uses your name when it talks to you">
        <NameRow />
        <EmailRow />
      </Block>

      <PasswordBlock />
      <DeleteAccount />
    </>
  );
}
