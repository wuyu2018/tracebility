import { createApp } from 'vue'
import ToolsPage from './views/Admin.vue'
import './styles/main.css'
import router from './router'

import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import ElementPlus from 'element-plus'

import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const app = createApp(ToolsPage)

app.use(ElementPlus, {
  locale: zhCn,
  size: 'default'
})
.use(router)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
