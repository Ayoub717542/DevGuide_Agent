import axiosApi from "./axiosApi";

export const getPendingActions = () =>
  axiosApi.get("/api/trello/pending").then((res) => res.data);

export const approveAction = (id) =>
  axiosApi.post(`/api/trello/pending/${id}/approve`).then((res) => res.data);

export const rejectAction = (id) =>
  axiosApi.delete(`/api/trello/pending/${id}`);

export const getConnectUrl = () =>
  axiosApi.get("/api/trello/connect").then((res) => res.data);

export const finishConnect = (code, state) =>
  axiosApi.post("/api/trello/finish", { code, state }).then((res) => res.data);

export const disconnectTrello = () => axiosApi.delete("/api/trello");
