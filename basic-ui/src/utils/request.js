import axios from "axios";
import {
  baseURL,
  contentType,
  debounce,
  invalidCode,
  loginInterception,
  noPermissionCode,
  recordRoute,
  resultCode,
  requestTimeout,
  successCode,
  tokenName,
} from "@/config";
import store from "@/store";
import qs from "qs";
import router from "@/router";
import { isArray } from "@/utils/validate";
import { ElLoading, ElMessage } from "element-plus";
import { pickBy, identity } from "lodash-es";
import { consumeAgentTrace } from "@/utils/aiAgent/traceContext";

let loadingInstance;

const authRedirectCodes = [
  resultCode.unauthorized,
  resultCode.tokenInvalid,
  resultCode.tokenExpired,
  resultCode.accountDisabled,
  resultCode.accountLocked,
];

const redirectToLogin = () => {
  store.dispatch("user/resetAccessToken");
  if (!loginInterception) return;

  const currentRoute = router.currentRoute.value;
  const redirect =
    recordRoute && currentRoute && currentRoute.path !== "/login"
      ? currentRoute.fullPath
      : undefined;

  router
    .replace({
      path: "/login",
      query: redirect ? { redirect } : {},
    })
    .catch(() => {});
};

/**
 * @author https://github.com/zxwk1998/vue-admin-better （不想保留author可删除）
 * @description 处理code异常
 * @param {*} code
 * @param {*} msg
 */
const handleCode = (code, msg) => {
  if (authRedirectCodes.includes(code)) {
    ElMessage.error(msg || "登录状态已失效，请重新登录");
    redirectToLogin();
    return;
  }

  switch (code) {
    case invalidCode:
      ElMessage.error(msg || `后端接口${code}异常`);
      redirectToLogin();
      break;
    case resultCode.forbidden:
    case noPermissionCode:
      router.push({ path: "/401" }).catch(() => {});
      break;
    default:
      ElMessage.error(msg || `后端接口${code}异常`);
      break;
  }
};

// 仅允许具体请求声明可由调用方恢复的业务错误码，其他错误仍走全局提示。
const shouldSilenceError = (config, code) =>
  Array.isArray(config?.silentErrorCodes) && config.silentErrorCodes.includes(code);

const localDateTimePattern =
  /^(\d{4}-\d{2}-\d{2})T(\d{2}:\d{2}:\d{2})(?:\.\d+)?$/;

const normalizeDateTimeFields = (value, seen = new WeakSet()) => {
  if (typeof value === "string") {
    return value.replace(localDateTimePattern, "$1 $2");
  }

  if (!value || typeof value !== "object") return value;

  if (seen.has(value)) return value;
  seen.add(value);

  if (Array.isArray(value)) {
    value.forEach((item, index) => {
      value[index] = normalizeDateTimeFields(item, seen);
    });
    return value;
  }

  Object.keys(value).forEach((key) => {
    value[key] = normalizeDateTimeFields(value[key], seen);
  });
  return value;
};

// 请求重试配置
const retryConfig = {
  retry: 3, // 重试次数
  retryDelay: 1000, // 重试间隔时间
};

// 创建axios实例
const instance = axios.create({
  baseURL,
  timeout: requestTimeout,
  headers: {
    "Content-Type": contentType,
  },
});

// 请求重试方法
instance.defaults.retry = retryConfig.retry;
instance.defaults.retryDelay = retryConfig.retryDelay;

