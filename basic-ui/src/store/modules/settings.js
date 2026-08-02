/**
 * @author https://github.com/zxwk1998/vue-admin-better （不想保留author可删除）
 * @description 所有全局配置的状态管理，如无必要请勿修改
 */

import defaultSettings from '@/config'
import { getPublicSystemSettings } from '@/api/system/config'
import { normalizeSystemName } from '@/utils/systemName'

const { tabsBar, logo, layout, header, themeBar, title } = defaultSettings
const theme = JSON.parse(localStorage.getItem('basic-manage-theme')) || ''
const state = () => ({
  tabsBar: theme.tabsBar || tabsBar,
  logo,
  collapse: false,
  layout: theme.layout || layout,
  header: theme.header || header,
  device: 'desktop',
  themeBar,
  systemName: title,
})
const getters = {
  collapse: (state) => state.collapse,
  device: (state) => state.device,
  header: (state) => state.header,
  layout: (state) => state.layout,
  logo: (state) => state.logo,
  tabsBar: (state) => state.tabsBar,
  themeBar: (state) => state.themeBar,
  systemName: (state) => state.systemName,
}
const mutations = {
  setSystemName: (state, systemName) => {
    state.systemName = normalizeSystemName(systemName, title)
  },
  changeLayout: (state, layout) => {
    if (layout) state.layout = layout
  },
  changeHeader: (state, header) => {
    if (header) state.header = header
  },
  changeTabsBar: (state, tabsBar) => {
    if (tabsBar) state.tabsBar = tabsBar
  },
  changeCollapse: (state) => {
    state.collapse = !state.collapse
  },
  foldSideBar: (state) => {
    state.collapse = true
  },
  openSideBar: (state) => {
    state.collapse = false
  },
  toggleDevice: (state, device) => {
    state.device = device
  },
}
const actions = {
  async loadPublicSettings({ commit }) {
    try {
      const { data } = await getPublicSystemSettings()
      commit('setSystemName', data?.systemName)
    } catch (error) {
      // 公开配置不可用不应阻塞登录，保留静态标题并记录诊断信息。
      commit('setSystemName', title)
      console.warn('获取公开系统设置失败，已使用静态配置。', error)
    }
  },
  changeLayout({ commit }, layout) {
    commit('changeLayout', layout)
  },
  changeHeader({ commit }, header) {
    commit('changeHeader', header)
  },
  changeTabsBar({ commit }, tabsBar) {
    commit('changeTabsBar', tabsBar)
  },
  changeCollapse({ commit }) {
    commit('changeCollapse')
  },
  foldSideBar({ commit }) {
    commit('foldSideBar')
  },
  openSideBar({ commit }) {
    commit('openSideBar')
  },
  toggleDevice({ commit }, device) {
    commit('toggleDevice', device)
  },
}
export default { state, getters, mutations, actions }
