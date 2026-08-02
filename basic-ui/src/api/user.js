import request from "@/utils/request";

export async function login(data) {
  return request({
    baseURL: "",
    url: "/auth/login",
    method: "post",
    data,
  });
}

export function getUserInfo() {
  return request({
    baseURL: "",
    url: "/auth/info",
    method: "get",
  });
}

export function getAvatar() {
  return request({
    baseURL: "",
    url: "/system/user/getAvatar",
    method: "get",
  });
}

export function uploadUserAvatar(file) {
  const data = new FormData();
  data.append("file", file);

  return request({
    baseURL: "",
    url: "/system/user/avatar",
    method: "post",
    data,
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
}

export function logout() {
  return request({
    baseURL: "",
    url: "/auth/logout",
    method: "post",
  });
}

export function forgotPasswordReset(data) {
  return request({
    baseURL: "",
    url: "/auth/forgotPassword/reset",
    method: "post",
    data,
  });
}

export function register(data) {
  return request({
    baseURL: "",
    url: "/auth/register",
    method: "post",
    data,
  });
}