// 请求拦截器
instance.interceptors.request.use(
  (config) => {
    if (store.state.user.accessToken) {
      config.headers = config.headers || {};
      config.headers[tokenName] = store.state.user.accessToken;
      config.headers.Authorization = `Bearer ${store.state.user.accessToken}`;
    }

    // 页面可显式传入追踪对象；通用页面代理自动把当前动作关联到第一笔业务请求。
    const agentTrace = config.aiAgentTrace || consumeAgentTrace();
    if (agentTrace) {
      config.headers = config.headers || {};
      config.headers["X-AI-Agent-Task-Id"] = String(agentTrace.taskId);
      config.headers["X-AI-Agent-Action-Id"] = agentTrace.actionId;
      // 代理动作的业务请求必须保持单次执行，避免重试造成重复写入。
      config.retry = 0;
    }

    //这里会过滤普通对象中为空、0、false的key，如果不需要请自行注释
    if (
      config.data &&
      Object.prototype.toString.call(config.data) === "[object Object]"
    ) {
      const preserveFalsyKeys = config.preserveFalsyKeys || [];
      config.data = pickBy(config.data, (value, key) => {
        if (preserveFalsyKeys.includes(key)) {
          return value !== "" && value !== undefined && value !== null;
        }
        return identity(value);
      });
    }
    if (
      config.data &&
      config.headers["Content-Type"] ===
        "application/x-www-form-urlencoded;charset=UTF-8"
    )
      config.data = qs.stringify(config.data);
    if (debounce.some((item) => config.url.includes(item)))
      loadingInstance = ElLoading.service();

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
instance.interceptors.response.use(
  (response) => {
    if (loadingInstance) loadingInstance.close();

    const { data, config } = response;

    // 判断data是否为undefined或null
    if (data === undefined || data === null) {
      ElMessage.error("后端接口返回数据为空");
      return Promise.reject("后端接口返回数据为空");
    }

    // 安全地解构code和msg，避免undefined异常
    const code = data.code !== undefined ? data.code : null;
    const msg =
      data.msg !== undefined ? data.msg : data.message || "未知错误";

    // 操作正常Code数组
    const codeVerificationArray = isArray(successCode)
      ? [...successCode]
      : [...[successCode]];

    // 是否操作正常
    if (code !== null && codeVerificationArray.includes(code)) {
      return normalizeDateTimeFields(data);
    } else {
      if (!shouldSilenceError(config, code)) handleCode(code, msg);
      return Promise.reject(
        `vue-admin-better请求异常拦截:${JSON.stringify({
          url: config.url,
          code,
          msg,
        })}` || "Error"
      );
    }
  },
  (error) => {
    if (loadingInstance) loadingInstance.close();

    // 处理请求重试
    const { config } = error;
    if (config && config.retry) {
      // 设置当前重试次数
      config.__retryCount = config.__retryCount || 0;

      // 检查是否可以重试
      if (config.__retryCount < config.retry) {
        // 增加重试次数
        config.__retryCount += 1;

        // 创建新的Promise进行重试
        const backoff = new Promise((resolve) => {
          setTimeout(() => {
            console.log(
              `重试请求: ${config.url}, 尝试次数: ${config.__retryCount}`
            );
            resolve();
          }, config.retryDelay || 1000);
        });

        // 重新发起请求
        return backoff.then(() => instance(config));
      }
    }

    // 处理undefined或无法解析的错误情况
    if (!error) {
      ElMessage.error("发生未知错误");
      return Promise.reject("发生未知错误");
    }

    const { response, message } = error;
    if (response && response.data) {
      const { status, data } = response;
      const code = data && data.code !== undefined ? data.code : status;
      if (!shouldSilenceError(config, code)) {
        handleCode(code, data.msg || data.message || message || "未知错误");
      }
      return Promise.reject(error);
    } else {
      let errorMsg = "后端接口未知异常";

      if (message) {
        if (message === "Network Error") {
          errorMsg = "后端接口连接异常";
        } else if (message.includes("timeout")) {
          errorMsg = "后端接口请求超时";
        } else if (message.includes("Request failed with status code")) {
          const code = message.substr(message.length - 3);
          errorMsg = `后端接口${code}异常`;
        }
      }

      ElMessage.error(errorMsg);
      return Promise.reject(error);
    }
  }
);

export default instance;
