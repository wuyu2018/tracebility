import { createApp } from 'vue'
import ToolsPage from './views/ToolsStandalone.vue'
import './assets/css/main.css'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const app = createApp(ToolsPage)

app.use(ElementPlus, {
  locale: zhCn,
  size: 'default'
})

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#tools-app')