import { createContext, useCallback, useContext, useEffect, useState } from "react";
import { TOKEN_KEY } from "../api/axiosApi";
import * as client from "../api/Client";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(() => !!localStorage.getItem(TOKEN_KEY));

  useEffect(() => {
    if (!localStorage.getItem(TOKEN_KEY)) return;
    client.getMe()
      .then(setUser)
      .catch(() => localStorage.removeItem(TOKEN_KEY))
      .finally(() => setLoading(false));
  }, []);

  const loginWithToken = useCallback(async (token) => {
    localStorage.setItem(TOKEN_KEY, token);
    setUser(await client.getMe());
  }, []);

  const login = async (email, password) => {
    const { token } = await client.login(email, password);
    await loginWithToken(token);
  };

  const logout = () => {
    localStorage.removeItem(TOKEN_KEY);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, loginWithToken, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = () => useContext(AuthContext);
