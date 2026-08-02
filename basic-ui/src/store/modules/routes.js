/**
 * @author https://github.com/zxwk1998/vue-admin-better （不想保留author可删除）
 * @description 路由拦截状态管理，业务导航由后端/auth/routes返回
 */
import { constantRoutes, fallbackRoute } from '@/router'
import { getAuthRoutes } from '@/api/router'
import { convertAuthRoutes } from '@/utils/handleRoutes'

const state = () => ({
  routes: [],
  partialRoutes: [],
  isRoutesLoaded: false,
})
const getters = {
  routes: (state) => state.routes,
  partialRoutes: (state) => state.partialRoutes,
  isRoutesLoaded: (state) => state.isRoutesLoaded,
}
const mutations = {
  setRoutes(state, routes) {
    state.routes = constantRoutes.concat(routes)
    state.isRoutesLoaded = true
  },
  setAllRoutes(state, routes) {
    state.routes = constantRoutes.concat(routes)
    state.isRoutesLoaded = true
  },
  setMixedRoutes(state, routes) {
    state.routes = constantRoutes.concat(routes)
    state.isRoutesLoaded = true
  },
  resetRoutes(state) {
    state.routes = []
    state.partialRoutes = []
    state.isRoutesLoaded = false
  },
}
const actions = {
  /**
   * @author https://github.com/zxwk1998/vue-admin-better （不想保留author可删除）
   * @description 本地写死导航已移除，保留空实现兼容旧配置
   * @param {*} { commit }
   * @param {*} permissions
   * @returns
   */
  async setRoutes({ commit }) {
    const accessedRoutes = []
    commit('setRoutes', accessedRoutes)
    return accessedRoutes
  },
  /**
   * @author https://github.com/zxwk1998/vue-admin-better （不想保留author可删除）
   * @description all模式设置路由
   * @param {*} { commit }
   * @returns
   */
  async setAllRoutes({ commit }) {
    try {
      let { data } = await getAuthRoutes()
      if (!data || !Array.isArray(data)) {
        console.error('后端返回的/auth/routes数据格式不正确', data)
        data = []
      }

      const accessedRoutes = convertAuthRoutes(data).concat([fallbackRoute])
      commit('setAllRoutes', accessedRoutes)
      return accessedRoutes
    } catch (error) {
      console.error('获取/auth/routes失败', error)
      commit('resetRoutes')
      throw error
    }
  },
  async setMixedRoutes({ dispatch }) {
    return dispatch('setAllRoutes')
  },
  resetRoutes({ commit }) {
    commit('resetRoutes')
  },
}
export default { state, getters, mutations, actions }
