import { useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import * as client from "../../api/Client";
import AuthLayout, { Field } from "../../components/AuthLayout";
import { useAuth } from "../../context/AuthContext";

export default function Register() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  if (user) return <Navigate to="/" replace />;

  const handleSubmit = async () => {
    await client.register(name, email, password);
    await login(email, password);
    navigate("/", { replace: true });
  };

  return (
    <AuthLayout
      title="Create your account"
      subtitle="Start learning from your own code"
      submitLabel="Create account"
      onSubmit={handleSubmit}
      footer={<>Have an account? <Link to="/login" className="text-link hover:underline">Log in</Link></>}
    >
      <Field id="name" label="Name" minLength={2}
             value={name} onChange={(e) => setName(e.target.value)} />
      <Field id="email" label="Email" type="email" placeholder="you@example.com"
             value={email} onChange={(e) => setEmail(e.target.value)} />
      <Field id="password" label="Password" type="password" minLength={8} placeholder="At least 8 characters"
             value={password} onChange={(e) => setPassword(e.target.value)} />
    </AuthLayout>
  );
}
