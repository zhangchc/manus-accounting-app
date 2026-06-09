import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router, { addMenuRoutes } from './router'
import { createPinia } from 'pinia'
import permission from './directives/permission'
import './styles/global.css'

// F5 刷新时同步恢复动态路由，防止白屏
const savedMenus = localStorage.getItem('admin_menus')
if (savedMenus) {
  try {
    addMenuRoutes(JSON.parse(savedMenus))
  } catch (e) { /* ignore */ }
}

const app = createApp(App)
app.use(ElementPlus)
app.use(router)
app.use(createPinia())

app.directive('permission', permission)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
