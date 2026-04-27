class MessageManager {
  activeListenerMap = {};

  sendMessage = (activeName, message) => {
    const listeners = this.activeListenerMap[activeName] || [];
    listeners.forEach((listener) => listener(message));
  };

  sendMessageToAll = (message) => {
    Object.keys(this.activeListenerMap).forEach((activeName) => {
      const listeners = this.activeListenerMap[activeName] || [];
      listeners.forEach((listener) => listener(message));
    });
  };

  addEventListener = (activeName, listener) => {
    if (!this.activeListenerMap[activeName]) {
      this.activeListenerMap[activeName] = [];
    }
    this.activeListenerMap[activeName].push(listener);
  };

  removeAllListeners = (activeName) => {
    delete this.activeListenerMap[activeName];
  };
}

export default new MessageManager();
