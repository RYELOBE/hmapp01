import { http } from "./http.js";

export async function fetchAiPresets() {
  return http.get("/ai/presets");
}

export async function fetchRecentSessions() {
  return http.get("/ai/sessions/recent");
}

export async function fetchSessionMessages(sessionId) {
  return http.get(`/ai/sessions/${sessionId}/messages`);
}

export async function chatWithAssistant(message) {
  return http.post("/ai/chat", message);
}
