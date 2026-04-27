import { reactive } from "vue";

window.MIN_FRAME_INFOMATIONS = reactive({
  current_app_code: ""
});

export function setFrameInfo(key, value) {
  window.MIN_FRAME_INFOMATIONS[key] = value;
}
