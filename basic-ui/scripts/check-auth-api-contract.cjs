const fs = require("fs");
const path = require("path");

const root = path.resolve(__dirname, "..");
const userApi = fs.readFileSync(path.join(root, "src", "api", "user.js"), "utf8");

const checks = [
  {
    label: "getUserInfo uses the documented /auth/info endpoint",
    pass: /url:\s*["']\/auth\/info["']/.test(userApi),
  },
  {
    label: "getUserInfo uses GET as documented",
    pass: /method:\s*["']get["']/.test(userApi),
  },
  {
    label: "legacy /userInfo endpoint is not used",
    pass: !/["']\/userInfo["']/.test(userApi),
  },
];

const failures = checks.filter((check) => !check.pass);

if (failures.length) {
  console.error("Auth API contract check failed:");
  failures.forEach((failure) => console.error(`- ${failure.label}`));
  process.exit(1);
}

console.log("Auth API contract check passed.");
