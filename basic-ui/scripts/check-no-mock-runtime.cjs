const fs = require("fs");
const path = require("path");

const root = path.resolve(__dirname, "..");

const checks = [
  {
    label: "mock directory was removed",
    pass: () => !fs.existsSync(path.join(root, "mock")),
  },
  {
    label: "src/utils/static.js was removed",
    pass: () => !fs.existsSync(path.join(root, "src", "utils", "static.js")),
  },
  {
    label: "package.json no longer depends on mockjs",
    pass: () => {
      const pkg = JSON.parse(
        fs.readFileSync(path.join(root, "package.json"), "utf8")
      );
      return !(
        (pkg.dependencies && pkg.dependencies.mockjs) ||
        (pkg.devDependencies && pkg.devDependencies.mockjs)
      );
    },
  },
  {
    label: "runtime files no longer reference mock plumbing",
    pass: () => {
      const files = [
        "rspack.config.js",
        "rspack.js",
        path.join("src", "main.js"),
        path.join("src", "utils", "request.js"),
        path.join("src", "config", "net.config.js"),
      ];
      const forbidden = [
        /VUE_APP_MOCK_ENABLE/,
        /vab-mock-server/,
        /mockXHR/,
        /mockjs/,
        /require\.context\([^)]*mock/,
        /require\(["']\.\/mock/,
        /from ["']mockjs["']/,
      ];

      return files.every((file) => {
        const content = fs.readFileSync(path.join(root, file), "utf8");
        return forbidden.every((pattern) => !pattern.test(content));
      });
    },
  },
];

const failures = checks.filter((check) => !check.pass());

if (failures.length) {
  console.error("Mock runtime cleanup check failed:");
  failures.forEach((failure) => console.error(`- ${failure.label}`));
  process.exit(1);
}

console.log("Mock runtime cleanup check passed.");
