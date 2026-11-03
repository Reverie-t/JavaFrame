import request from './request'

import type { Result } from './types'

export function fetchHealth() {
  return request.get<Result<{ status: string }>>('/api/v1/health')
}