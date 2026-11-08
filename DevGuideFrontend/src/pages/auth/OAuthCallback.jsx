import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";

export default function OAuthCallback() {
  const { loginWithToken } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    const token = new URLSearchParams(window.location.hash.slice(1)).get("token");
    const fail = () => navigate("/login", { replace: true, state: { error: "Social login failed. Try again." } });

    if (!token) return fail();
    loginWithToken(token)
      .then(() => navigate("/", { replace: true }))
      .catch(fail);
  }, [loginWithToken, navigate]);

  return <p className="center">Logging you in…</p>;
}
