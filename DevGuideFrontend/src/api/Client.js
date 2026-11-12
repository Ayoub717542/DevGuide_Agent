import axiosApi from "./axiosApi";

export const login = (email, password) =>
  axiosApi.post("/api/auth/login", { email, password }).then((res) => res.data);

export const register = (name, email, password) =>
  axiosApi.post("/api/auth/register", { name, email, password }).then((res) => res.data);

export const resendVerification = (email) =>
  axiosApi.post("/api/auth/resend-verification", { email }).then((res) => res.data);

export const getMe = (token) =>
  axiosApi
    .get("/api/users/me", token ? { headers: { Authorization: `Bearer ${token}` } } : undefined)
    .then((res) => res.data);
