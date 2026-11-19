import axios from "axios";

export const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8081";

let accessToken = null;
let refreshing = null;

export const getAccessToken = () => accessToken;

export const setAccessToken = (token) => {
  accessToken = token;
};

export const refreshAccessToken = () => {
  if (!refreshing) {
    refreshing = axios
      .post(`${API_URL}/api/auth/refresh`, null, { withCredentials: true })
      .then((res) => {
        accessToken = res.data.accessToken;
        return accessToken;
      })
      .finally(() => {
        refreshing = null;
      });
  }
  return refreshing;
};

const axiosApi = axios.create({
  baseURL: API_URL,
  withCredentials: true,
});

// Add the JWT to every request.
axiosApi.interceptors.request.use((config) => {
  if (accessToken && !config.headers.Authorization) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  return config;
});

export const goToLogin = () => {
  accessToken = null;
  window.location.href = "/login";
};

axiosApi.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config;
    const isAuthCall = original?.url?.startsWith("/api/auth/");
    if (error.response?.status !== 401 || isAuthCall || original._retried) {
      return Promise.reject(error);
    }
    original._retried = true;
    try {
      const token = await refreshAccessToken();
      original.headers.Authorization = `Bearer ${token}`;
      return axiosApi(original);
    } catch {
      goToLogin();
      return Promise.reject(error);
    }
  }
);

export default axiosApi;
