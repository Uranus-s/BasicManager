const fs = require("fs");
const path = require("path");

const root = path.resolve(__dirname, "..");
const read = (file) => fs.readFileSync(path.join(root, file), "utf8");

const guard = read(path.join("src", "config", "permission.js"));
const routesStore = read(path.join("src", "store", "modules", "routes.js"));

const checks = [
  {
    label: "navigation guard uses independent route initialization state",
    pass: /routes\/isRoutesLoaded/.test(guard),
  },
  {
    label: "navigation guard no longer treats empty permissions as uninitialized",
    pass: !/permissions["'\]]\s*&&[\s\S]{0,120}permissions["'\]]\.length\s*>\s*0/.test(
      guard
    ),
  },
  {
    label: "routes store exposes isRoutesLoaded",
    pass: /isRoutesLoaded/.test(routesStore),
  },
  {
    label: "routes store can reset route initialization state",
    pass: /resetRoutes/.test(routesStore),
  },
  {
    label: "navigation guard redirects empty-permission users to no-permission page",
    pass:
      /NO_PERMISSION_PATH/.test(guard) &&
      /permissions\.length\s*===\s*0/.test(guard),
  },
];

const failures = checks.filter((check) => !check.pass);

if (failures.length) {
  console.error("Route initialization guard check failed:");
  failures.forEach((failure) => console.error(`- ${failure.label}`));
  process.exit(1);
}

console.log("Route initialization guard check passed.");
