import { useState } from "react";
import { Link } from "react-router-dom";
import * as client from "../../api/Client";
import AuthLayout, { Field } from "../../components/AuthLayout";

export default function ForgotPassword() {
  const [email, setEmail] = useState("");
  const [sent, setSent] = useState(false);

  const handleSubmit = async () => {
    setSent(false);
    await client.forgotPassword(email);
    setSent(true);
  };

  return (
    <AuthLayout
      title="Forgot your password?"
      subtitle="We will email you a link to choose a new one"
      submitLabel="Send reset link"
      onSubmit={handleSubmit}
      social={false}
      notice={sent ? "If this email has an account, we sent a reset link. Check your inbox." : ""}
      footer={<>Remembered it? <Link to="/login" className="text-link hover:underline">Log in</Link></>}
    >
      <Field id="email" label="Email" type="email" placeholder="you@example.com"
             value={email} onChange={(e) => setEmail(e.target.value)} />
    </AuthLayout>
  );
}
