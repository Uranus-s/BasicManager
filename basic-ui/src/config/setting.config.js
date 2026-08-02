/**
 * @description 导出默认通用配置
 */
const setting = {
  // 生产环境构建文件的目录名
  outputDir: "dist",
  // 放置生成的静态资源 (js、css、img、fonts) 的 (相对于 outputDir 的) 目录。
  assetsDir: "static",
  //标题 （包括初次加载雪花屏的标题 页面的标题 浏览器的标题）
  title: "BasicManage",
  //开发环境端口号
  devPort: "8091",
  //copyright
  copyright: "zxwk1998",
  //是否显示页面底部自定义版权信息
  footerCopyright: true,
  //是否显示顶部进度条
  progressBar: true,
  //缓存路由的最大数量
  keepAliveMaxNum: 99,
  //不经过token校验的路由
  routesWhiteList: ["/login", "/register", "/forgot-password", "/404", "/401"],
  //加载时显示文字
  loadingText: "正在加载中...",
  //token名称
  tokenName: "accessToken",
  //token在localStorage、sessionStorage存储的key的名称
  tokenTableName: "basic-manage-token",
  //token存储位置localStorage sessionStorage
  storage: "localStorage",
  //token失效回退到登录页时是否记录本次的路由
  recordRoute: true,
  //是否显示logo，不显示时设置false，显示时请填写remixIcon图标名称，暂时只支持设置remixIcon
  logo: "vuejs-fill",
  //是否开启登录拦截
  loginInterception: true,
  //业务导航使用后端/auth/routes
  authentication: "all",
  //vertical布局时默认展开的菜单path，使用逗号隔开建议只展开一个
  defaultOopeneds: [],
  //需要加loading层的请求，防止重复提交
  debounce: ["doEdit"],
};
module.exports = setting;
