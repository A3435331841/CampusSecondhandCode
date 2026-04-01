// 封装统一的请求工具
const BASE_URL = 'http://localhost:8080/api';

export const request = (options) => {
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        ...options.header
      },
      success: (res) => {
        if (res.statusCode === 200) {
          // 假设后端返回格式为 { code: 200, message: 'xxx', data: ... }
          if (res.data.code === 200) {
            resolve(res.data.data);
          } else {
            uni.showToast({
              title: res.data.message || '请求失败',
              icon: 'none'
            });
            reject(res.data);
          }
        } else {
          uni.showToast({
            title: '网络错误',
            icon: 'none'
          });
          reject(res);
        }
      },
      fail: (err) => {
        uni.showToast({
          title: '服务器连接失败',
          icon: 'none'
        });
        reject(err);
      }
    });
  });
};