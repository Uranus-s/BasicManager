import request from "@/utils/request";

export function getRoleList(params) {
  return request({
    baseURL: "",
    url: "/system/role/list",
    method: "get",
    params,
  });
}

export function getRoleAll() {
  return request({
    baseURL: "",
    url: "/system/role/all",
    method: "get",
  });
}

export function getAllRoles() {
  return getRoleAll();
}

export function getRoleDetail(id) {
  return request({
    baseURL: "",
    url: `/system/role/${id}`,
    method: "get",
  });
}

export function createRole(data) {
  return request({
    baseURL: "",
    url: "/system/role",
    method: "post",
    data,
    preserveFalsyKeys: ["status"],
  });
}

export function updateRole(data) {
  return request({
    baseURL: "",
    url: "/system/role",
    method: "put",
    data,
    preserveFalsyKeys: ["status"],
  });
}

export function deleteRole(id) {
  return request({
    baseURL: "",
    url: `/system/role/${id}`,
    method: "delete",
  });
}

export function assignRolePermissions(roleId, permissionIds) {
  return request({
    baseURL: "",
    url: `/system/role/assignPermissions/${roleId}`,
    method: "post",
    data: permissionIds,
  });
}

export function getRolePermissions(roleId) {
  return request({
    baseURL: "",
    url: `/system/role/permissions/${roleId}`,
    method: "get",
  });
}

export function getRoleUsers(roleId) {
  return request({
    baseURL: "",
    url: `/system/role/users/${roleId}`,
    method: "get",
  });
}

export function updateRoleUsers(roleId, data) {
  return request({
    baseURL: "",
    url: `/system/role/users/${roleId}`,
    method: "post",
    data,
  });
}
