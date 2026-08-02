import request from "@/utils/request";

export function getLogList(params) {
  return request({
    baseURL: "",
    url: "/system/log/list",
    method: "get",
    params,
  });
}
