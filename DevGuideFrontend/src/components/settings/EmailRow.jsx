import { Check, Mail } from "lucide-react";
import { useState } from "react";
import { resendVerification } from "../../api/Client";
import { useAuth } from "../../context/AuthContext";
import { Button } from "../ui/button";
import { Row } from "./Block";

export default function EmailRow() {
  const { user } = useAuth();
  const [sent, setSent] = useState(false);

  const resend = async () => {
    await resendVerification(user.email);
    setSent(true);
  };

  const help = (
    <p className="mt-0.5 text-[0.8125rem] text-muted-foreground">
      {user.emailVerified ? "Used to log in. It can't be changed." : "Check your inbox and click the link to verify it."}
    </p>
  );

  return (
    <Row title="Email" help={help}>
      {user.emailVerified ? (
        <span className="inline-flex items-center gap-1.5 rounded-full bg-success/10 px-2.5 py-0.5 text-xs font-medium text-success ring-1 ring-success/30 ring-inset">
          <Check className="size-3.25" /> Verified
        </span>
      ) : (
        <>
          <span className="rounded-full bg-warning/10 px-2.5 py-0.5 text-xs font-medium text-warning ring-1 ring-warning/30 ring-inset">
            Not verified
          </span>
          <Button variant="outline" onClick={resend} disabled={sent}>
            <Mail /> {sent ? "Link sent" : "Resend link"}
          </Button>
        </>
      )}
    </Row>
  );
}
