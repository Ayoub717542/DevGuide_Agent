import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";

export default function OAuthCallback() {
  const { loginWithToken } = useAuth();
  const navigate = useNavigate();
  const [hash] = useState(() => window.location.hash.slice(1));

  useEffect(() => {
    const params = new URLSearchParams(hash);
    const token = params.get("token");
    const messages = {
      verify_email_first: "Please verify your email first. We sent you a verification link. Click it, then log in again.",
      no_email: "Your account has no verified email. Verify an email with your provider, then try again.",
    };
    const error = messages[params.get("error")] || "Social login failed. Try again.";
    const fail = () => navigate("/login", { replace: true, state: { error } });

    if (!token) return fail();
    loginWithToken(token)
      .then(() => navigate("/", { replace: true }))
      .catch(fail);
  }, [hash, loginWithToken, navigate]);

  return <p className="center">Logging you in…</p>;
}
