/**
 * 网络请求封装
 */

const BASE_URL = 'http://localhost:8080/api';

const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token');
    
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? 'Bearer ' + token : '',
        ...options.header
      },
      success: (res) => {
        if (res.data.code === 200) {
          resolve(res.data);
        } else if (res.data.code === 401) {
          // 未登录，跳转登录
          uni.removeStorageSync('token');
          uni.removeStorageSync('userInfo');
          uni.showToast({
            title: '请先登录',
            icon: 'none'
          });
          reject(res.data);
        } else {
          uni.showToast({
            title: res.data.message || '请求失败',
            icon: 'none'
          });
          reject(res.data);
        }
      },
      fail: (err) => {
        uni.showToast({
          title: '网络异常',
          icon: 'none'
        });
        reject(err);
      }
    });
  });
};

export const get = (url, data) => request({ url, method: 'GET', data });
export const post = (url, data) => request({ url, method: 'POST', data });
export const put = (url, data) => request({ url, method: 'PUT', data });
export const del = (url, data) => request({ url, method: 'DELETE', data });

export default request;
