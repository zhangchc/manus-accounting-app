/**
 * API 接口封装
 */
import { get, post, put, del } from '../utils/request';
import { currentEnv } from '../config/env';

// ========== 用户相关 ==========
export const wxLogin = (data) => post('/user/wxLogin', data);
export const devLogin = (data) => post('/user/login', data);
export const getUserInfo = () => get('/user/info');
export const updateUserInfo = (data) => put('/user/info', data);

/**
 * 上传头像 - 将微信临时文件上传到服务器
 * @param {string} tempFilePath - wxfile:// 临时路径
 * @returns {Promise<string>} 服务器返回的头像 URL
 */
export const uploadAvatar = (tempFilePath) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token');
    uni.uploadFile({
      url: currentEnv.baseUrl + '/user/avatar',
      filePath: tempFilePath,
      name: 'file',
      header: {
        'Authorization': token ? 'Bearer ' + token : ''
      },
      success: (res) => {
        try {
          const data = JSON.parse(res.data);
          if (data.code === 200) {
            resolve(currentEnv.baseUrl + data.data);
          } else {
            reject(new Error(data.message || '上传失败'));
          }
        } catch (e) {
          reject(new Error('解析响应失败'));
        }
      },
      fail: (err) => reject(err)
    });
  });
};

// ========== 账本相关 ==========
export const getBookList = () => get('/book/list');
export const getDefaultBook = () => get('/book/default');
export const createBook = (data) => post('/book', data);
export const updateBook = (data) => put('/book', data);
export const deleteBook = (id) => del(`/book/${id}`);

// ========== 分类相关 ==========
export const getCategoryList = (type) => get('/category/list', { type });
export const createCategory = (data) => post('/category', data);
export const deleteCategory = (id) => del(`/category/${id}`);

// ========== 记账记录相关 ==========
export const addRecord = (data) => post('/record', data);
export const updateRecord = (data) => put('/record', data);
export const deleteRecord = (id) => del(`/record/${id}`);
export const getMonthBill = (params) => get('/record/bill/month', params);
export const getStatistics = (params) => get('/record/statistics', params);
export const getYearSummary = (params) => get('/record/summary/year', params);
export const getTodayExpense = () => get('/record/today/expense');

// ========== 预算相关 ==========
export const setBudget = (data) => post('/budget', data);
export const getMonthBudgets = (yearMonth) => get('/budget/month', { yearMonth });
