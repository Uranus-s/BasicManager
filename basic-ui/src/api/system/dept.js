import request from "@/utils/request";

const preserveFalsyKeys = ["parentId", "sort"];

export function getDeptTree() {
  return request({
    baseURL: "",
    url: "/system/dept/tree",
    method: "get",
  });
}

export function getDeptAll() {
  return request({
    baseURL: "",
    url: "/system/dept/all",
    method: "get",
  });
}

export function getDeptDetail(id) {
  return request({
    baseURL: "",
    url: `/system/dept/${id}`,
    method: "get",
  });
}

export function createDept(data) {
  return request({
    baseURL: "",
    url: "/system/dept",
    method: "post",
    data,
    preserveFalsyKeys,
  });
}

export function updateDept(data) {
  return request({
    baseURL: "",
    url: "/system/dept",
    method: "put",
    data,
    preserveFalsyKeys,
  });
}

export function deleteDept(id) {
  return request({
    baseURL: "",
    url: `/system/dept/${id}`,
    method: "delete",
  });
}

export function getDeptUsers(deptId) {
  return request({
    baseURL: "",
    url: `/system/dept/users/${deptId}`,
    method: "get",
  });
}

export function addDeptUsers(deptId, userIds) {
  return request({
    baseURL: "",
    url: `/system/dept/users/${deptId}`,
    method: "post",
    data: userIds,
  });
}

export function removeDeptUsers(deptId, userIds) {
  return request({
    baseURL: "",
    url: `/system/dept/users/${deptId}`,
    method: "delete",
    data: userIds,
  });
}
