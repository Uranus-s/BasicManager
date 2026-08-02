#!/usr/bin/env node

// 禁用弃用警告
process.noDeprecation = true;

const { rspack } = require("@rspack/core");
const path = require("path");

const configPath = path.resolve(__dirname, "rspack.config.js");
const config = require(configPath);
const mode = process.argv[2] === "build" ? "production" : "development";

// 增强环境变量设置，确保所有编译阶段都使用相同的NODE_ENV值
process.env.NODE_ENV = mode;
// 设置webpack特定的环境变量，以避免冲突
process.env.WEBPACK_ENV = mode;
process.env.BABEL_ENV = mode;
console.log("设置环境变量 NODE_ENV =", process.env.NODE_ENV);

// 读取配置
config.mode = mode;

if (mode === "production") {
  // 生产环境配置
  console.log("正在极速打包中，预计用时3秒，请稍后...");

  // 生产环境下增加额外配置
  config.optimization = {
    ...config.optimization,
    moduleIds: "deterministic", // 稳定的模块ID以优化长期缓存
    chunkIds: "deterministic", // 稳定的chunk ID以优化长期缓存
    removeEmptyChunks: true, // 移除空的chunks
  };

  rspack(config).run((err, stats) => {
    if (err) {
      console.error(err.stack || err);
      if (err.details) {
        console.error(err.details);
      }
      process.exit(1);
      return;
    }

    const info = stats.toJson();

    if (stats.hasErrors()) {
      console.error(info.errors);
    }

    if (stats.hasWarnings()) {
      console.warn(info.warnings);
    }

    console.log(
      stats.toString({
        colors: true,
        modules: false,
        children: false,
        chunks: false,
        chunkModules: false,
      })
    );
  });
} else {
  // 开发环境配置
  try {
    // 尝试使用rspack的dev server
    const { RspackDevServer } = require("@rspack/dev-server");

    const compiler = rspack(config);

    // 使用rspack.config.js中的所有devServer配置
    const devServerOptions = config.devServer || {};

    const server = new RspackDevServer(devServerOptions, compiler);

    server.start().catch((err) => {
      console.error("启动RspackDevServer失败:", err);
    });
  } catch (error) {
    console.error("加载@rspack/dev-server失败:", error);
    process.exit(1);
  }
}
