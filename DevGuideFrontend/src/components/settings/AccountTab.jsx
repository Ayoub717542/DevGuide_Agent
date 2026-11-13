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
      <div className="mb-5.5 flex items-center gap-4">
        <div className="grid size-14 place-items-center rounded-full border bg-card text-[1.375rem] font-semibold">
          {name[0].toUpperCase()}
        </div>
        <div>
          <b className="block text-[1.0625rem] font-semibold">{name}</b>
          <span className="text-[0.84375rem] text-muted-foreground">{user.email}</span>
        </div>
      </div>

      <Block title="Profile" lead="Your tutor uses your name when it talks to you.">
        <NameRow />
        <EmailRow />
      </Block>

      <PasswordBlock />
      <DeleteAccount />
    </>
  );
}
