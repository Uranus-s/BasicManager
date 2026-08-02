import request from "@/utils/request";

const preserveFalsyKeys = ["parentId", "sort", "visible", "status"];

export function getPermissionTree() {
  return request({
    baseURL: "",
    url: "/system/permission/tree",
    method: "get",
  });
}

export function getPermissionList(params) {
  return request({
    baseURL: "",
    url: "/system/permission/list",
    method: "get",
    params,
  });
}

export function getPermissionAll() {
  return request({
    baseURL: "",
    url: "/system/permission/all",
    method: "get",
  });
}

export function getPermissionDetail(id) {
  return request({
    baseURL: "",
    url: `/system/permission/${id}`,
    method: "get",
  });
}

export function createPermission(data) {
  return request({
    baseURL: "",
    url: "/system/permission",
    method: "post",
    data,
    preserveFalsyKeys,
  });
}

export function updatePermission(data) {
  return request({
    baseURL: "",
    url: "/system/permission",
    method: "put",
    data,
    preserveFalsyKeys,
  });
}

export function deletePermission(id) {
  return request({
    baseURL: "",
    url: `/system/permission/${id}`,
    method: "delete",
  });
}
