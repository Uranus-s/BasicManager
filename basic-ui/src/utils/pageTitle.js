import { title } from '@/config'
import store from '@/store'
import { buildPageTitle, normalizeSystemName } from '@/utils/systemName'

/**
 * @author https://github.com/zxwk1998/vue-admin-better （不想保留author可删除）
 * @description 设置标题
 * @param pageTitle
 * @returns {string}
 */
export default function getPageTitle(pageTitle) {
  const systemName = normalizeSystemName(
    store.getters['settings/systemName'],
    title
  )
  return buildPageTitle(pageTitle, systemName)
}
