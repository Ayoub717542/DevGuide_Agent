import axiosApi from "./axiosApi";

export const login = (email, password) =>
  axiosApi.post("/api/auth/login", { email, password }).then((res) => res.data);

export const register = (name, email, password) =>
  axiosApi.post("/api/auth/register", { name, email, password }).then((res) => res.data);

export const getMe = () =>
  axiosApi.get("/api/users/me").then((res) => res.data);
