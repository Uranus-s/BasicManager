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

export function createNotice(data, options = {}) {
  return request({
    ...options,
    baseURL: "",
    url: "/system/notice",
    method: "post",
    data,
    preserveFalsyKeys: ["version"],
  });
}

export function updateNotice(data, options = {}) {
  return request({
    ...options,
    baseURL: "",
    url: "/system/notice",
    method: "put",
    data,
    preserveFalsyKeys: ["version"],
  });
}

export function deleteNotice(id, options = {}) {
  return request({
    ...options,
    baseURL: "",
    url: `/system/notice/${id}`,
    method: "delete",
  });
}

export function publishNotice(id, options = {}) {
  return request({
    ...options,
    baseURL: "",
    url: `/system/notice/${id}/publish`,
    method: "post",
  });
}

export function withdrawNotice(id, options = {}) {
  return request({
    ...options,
    baseURL: "",
    url: `/system/notice/${id}/withdraw`,
    method: "post",
  });
}
