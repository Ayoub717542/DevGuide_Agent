import axiosApi from "./axiosApi";

const ME = "/api/users/me";

export const updateName = (name) => axiosApi.put(ME, { name }).then((res) => res.data);

export const changePassword = (currentPassword, newPassword) =>
  axiosApi.put(`${ME}/password`, { currentPassword, newPassword });

export const deleteAccount = () => axiosApi.delete(ME);

export const disconnectGithub = () => axiosApi.delete(`${ME}/github`);

export const getPreferences = () => axiosApi.get(`${ME}/preferences`).then((res) => res.data);

export const updatePreferences = (preferences) =>
  axiosApi.put(`${ME}/preferences`, preferences).then((res) => res.data);
