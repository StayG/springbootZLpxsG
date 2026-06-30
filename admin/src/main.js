import { createApp } from "vue";
//导入重置样式
import "@/styles/reset.css";
//导入自定义样式
import './styles/element/index.scss'
import './styles/index.scss'
// 导入elementplus
import ElementPlus from "element-plus";
import zhCn from "element-plus/es/locale/lang/zh-cn";
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
//导入路由
import router from "./router/router";
//导入pinia
import { createPinia } from "pinia"; 
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
//导入全局注册插件
import globalInstall from '@/components/index'
import App from "./App.vue";

const app = createApp(App);
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(ElementPlus, {
  locale: zhCn,
});
app.use(router);
app.use(pinia);
app.use(globalInstall);

// 初始化主题配置
import { useThemeStore } from "@/stores/theme";
app.mount("#app");

// 应用主题（需要在mount之后）
const themeStore = useThemeStore();
themeStore.applyTheme();
