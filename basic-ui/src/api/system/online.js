import request from "@/utils/request";

export function getOnlineUsers() {
  return request({
    baseURL: "",
    url: "/auth/onlineUsers",
    method: "get",
  });
}

export function forceLogout(userId) {
  return request({
    baseURL: "",
    url: `/auth/forceLogout/${userId}`,
    method: "post",
  });
}
