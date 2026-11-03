import request from './request'
import type { TokenPair } from './refresh'
import type { Result } from './types'

export type { TokenPair }

export function login(username: string, password: string) {
  return request.post<Result<TokenPair>>('/api/v1/auth/login', { username, password })
}

export function fetchMe() {
  return request.get<Result<{ username: string }>>('/api/v1/me')
}