import request from "@/utils/request";

export function getSystemMonitorStatus() {
  return request({
    baseURL: "",
    url: "/system/monitor/status",
    method: "get",
  });
}

export function getThreadPoolMonitorStatus() {
  return request({
    baseURL: "",
    url: "/system/monitor/thread-pools",
    method: "get",
  });
}
