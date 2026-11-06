import { useState } from "react";
import { Link, Navigate, useLocation, useNavigate } from "react-router-dom";
import AuthLayout, { Field } from "../../components/AuthLayout";
import { useAuth } from "../../context/AuthContext";

export default function Login() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  if (user) return <Navigate to="/" replace />;

  const handleSubmit = async () => {
    await login(email, password);
    navigate("/", { replace: true });
  };

  return (
    <AuthLayout
      title="Welcome back"
      subtitle="Log in to your study desk"
      submitLabel="Log in"
      onSubmit={handleSubmit}
      initialError={location.state?.error}
      footer={<>No account? <Link to="/register" className="text-link hover:underline">Create one</Link></>}
    >
      <Field id="email" label="Email" type="email" placeholder="you@example.com"
             value={email} onChange={(e) => setEmail(e.target.value)} />
      <Field id="password" label="Password" type="password"
             value={password} onChange={(e) => setPassword(e.target.value)} />
    </AuthLayout>
  );
}
