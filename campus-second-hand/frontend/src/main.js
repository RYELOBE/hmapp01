import { createApp } from 'vue'
import ArcoVue from '@arco-design/web-vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import ArcoVueIcon from '@arco-design/web-vue/dist/arco-vue-icon.js'
import '@arco-design/web-vue/dist/arco.css'
import './assets/styles/global.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(ArcoVue)
app.use(ArcoVueIcon)

app.mount('#app')
