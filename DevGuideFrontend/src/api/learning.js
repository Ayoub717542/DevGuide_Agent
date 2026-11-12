import axiosApi from "./axiosApi";

export const getReviews = () =>
  axiosApi.get("/api/users/me/reviews").then((res) => res.data);

export const getProgress = () =>
  axiosApi.get("/api/users/me/progress").then((res) => res.data);

export const getNotes = () =>
  axiosApi.get("/api/users/me/notes").then((res) => res.data);

export const forgetNote = (id) =>
  axiosApi.delete(`/api/users/me/notes/${id}`);
