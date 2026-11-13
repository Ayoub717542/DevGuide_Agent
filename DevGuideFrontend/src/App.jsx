import { Navigate, Route, Routes } from "react-router-dom";
import ProtectedRoute from "./components/ProtectedRoute";
import Login from "./pages/auth/Login";
import OAuthCallback from "./pages/auth/OAuthCallback";
import ForgotPassword from "./pages/auth/ForgotPassword";
import Register from "./pages/auth/Register";
import ResetPassword from "./pages/auth/ResetPassword";
import StudyDesk from "./pages/StudyDesk";

function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/forgot-password" element={<ForgotPassword />} />
      <Route path="/reset-password" element={<ResetPassword />} />
      <Route path="/oauth/callback" element={<OAuthCallback />} />
      <Route path="/" element={<ProtectedRoute><StudyDesk /></ProtectedRoute>} />
      <Route path="/settings" element={<ProtectedRoute><StudyDesk /></ProtectedRoute>} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default App
