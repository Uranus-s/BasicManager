/**
 * @author https://github.com/zxwk1998/vue-admin-better （不想保留author可删除）
 * @description 登录、获取用户信息、退出登录、清除accessToken逻辑，不建议修改
 */

import { getAvatar, getUserInfo, login, logout } from "@/api/user";
import {
  getAccessToken,
  removeAccessToken,
  setAccessToken,
} from "@/utils/accessToken";
import { resetRouter } from "@/router";
import { tokenName } from "@/config";
import { ElMessage } from "element-plus";

const state = () => ({
  accessToken: getAccessToken(),
  username: "",
  nickname: "",
  email: "",
  avatar: "",
  roles: [],
  deptNames: [],
  permissions: [],
});
const getters = {
  accessToken: (state) => state.accessToken,
  username: (state) => state.username,
  nickname: (state) => state.nickname,
  email: (state) => state.email,
  avatar: (state) => state.avatar,
  roles: (state) => state.roles,
  deptNames: (state) => state.deptNames,
  permissions: (state) => state.permissions,
};
const mutations = {
  setAccessToken(state, accessToken) {
    state.accessToken = accessToken;
    setAccessToken(accessToken);
  },
  setUsername(state, username) {
    state.username = username;
  },
  setNickname(state, nickname) {
    state.nickname = nickname;
  },
  setEmail(state, email) {
    state.email = email;
  },
  setAvatar(state, avatar) {
    state.avatar = avatar;
  },
  setRoles(state, roles) {
    state.roles = roles;
  },
  setDeptNames(state, deptNames) {
    state.deptNames = deptNames;
  },
  setPermissions(state, permissions) {
    state.permissions = permissions;
  },
};
const actions = {
  setPermissions({ commit }, permissions) {
    commit("setPermissions", permissions);
  },
  async refreshAvatar({ commit }) {
    const { data } = await getAvatar();
    commit("setAvatar", data || "");
    return data;
  },
  async login({ commit, rootGetters }, userInfo) {
    const { data } = await login(userInfo);
    const accessToken = data[tokenName] || data.token;
    if (accessToken) {
      commit("setAccessToken", accessToken);
      const hour = new Date().getHours();
      const thisTime =
        hour < 8
          ? "早上好"
          : hour <= 11
          ? "上午好"
          : hour <= 13
          ? "中午好"
          : hour < 18
          ? "下午好"
          : "晚上好";
      ElMessage.success(
        `欢迎登录${rootGetters["settings/systemName"]}，${thisTime}！`
      );
    } else {
      ElMessage.error(`登录接口异常，未正确返回${tokenName}...`);
    }
  },
  async getUserInfo({ commit, state }) {
    try {
      const { data } = await getUserInfo();
      if (!data) {
        ElMessage.error("验证失败，请重新登录...");
        return false;
      }
      const {
        permissions,
        username,
        nickname,
        email,
        avatar,
        roles,
        deptNames,
        deptName,
        department,
        dept,
      } = data;
      const normalizedDeptNames = Array.isArray(deptNames)
        ? deptNames
        : [deptName, department, dept && (dept.deptName || dept.name)]
            .filter(Boolean);
      const displayName = nickname || username;
      if (Array.isArray(permissions) && displayName) {
        commit("setPermissions", permissions);
        commit("setUsername", username || "");
        commit("setNickname", nickname || "");
        commit("setEmail", email || "");
        commit("setAvatar", avatar || "");
        commit("setRoles", Array.isArray(roles) ? roles : []);
        commit("setDeptNames", normalizedDeptNames);
        try {
          const { data: currentAvatar } = await getAvatar();
          commit("setAvatar", currentAvatar || "");
        } catch (avatarError) {
          console.error("获取头像失败:", avatarError);
        }
        return permissions;
      } else {
        ElMessage.error("用户信息接口异常");
        return false;
      }
    } catch (error) {
      console.error("获取用户信息失败:", error);
      ElMessage.error("获取用户信息失败，请重新登录");
      return false;
    }
  },
  async logout({ dispatch }) {
    await logout(state.accessToken);
    await dispatch("resetAccessToken");
    await resetRouter();
    location.reload();
  },
  resetAccessToken({ commit, dispatch }) {
    commit("setPermissions", []);
    commit("setUsername", "");
    commit("setNickname", "");
    commit("setEmail", "");
    commit("setAvatar", "");
    commit("setRoles", []);
    commit("setDeptNames", []);
    commit("setAccessToken", "");
    dispatch("routes/resetRoutes", null, { root: true });
    resetRouter();
    removeAccessToken();
  },
};
export default { state, getters, mutations, actions };
