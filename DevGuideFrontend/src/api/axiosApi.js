import axios from "axios";

export const TOKEN_KEY = "token";
export const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8081";

const axiosApi = axios.create({
  baseURL: API_URL,
});

// Add the JWT to every request.
axiosApi.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token && !config.headers.Authorization) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const goToLogin = () => {
  localStorage.removeItem(TOKEN_KEY);
  window.location.href = "/login";
};

// Token expired or invalid: log out and go to the login page.
// Auth calls are skipped so a wrong password just shows an error.
axiosApi.interceptors.response.use(
  (response) => response,
  (error) => {
    const isAuthCall = error.config?.url?.startsWith("/api/auth/");
    if (error.response?.status === 401 && !isAuthCall) {
      goToLogin();
    }
    return Promise.reject(error);
  }
);

export default axiosApi;
