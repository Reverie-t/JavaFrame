import axios, { type InternalAxiosRequestConfig } from 'axios'
import { ApiError, type FieldViolation, type Result } from './types'
import { refreshTokens } from './refresh'

type RetriableConfig = InternalAxiosRequestConfig & { _retried?: boolean }

const request = axios.create({
  timeout: 10000,
})

let refreshing: Promise<string> | null = null

request.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => response,
  async (error) => {
    const config = error.config as RetriableConfig | undefined
    const url = config?.url ?? ''
    const isAuthUrl = url.includes('/api/v1/auth/login') || url.includes('/api/v1/auth/refresh')
    if (error.response?.status === 401 && config && !config._retried && !isAuthUrl) {
      const refreshToken = sessionStorage.getItem('refreshToken')
      if (refreshToken) {
        try {
          if (!refreshing) {
            refreshing = refreshTokens(refreshToken)
              .then((pair) => {
                sessionStorage.setItem('token', pair.token)
                sessionStorage.setItem('refreshToken', pair.refreshToken)
                return pair.token
              })
              .finally(() => {
                refreshing = null
              })
          }
          const token = await refreshing
          config._retried = true
          config.headers.Authorization = `Bearer ${token}`
          return request(config)
        } catch {
          sessionStorage.removeItem('token')
          sessionStorage.removeItem('refreshToken')
          return Promise.reject(new ApiError('登录已过期，请重新登录'))
        }
      }
    }
    const data = error.response?.data as Result<{ fields?: FieldViolation[] }> | undefined
    return Promise.reject(new ApiError(data?.message ?? error.message, data?.data?.fields ?? []))
  },
)

export default request