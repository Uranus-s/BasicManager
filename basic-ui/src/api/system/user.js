import request from "@/utils/request";

export function getUserList(params) {
  return request({
    baseURL: "",
    url: "/system/user/list",
    method: "get",
    params,
  });
}

export function getUserDetail(id) {
  return request({
    baseURL: "",
    url: `/system/user/${id}`,
    method: "get",
  });
}

export function createUser(data) {
  return request({
    baseURL: "",
    url: "/system/user",
    method: "post",
    data,
  });
}

export function updateUser(data) {
  return request({
    baseURL: "",
    url: "/system/user",
    method: "put",
    data,
  });
}

export function deleteUser(id) {
  return request({
    baseURL: "",
    url: `/system/user/${id}`,
    method: "delete",
  });
}

export function getUserRoles(userId) {
  return request({
    baseURL: "",
    url: `/system/user/roles/${userId}`,
    method: "get",
  });
}

export function assignUserRoles(userId, roleIds) {
  return request({
    baseURL: "",
    url: `/system/user/assignRoles/${userId}`,
    method: "post",
    data: roleIds,
  });
}
