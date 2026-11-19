import { useState } from "react";
import { useNavigate } from "react-router-dom";
import * as client from "../../api/Client";
import AuthLayout, { Field } from "../../components/AuthLayout";

export default function ResetPassword() {
  const navigate = useNavigate();
  const [token] = useState(() => new URLSearchParams(window.location.hash.slice(1)).get("token"));
  const [password, setPassword] = useState("");

  const handleSubmit = async () => {
    await client.resetPassword(token, password);
    navigate("/login", {
      replace: true,
      state: { notice: "Password changed. Log in with your new password." },
    });
  };

  return (
    <AuthLayout
      title="Choose a new password"
      subtitle="This link works once and for 1 hour"
      submitLabel="Change password"
      onSubmit={handleSubmit}
      social={false}
      initialError={token ? "" : "This link is invalid or expired. Ask for a new one."}
      topLink={{ text: "Need a new link?", label: "Ask again", to: "/forgot-password" }}
    >
      <Field id="password" label="New password" type="password" minLength={8} placeholder="At least 8 characters"
             value={password} onChange={(e) => setPassword(e.target.value)} />
    </AuthLayout>
  );
}
