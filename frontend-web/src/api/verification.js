import request from './request'

export const verifyStudent = (data) =>
  request.post('/verification/student', data)
