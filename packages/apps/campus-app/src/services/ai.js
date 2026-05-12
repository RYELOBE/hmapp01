import http from "./core/http";

export async function fetchAiPresets(optionConfig = {}) {
  return http.get("/ai/presets", {}, optionConfig);
}

export async function fetchRecentSessions(optionConfig = {}) {
  return http.get("/ai/sessions/recent", {}, optionConfig);
}

export async function fetchSessionMessages(sessionId, optionConfig = {}) {
  return http.get(`/ai/sessions/${sessionId}/messages`, {}, optionConfig);
}

export async function chatWithAssistant(message, optionConfig = {}) {
  return http.post("/ai/chat", message, {}, optionConfig);
}

export async function createNewSession(optionConfig = {}) {
  return http.post("/ai/sessions", {}, {}, optionConfig);
}

export async function deleteSession(sessionId, optionConfig = {}) {
  return http.delete(`/ai/sessions/${sessionId}`, {}, optionConfig);
}