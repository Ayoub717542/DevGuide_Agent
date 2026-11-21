import axiosApi from "./axiosApi";

const USERS = "/api/users";

export const getUsers = () => axiosApi.get(USERS).then((res) => res.data);

export const updateRole = (id, role) => axiosApi.put(`${USERS}/${id}`, { role }).then((res) => res.data);

export const deleteUser = (id) => axiosApi.delete(`${USERS}/${id}`);
