const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const vm = require("node:vm");
const babel = require("@babel/core");

const projectRoot = path.resolve(__dirname, "..");

function read(relativePath) {
  return fs.readFileSync(path.join(projectRoot, relativePath), "utf8");
}

function loadAiChatUtils() {
  const source = read("src/utils/aiChat.js");
  const { code } = babel.transformSync(source, {
    filename: "src/utils/aiChat.js",
    presets: [
      ["@babel/preset-env", { targets: { node: "current" }, modules: "commonjs" }],
    ],
  });
  const module = { exports: {} };

  vm.runInNewContext(code, { module, exports: module.exports, require }, {
    filename: "src/utils/aiChat.js",
  });
  return module.exports;
}

const { dockBottomRightRect } = loadAiChatUtils();

assert.equal(typeof dockBottomRightRect, "function", "应提供右下角停靠函数");
assert.deepEqual(
  JSON.parse(JSON.stringify(dockBottomRightRect(
    { x: 0, y: 0, width: 420, height: 600 },
    { width: 1440, height: 900 }
  ))),
  { x: 1004, y: 284, width: 420, height: 600 }
);
assert.deepEqual(
  JSON.parse(JSON.stringify(dockBottomRightRect(
    { x: 240, y: 180, width: 500, height: 550 },
    { width: 1440, height: 900 }
  ))),
  { x: 924, y: 334, width: 500, height: 550 }
);
assert.deepEqual(
  JSON.parse(JSON.stringify(dockBottomRightRect(
    { x: 240, y: 180, width: 500, height: 550 },
    { width: 400, height: 500 }
  ))),
  { x: 16, y: 16, width: 368, height: 468 }
);

const component = read("src/components/AiChatFloat/index.vue");

assert.match(component, /dockBottomRightRect/);
assert.match(component, /const rect = ref\(dockBottomRightRect\(/);
assert.match(component, /const openChat = async \(\) => \{\s*rect\.value = dockBottomRightRect\(/);

console.log("AI 聊天浮窗右下角定位检查通过");
