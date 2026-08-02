import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import store from "@/store"; // 导入Vuex store
import plugins from "./plugins";
// 导入布局组件注册函数
import { registerLayoutComponents } from "@/layouts/export";
// 导入事件总线
import eventBus from "@/utils/eventBus";

/**
 * @author https://github.com/zxwk1998/vue-admin-better （不想保留author可删除）
 */

async function bootstrap() {
  // 登录页需要在首次渲染前取得公开系统名称，接口不可用时 action 会自动回退。
  await store.dispatch("settings/loadPublicSettings");

  const app = createApp(App);

  app.use(store);
  app.use(router);

  plugins(app);

  registerLayoutComponents(app);

  app.config.globalProperties.$eventBus = eventBus;
  window.$eventBus = eventBus;

  app.mount("#vue-admin-better");
}

bootstrap();
