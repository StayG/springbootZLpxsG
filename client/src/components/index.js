import { defineAsyncComponent } from 'vue'

//全局注册组件插件
export default {
  install(app) {
    const components = import.meta.glob('./*/index.vue')

    //遍历获取到的组件模块
    for (const [fullPath, fn] of Object.entries(components)) {
      const componentName = fullPath.replace('./', '').split('/')[0]

      app.component(componentName, defineAsyncComponent(fn))
    }
  }
}
