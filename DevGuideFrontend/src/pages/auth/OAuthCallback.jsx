import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { useAuth } from "../../context/AuthContext";

export default function OAuthCallback() {
  const { restoreSession } = useAuth();
  const navigate = useNavigate();
  const [hash] = useState(() => window.location.hash.slice(1));
  const [connectingGithub] = useState(() => sessionStorage.getItem("connectingGithub") === "1");

  useEffect(() => {
    const params = new URLSearchParams(hash);
    const messages = {
      verify_email_first: "Please verify your email first. We sent you a verification link. Click it, then log in again.",
      no_email: "Your account has no verified email. Verify an email with your provider, then try again.",
    };
    const error = messages[params.get("error")] || "Social login failed. Try again.";
    sessionStorage.removeItem("connectingGithub");

    const fail = () => {
      if (connectingGithub) {
        toast.error("GitHub connection failed", { id: "github-connect", description: error });
        return navigate("/settings#apps", { replace: true });
      }
      navigate("/login", { replace: true, state: { error } });
    };

    if (params.get("error")) return fail();
    restoreSession()
      .then(() => {
        if (!connectingGithub) return navigate("/", { replace: true });
        toast.success("GitHub connected", { id: "github-connect" });
        navigate("/settings#apps", { replace: true });
      })
      .catch(fail);
  }, [hash, connectingGithub, restoreSession, navigate]);

  return <p className="center">Logging you in…</p>;
}
