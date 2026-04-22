const BASE_URL = 'http://localhost:8080/api';

export const request = (options) => {
  return new Promise((resolve, reject) => {
    // 从本地存储获取登录 token
    const token = uni.getStorageSync('user_token') || ''

    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        'Authorization': token,
        ...options.header
      },
      success: (res) => {
        if (res.statusCode === 200) {
          if (res.data.code === 200) {
            resolve(res.data.data)
          } else if (res.data.code === 401) {
            // Token 失效，跳转到登录页
            uni.removeStorageSync('user_token')
            uni.removeStorageSync('user_info')
            uni.reLaunch({ url: '/pages/login/login' })
            reject(res.data)
          } else {
            uni.showToast({
              title: res.data.msg || '请求失败',
              icon: 'none'
            })
            reject(res.data)
          }
        } else {
          uni.showToast({ title: '网络错误', icon: 'none' })
          reject(res)
        }
      },
      fail: (err) => {
        uni.showToast({ title: '服务器连接失败，请检查网络', icon: 'none' })
        reject(err)
      }
    })
  })
}
