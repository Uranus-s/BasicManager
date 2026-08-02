const fs = require("fs");
const path = require("path");

const root = path.resolve(__dirname, "..");

const read = (file) => fs.readFileSync(path.join(root, file), "utf8");

const checks = [
  {
    label: "legacy /vab views directory was removed",
    pass: () => !fs.existsSync(path.join(root, "src", "views", "vab")),
  },
  {
    label: "legacy donate view directory was removed",
    pass: () => !fs.existsSync(path.join(root, "src", "views", "donate")),
  },
  {
    label: "legacy demo API modules were removed",
    pass: () =>
      ["table.js", "tree.js", "icon.js"].every(
        (file) => !fs.existsSync(path.join(root, "src", "api", file))
      ),
  },
  {
    label: "router no longer defines hard-coded /vab or donate navigation",
    pass: () => {
      const router = read(path.join("src", "router", "index.js"));
      return !/["']\/vab\b/.test(router) && !/donate/i.test(router);
    },
  },
  {
    label: "router keeps a hidden local home route",
    pass: () => {
      const router = read(path.join("src", "router", "index.js"));
      return (
        /path:\s*["']\/["']/.test(router) &&
        /redirect:\s*["']\/index["']/.test(router) &&
        /path:\s*["']index["']/.test(router) &&
        /views\/index\/index\.vue/.test(router)
      );
    },
  },
  {
    label: "dashboard quick entries do not link to removed /vab pages",
    pass: () => !/["']\/vab\b/.test(read(path.join("src", "views", "index", "index.vue"))),
  },
  {
    label: "default side menu open path does not point to /vab",
    pass: () => !/defaultOopeneds:\s*\[\s*["']\/vab["']\s*\]/.test(read(path.join("src", "config", "setting.config.js"))),
  },
  {
    label: "route mode no longer merges local asyncRoutes with backend routes",
    pass: () => /authentication:\s*["']all["']/.test(read(path.join("src", "config", "setting.config.js"))),
  },
];

const failures = checks.filter((check) => !check.pass());

if (failures.length) {
  console.error("Legacy navigation cleanup check failed:");
  failures.forEach((failure) => console.error(`- ${failure.label}`));
  process.exit(1);
}

console.log("Legacy navigation cleanup check passed.");
