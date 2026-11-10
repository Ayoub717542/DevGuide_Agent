import { API_URL, TOKEN_KEY, goToLogin } from "./axiosApi";

export async function streamReply(question, handlers, signal) {
  const form = new FormData();
  form.append("conversationId", question.conversationId);
  form.append("content", question.content);
  form.append("hintMode", question.hintMode ?? false);
  form.append("devMode", question.devMode ?? false);
  if (question.file) form.append("file", question.file);

  const response = await fetch(`${API_URL}/api/chat/stream`, {
    method: "POST",
    headers: { Authorization: `Bearer ${localStorage.getItem(TOKEN_KEY)}` },
    body: form,
    signal,
  });

  if (response.status === 401) {
    goToLogin();
    return;
  }
  if (!response.ok) {
    const error = await response.json().catch(() => null);
    handlers.onError(error?.message || "Something went wrong. Please try again.");
    return;
  }

  const reader = response.body.pipeThrough(new TextDecoderStream()).getReader();
  let buffer = "";
  while (true) {
    const { value, done } = await reader.read();
    if (done) break;
    buffer += value;
    const events = buffer.split("\n\n");
    buffer = events.pop();
    events.forEach((text) => handleEvent(text, handlers));
  }
}

function handleEvent(text, handlers) {
  let name = "message";
  let data = "";
  for (const line of text.split("\n")) {
    if (line.startsWith("event:")) name = line.slice(6).trim();
    if (line.startsWith("data:")) data += line.slice(5);
  }
  if (!data) return;

  const json = JSON.parse(data);
  if (name === "delta") handlers.onDelta(json.text);
  if (name === "done") handlers.onDone(json);
  if (name === "error") handlers.onError(json.message);
}
