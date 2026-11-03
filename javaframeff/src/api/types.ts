export interface Result<T> {
  code: number
  message: string
  data: T | null
}

export interface FieldViolation {
  field: string
  message: string
}

export class ApiError extends Error {
  readonly fields: FieldViolation[]

  constructor(message: string, fields: FieldViolation[] = []) {
    super(message)
    this.name = 'ApiError'
    this.fields = fields
  }
}

export function requireData<T>(result: Result<T>): T {
  if (result.data === null || result.data === undefined) {
    throw new ApiError('响应缺少数据')
  }
  return result.data
}
