import request from './request'

export const publishProduct = (data) =>
  request.post('/product/publish', data)

export const uploadFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 30000
  })
}

export const getMyProducts = (params) =>
  request.get('/product/my', { params })
