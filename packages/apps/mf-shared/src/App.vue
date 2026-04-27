<template>
  <main class="shared-home">
    <h1>MF Shared Running</h1>
    <p>This app exposes shared modules for shell, portal and ops.</p>
    <div class="modules">
      <h2>Available Modules:</h2>
      <ul>
        <li><strong>auth-sdk</strong> - Authentication SDK (login, logout, token management)</li>
        <li><strong>roles</strong> - Role definitions and utilities</li>
        <li><strong>http</strong> - HTTP request client with interceptors</li>
        <li><strong>theme</strong> - Theme configuration</li>
        <li><strong>ai-sdk</strong> - AI assistant API</li>
        <li><strong>Login</strong> - Login component</li>
        <li><strong>StatusTag</strong> - Status tag component</li>
      </ul>
    </div>
  </main>
</template>

<style scoped>
.shared-home {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: #f4f9ff;
  color: #1b2a41;
  font-family: "Segoe UI", "PingFang SC", sans-serif;
}

h1 {
  margin: 0 0 20px 0;
}

.modules {
  background: white;
  padding: 20px 40px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  max-width: 600px;
}

.modules h2 {
  margin: 0 0 15px 0;
  color: #336ad8;
}

.modules ul {
  text-align: left;
  line-height: 1.8;
}

.modules li strong {
  color: #336ad8;
}
</style>
