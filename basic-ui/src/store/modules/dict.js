import { getDictItemsByCode } from "@/api/system/dict";

const normalizeValue = (value) => String(value ?? "");

const sortDictItems = (items) =>
  [...items].sort((a, b) => {
    const sortA = Number(a.sort ?? 0);
    const sortB = Number(b.sort ?? 0);
    if (sortA !== sortB) return sortA - sortB;
    return Number(a.id ?? 0) - Number(b.id ?? 0);
  });

const state = () => ({
  itemsByCode: {},
  loadingByCode: {},
});

const getters = {
  itemsByCode: (state) => (code) => state.itemsByCode[code] || [],
  label: (state, getters) => (code, value, fallback) => {
    const matched = getters
      .itemsByCode(code)
      .find((item) => normalizeValue(item.itemValue) === normalizeValue(value));

    if (matched) return matched.itemLabel;
    if (fallback !== undefined) return fallback;
    return value === undefined || value === null || value === "" ? "-" : value;
  },
  options: (state, getters) => (code, fallbackOptions = []) => {
    const items = getters.itemsByCode(code);
    const source = items.length
      ? items
      : fallbackOptions.map((option) => ({
          itemLabel: option.label,
          itemValue: option.value,
          status: option.disabled ? 0 : 1,
        }));

    return source.map((item) => ({
      label: item.itemLabel,
      value: item.itemValue,
      disabled: item.status === 0,
      raw: item,
    }));
  },
  tagType: () => (code, value) => {
    if (code === "user_status") {
      const status = normalizeValue(value);
      if (status === "1") return "success";
      if (status === "0") return "info";
    }

    return "";
  },
};

const mutations = {
  setItems(state, { code, items }) {
    state.itemsByCode[code] = items;
  },
  setLoading(state, { code, loading }) {
    if (loading) {
      state.loadingByCode[code] = loading;
      return;
    }

    delete state.loadingByCode[code];
  },
  clearDict(state, code) {
    delete state.itemsByCode[code];
    delete state.loadingByCode[code];
  },
  clearAll(state) {
    state.itemsByCode = {};
    state.loadingByCode = {};
  },
};

const actions = {
  async loadDict({ state, commit }, code) {
    if (!code) return [];
    if (state.itemsByCode[code]) return state.itemsByCode[code];
    if (state.loadingByCode[code]) return state.loadingByCode[code];

    const loading = getDictItemsByCode(code)
      .then(({ data }) => {
        const items = sortDictItems(
          (Array.isArray(data) ? data : []).filter((item) => item.status === 1)
        );
        commit("setItems", { code, items });
        return items;
      })
      .finally(() => {
        commit("setLoading", { code, loading: null });
      });

    commit("setLoading", { code, loading });
    return loading;
  },
  loadDicts({ dispatch }, codes) {
    return Promise.all((codes || []).map((code) => dispatch("loadDict", code)));
  },
  clearDict({ commit }, code) {
    commit("clearDict", code);
  },
  clearAll({ commit }) {
    commit("clearAll");
  },
};

export default { state, getters, mutations, actions };
