import request from "@/utils/request";

const preserveFalsyKeys = ["status", "sort"];

export function getDictList(params) {
  return request({
    baseURL: "",
    url: "/system/dict/list",
    method: "get",
    params,
  });
}

export function getDictAll() {
  return request({
    baseURL: "",
    url: "/system/dict/all",
    method: "get",
  });
}

export function getDictDetail(id) {
  return request({
    baseURL: "",
    url: `/system/dict/${id}`,
    method: "get",
  });
}

export function createDict(data) {
  return request({
    baseURL: "",
    url: "/system/dict",
    method: "post",
    data,
    preserveFalsyKeys,
  });
}

export function updateDict(data) {
  return request({
    baseURL: "",
    url: "/system/dict",
    method: "put",
    data,
    preserveFalsyKeys,
  });
}

export function deleteDict(id) {
  return request({
    baseURL: "",
    url: `/system/dict/${id}`,
    method: "delete",
  });
}

export function getDictItemList(params) {
  return request({
    baseURL: "",
    url: "/system/dict/item/list",
    method: "get",
    params,
  });
}

export function getDictItemsByCode(dictCode) {
  return request({
    baseURL: "",
    url: `/system/dict/item/code/${dictCode}`,
    method: "get",
  });
}

export function getDictItemDetail(id) {
  return request({
    baseURL: "",
    url: `/system/dict/item/${id}`,
    method: "get",
  });
}

export function createDictItem(data) {
  return request({
    baseURL: "",
    url: "/system/dict/item",
    method: "post",
    data,
    preserveFalsyKeys,
  });
}

export function updateDictItem(data) {
  return request({
    baseURL: "",
    url: "/system/dict/item",
    method: "put",
    data,
    preserveFalsyKeys,
  });
}

export function deleteDictItem(id) {
  return request({
    baseURL: "",
    url: `/system/dict/item/${id}`,
    method: "delete",
  });
}
