/**
 * 工具函数
 */

/**
 * 格式化金额
 */
export const formatMoney = (amount) => {
  if (!amount && amount !== 0) return '0.00';
  const num = parseFloat(amount);
  return num.toFixed(2);
};

/**
 * 获取当前年月 yyyy-MM
 */
export const getCurrentYearMonth = () => {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  return `${year}-${month}`;
};

/**
 * 获取当前日期 yyyy-MM-dd
 */
export const getCurrentDate = () => {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

/**
 * 获取当前时间 HH:mm
 */
export const getCurrentTime = () => {
  const now = new Date();
  const hours = String(now.getHours()).padStart(2, '0');
  const minutes = String(now.getMinutes()).padStart(2, '0');
  return `${hours}:${minutes}`;
};

/**
 * 获取星期几
 */
export const getWeekDay = (dateStr) => {
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
  const date = new Date(dateStr.replace(/-/g, '/'));
  return weekDays[date.getDay()];
};

/**
 * 判断是否是今天
 */
export const isToday = (dateStr) => {
  return dateStr === getCurrentDate();
};

/**
 * 判断是否是昨天
 */
export const isYesterday = (dateStr) => {
  const yesterday = new Date();
  yesterday.setDate(yesterday.getDate() - 1);
  const year = yesterday.getFullYear();
  const month = String(yesterday.getMonth() + 1).padStart(2, '0');
  const day = String(yesterday.getDate()).padStart(2, '0');
  return dateStr === `${year}-${month}-${day}`;
};

/**
 * 格式化日期显示
 */
export const formatDateDisplay = (dateStr) => {
  if (isToday(dateStr)) return '今天';
  if (isYesterday(dateStr)) return '昨天';
  const parts = dateStr.split('-');
  return `${parseInt(parts[1])}月${parseInt(parts[2])}日`;
};

/**
 * 获取月份中文名
 */
export const getMonthName = (yearMonth) => {
  const parts = yearMonth.split('-');
  return `${parts[0]}年${parseInt(parts[1])}月`;
};

/**
 * 上一个月
 */
export const getPrevMonth = (yearMonth) => {
  const parts = yearMonth.split('-');
  let year = parseInt(parts[0]);
  let month = parseInt(parts[1]) - 1;
  if (month === 0) {
    month = 12;
    year -= 1;
  }
  return `${year}-${String(month).padStart(2, '0')}`;
};

/**
 * 下一个月
 */
export const getNextMonth = (yearMonth) => {
  const parts = yearMonth.split('-');
  let year = parseInt(parts[0]);
  let month = parseInt(parts[1]) + 1;
  if (month === 13) {
    month = 1;
    year += 1;
  }
  return `${year}-${String(month).padStart(2, '0')}`;
};
