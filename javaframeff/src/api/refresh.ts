import axios from 'axios'
import { requireData, type Result } from './types'

export type TokenPair = { token: string; refreshToken: string }

export async function refreshTokens(refreshToken: string): Promise<TokenPair> {
  const response = await axios.post<Result<TokenPair>>(
    '/api/v1/auth/refresh',
    { refreshToken },
    { timeout: 10000 },
  )
  return requireData(response.data)
}
