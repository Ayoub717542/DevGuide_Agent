import { Navigate, Route, Routes } from "react-router-dom";
import ProtectedRoute from "./components/ProtectedRoute";
import { useAuth } from "./context/AuthContext";
import Login from "./pages/auth/Login";
import OAuthCallback from "./pages/auth/OAuthCallback";
import Register from "./pages/auth/Register";

// Placeholder until the Study Desk is built.
function Home() {
  const { user, logout } = useAuth();
  return (
    <div className="center">
      <div className="auth-card">
        <h1>Hello, {user.name}</h1>
        <p className="muted">{user.email} · {user.role}</p>
        <button onClick={logout}>Log out</button>
      </div>
    </div>
  );
}

function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/oauth/callback" element={<OAuthCallback />} />
      <Route path="/" element={<ProtectedRoute><Home /></ProtectedRoute>} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default App
