import axiosApi from "./axiosApi";

const BASE = "/api/chat/conversations";

export const getChats = () =>
  axiosApi.get(BASE).then((res) => res.data);

export const createChat = (title) =>
  axiosApi.post(BASE, { title }).then((res) => res.data);

export const renameChat = (id, title) =>
  axiosApi.patch(`${BASE}/${id}`, { title }).then((res) => res.data);

export const deleteChat = (id) =>
  axiosApi.delete(`${BASE}/${id}`);

export const getMessages = (id) =>
  axiosApi.get(`${BASE}/${id}/messages`).then((res) => res.data);

export const createQuiz = (id) =>
  axiosApi.post(`${BASE}/${id}/quiz`).then((res) => res.data);

export const saveQuizResult = (id, topic, score, total) =>
  axiosApi.post(`${BASE}/${id}/quiz/result`, { topic, score, total });

export const createChallenge = (id) =>
  axiosApi.post(`${BASE}/${id}/challenge`).then((res) => res.data);

export const submitChallenge = (challengeId, code) =>
  axiosApi.post(`/api/challenges/${challengeId}/submit`, { code }).then((res) => res.data);
