import request from "@/utils/request";

export function getNoticeList(params) {
  return request({
    baseURL: "",
    url: "/system/notice/list",
    method: "get",
    params,
  });
}

export function getNoticeDetail(id) {
  return request({
    baseURL: "",
    url: `/system/notice/${id}`,
    method: "get",
  });
}

export function getNoticeTargetOptions() {
  return request({
    baseURL: "",
    url: "/system/notice/target-options",
    method: "get",
  });
}

export function createNotice(data) {
  return request({
    baseURL: "",
    url: "/system/notice",
    method: "post",
    data,
    preserveFalsyKeys: ["version"],
  });
}

export function updateNotice(data) {
  return request({
    baseURL: "",
    url: "/system/notice",
    method: "put",
    data,
    preserveFalsyKeys: ["version"],
  });
}

export function deleteNotice(id) {
  return request({
    baseURL: "",
    url: `/system/notice/${id}`,
    method: "delete",
  });
}

export function publishNotice(id) {
  return request({
    baseURL: "",
    url: `/system/notice/${id}/publish`,
    method: "post",
  });
}

export function withdrawNotice(id) {
  return request({
    baseURL: "",
    url: `/system/notice/${id}/withdraw`,
    method: "post",
  });
}
