const { defineConfig } = require("@playwright/test")

module.exports = defineConfig({
  testDir: "./e2e",
  use: {
    baseURL: process.env.E2E_BASE_URL || "http://localhost:8091",
    trace: "retain-on-failure",
    screenshot: "only-on-failure",
  },
})
