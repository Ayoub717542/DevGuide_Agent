import { createContext, useCallback, useContext, useEffect, useState } from "react";
import { refreshAccessToken, setAccessToken } from "../api/axiosApi";
import * as client from "../api/Client";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const restoreSession = useCallback(async () => {
    await refreshAccessToken();
    setUser(await client.getMe());
  }, []);

  useEffect(() => {
    refreshAccessToken()
      .then(() => client.getMe())
      .then(setUser)
      .catch(() => setAccessToken(null))
      .finally(() => setLoading(false));
  }, []);

  const login = async (email, password) => {
    const { accessToken } = await client.login(email, password);
    setAccessToken(accessToken);
    setUser(await client.getMe());
  };

  const logout = async () => {
    await client.logout().catch(() => {});
    setAccessToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, setUser, loading, login, restoreSession, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = () => useContext(AuthContext);
