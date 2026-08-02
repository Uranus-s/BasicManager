const fs = require("fs");
const path = require("path");

const root = path.resolve(__dirname, "..");
const read = (file) => fs.readFileSync(path.join(root, file), "utf8");

const router = read(path.join("src", "router", "index.js"));
const guard = read(path.join("src", "config", "permission.js"));
const userStore = read(path.join("src", "store", "modules", "user.js"));
const routesStore = read(path.join("src", "store", "modules", "routes.js"));

const checks = [
  {
    label: "router exposes a tracked dynamic route registration helper",
    pass:
      /export function addDynamicRoute/.test(router) &&
      /dynamicRouteRemovers/.test(router) &&
      /router\.addRoute\(route\)/.test(router),
  },
  {
    label: "resetRouter removes tracked dynamic routes instead of fixed routes",
    pass:
      /dynamicRouteRemovers\.forEach/.test(router) &&
      /dynamicRouteRemovers\.clear\(\)/.test(router) &&
      !/name\s*!==\s*["']Login["']/.test(router),
  },
  {
    label: "navigation guard uses tracked dynamic route registration",
    pass:
      /addDynamicRoute/.test(guard) &&
      !/router\.addRoute\(item\)/.test(guard),
  },
  {
    label: "access-token reset clears router instance dynamic routes",
    pass: /resetRouter\(\)/.test(userStore),
  },
  {
    label: "auth route fetch failure does not mark routes as loaded",
    pass:
      /catch\s*\([^)]*\)\s*{[\s\S]*commit\(["']resetRoutes["']\)/.test(
        routesStore
      ) &&
      /catch\s*\([^)]*\)\s*{[\s\S]*throw\s+error/.test(routesStore) &&
      !/catch\s*\([^)]*\)\s*{[\s\S]*commit\(["']setAllRoutes["'],\s*\[\]\)/.test(
        routesStore
      ),
  },
];

const failures = checks.filter((check) => !check.pass);

if (failures.length) {
  console.error("Dynamic route lifecycle check failed:");
  failures.forEach((failure) => console.error(`- ${failure.label}`));
  process.exit(1);
}

console.log("Dynamic route lifecycle check passed.");
