import request from './request'

export const aiChat = (data) =>
  request.post('/ai/chat', data)

export const aiDialogue = (data) =>
  request.post('/ai/dialogue', data)
