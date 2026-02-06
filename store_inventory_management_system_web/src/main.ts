import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'
import VXETable from 'vxe-table'
import 'vxe-table/lib/style.css'
import './styles/reset.css'
import './styles/index.css'
import router from './router'
import App from './App.vue'
import registerDirectives from './directives/index'

const app = createApp(App)
app.use(ElementPlus, { locale: zhCn })
app.use(VXETable)
app.use(router)
registerDirectives(app)
app.mount('#app')
