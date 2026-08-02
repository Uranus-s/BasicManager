const fs = require("fs");
const path = require("path");

const root = path.resolve(__dirname, "..");
const read = (file) => fs.readFileSync(path.join(root, file), "utf8");

const router = read(path.join("src", "router", "index.js"));
const avatar = read(
  path.join("src", "layouts", "components", "VabAvatar", "index.vue")
);
const pagePath = path.join(root, "src", "views", "personalCenter", "index.vue");

const checks = [
  {
    label: "personal center page exists",
    pass: fs.existsSync(pagePath),
  },
  {
    label: "router exposes /personal-center as a fixed route",
    pass:
      /path:\s*["']\/personal-center["']/.test(router) &&
      /name:\s*["']PersonalCenter["']/.test(router),
  },
  {
    label: "avatar dropdown opens /personal-center",
    pass: /router\.push\(["']\/personal-center["']\)/.test(avatar),
  },
  {
    label: "legacy personal center path is not used",
    pass: !/\/personalCenter\/personalCenter/.test(avatar + router),
  },
];

const failures = checks.filter((check) => !check.pass);

if (failures.length) {
  console.error("Personal center check failed:");
  failures.forEach((failure) => console.error(`- ${failure.label}`));
  process.exit(1);
}

console.log("Personal center check passed.");
