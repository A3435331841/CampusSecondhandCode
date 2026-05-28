import request from './request'

export const login = (username, password) =>
  request.post('/auth/login', { username, password })

export const logout = () =>
  request.post('/auth/logout')
