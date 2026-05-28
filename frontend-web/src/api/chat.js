import request from './request'

export const getChatSessions = (params) =>
  request.get('/chat/sessions', { params })

export const getChatHistory = (params) =>
  request.get('/chat/history', { params })
