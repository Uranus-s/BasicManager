import request from "@/utils/request";

export function getLatestNotices(params) {
  return request({
    baseURL: "",
    url: "/notice/latest",
    method: "get",
    params,
  });
}

export function getVisibleNoticeList(params) {
  return request({
    baseURL: "",
    url: "/notice/list",
    method: "get",
    params,
  });
}

export function getVisibleNoticeDetail(id) {
  return request({
    baseURL: "",
    url: `/notice/${id}`,
    method: "get",
  });
}
