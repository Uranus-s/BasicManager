import request from "@/utils/request";

export function getPublicSystemSettings() {
  return request({
    baseURL: "",
    url: "/public/system/settings",
    method: "get",
    // 启动阶段只请求一次，后端不可用时立即使用静态配置，避免阻塞登录页。
    retry: 0,
  });
}

export function getSystemSettings() {
  return request({
    baseURL: "",
    url: "/system/config/settings",
    method: "get",
  });
}

export function updateBasicSettings(data) {
  return request({
    baseURL: "",
    url: "/system/config/settings/basic",
    method: "put",
    data,
  });
}

export function updateSecuritySettings(data) {
  return request({
    baseURL: "",
    url: "/system/config/settings/security",
    method: "put",
    data,
  });
}
