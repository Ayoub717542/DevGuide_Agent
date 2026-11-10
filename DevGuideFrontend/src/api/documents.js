import axiosApi from "./axiosApi";

export const uploadPdf = (conversationId, file) => {
  const form = new FormData();
  form.append("conversationId", conversationId);
  form.append("file", file);
  return axiosApi.post("/api/documents/upload", form).then((res) => res.data);
};

export const getChatPdfs = (conversationId) =>
  axiosApi.get("/api/documents/uploads", { params: { conversationId } }).then((res) => res.data);

export const getAllPdfs = () =>
  axiosApi.get("/api/documents/all").then((res) => res.data);

export const deletePdf = (documentId) =>
  axiosApi.delete(`/api/documents/uploads/${documentId}`);

export const getChatFiles = (conversationId) =>
  axiosApi.get("/api/documents/generated", { params: { conversationId } }).then((res) => res.data);

export const downloadFile = async (file) => {
  const res = await axiosApi.get(file.downloadPath, { responseType: "blob" });
  const url = URL.createObjectURL(res.data);
  const link = document.createElement("a");
  link.href = url;
  link.download = `${file.title}.${res.data.type === "application/pdf" ? "pdf" : "docx"}`;
  link.click();
  URL.revokeObjectURL(url);
};
