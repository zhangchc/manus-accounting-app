/**
 * API 接口封装
 */
import { get, post, put, del } from '../utils/request';

// ========== 用户相关 ==========
export const wxLogin = (data) => post('/user/wxLogin', data);
export const devLogin = (data) => post('/user/login', data);
export const getUserInfo = () => get('/user/info');
export const updateUserInfo = (data) => put('/user/info', data);

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
