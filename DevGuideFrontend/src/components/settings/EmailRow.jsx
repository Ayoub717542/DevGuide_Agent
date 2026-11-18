import { Mail } from "lucide-react";
import { useState } from "react";
import { resendVerification } from "../../api/Client";
import { useAuth } from "../../context/AuthContext";
import { Button } from "../ui/button";
import { Row, Status } from "./Block";

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
        <Status tone="success">Verified</Status>
      ) : (
        <>
          <Status tone="warning">Not verified</Status>
          <Button variant="outline" onClick={resend} disabled={sent}>
            <Mail /> {sent ? "Link sent" : "Resend link"}
          </Button>
        </>
      )}
    </Row>
  );
}
