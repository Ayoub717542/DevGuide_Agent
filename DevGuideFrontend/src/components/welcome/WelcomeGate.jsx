import { useEffect, useState } from "react";
import { Navigate } from "react-router-dom";
import { getPreferences } from "../../api/user";
import { useAuth } from "../../context/AuthContext";
import { hasSeenWelcome, markWelcomeSeen } from "./welcomeSeen";

export default function WelcomeGate({ children }) {
  const { user } = useAuth();
  const [status, setStatus] = useState(() => (hasSeenWelcome(user.email) ? "ok" : "checking"));

  useEffect(() => {
    if (status !== "checking") return;
    let ignore = false;
    getPreferences()
      .then((preferences) => {
        if (ignore) return;
        if (preferences.experienceLevel) {
          markWelcomeSeen(user.email);
          setStatus("ok");
        } else {
          setStatus("welcome");
        }
      })
      .catch(() => !ignore && setStatus("ok"));
    return () => { ignore = true; };
  }, [status, user.email]);

  if (status === "checking") return <p className="center">Loading…</p>;
  if (status === "welcome") return <Navigate to="/welcome" replace />;
  return children;
}
