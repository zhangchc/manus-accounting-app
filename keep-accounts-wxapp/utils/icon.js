/**
 * 分类图标映射工具
 * 将分类名称映射到静态图片路径
 */

// 分类名 → 图片文件名 映射
const categoryIconMap = {
  // 支出分类
  '餐饮': 'canyin',
  '交通': 'jiaotong',
  '购物': 'gouwu',
  '日用': 'riyong',
  '水果': 'shuiguo',
  '零食': 'lingshi',
  '运动': 'yundong',
  '娱乐': 'yule',
  '通讯': 'tongxun',
  '服饰': 'fushi',
  '美容': 'meirong',
  '住房': 'zhufang',
  '居家': 'jujia',
  '孩子': 'haizi',
  '长辈': 'zhangbei',
  '社交': 'shejiao',
  '旅行': 'lvxing',
  '宠物': 'chongwu',
  '医疗': 'yiliao',
  '学习': 'xuexi',
  // 收入分类
  '工资': 'gongzi',
  '奖金': 'jiangjin',
  '兼职': 'jianzhii',
  '理财': 'licai',
  '红包': 'hongbao',
  '转账': 'zhuanzhang',
  '退款': 'tuikuan'
};

// 分类名 → 柔和背景色 映射
const categoryBgColorMap = {
  '餐饮': '#FFF0ED',
  '交通': '#E8F8F0',
  '购物': '#EBF2FF',
  '日用': '#E8F8F0',
  '水果': '#FFE8EC',
  '零食': '#FFF5E6',
  '运动': '#E5F8F2',
  '娱乐': '#F0EAFF',
  '通讯': '#EBF2FF',
  '服饰': '#FFEEF5',
  '美容': '#FFE8F0',
  '住房': '#FFF2E8',
  '居家': '#E8F0FF',
  '孩子': '#FFE8F5',
  '长辈': '#FFF0E8',
  '社交': '#E8FFE8',
  '旅行': '#E8F5FF',
  '宠物': '#FFF5E8',
  '医疗': '#FFE8E8',
  '学习': '#EBF2FF',
  '工资': '#FFF8E6',
  '奖金': '#FFF5E8',
  '兼职': '#E8F8F0',
  '理财': '#EBF2FF',
  '红包': '#FFE8EC',
  '转账': '#E8F0FF',
  '退款': '#E8FFE8',
  '其他': '#F0F1F5'
};

/**
 * 根据分类名获取图标图片路径
 * @param {string} name 分类名称
 * @param {number} type 类型 1-支出 2-收入（用于区分"其他"分类）
 * @returns {string} 图片路径
 */
export function getCategoryIconPath(name, type) {
  if (name === '其他') {
    return type === 2 ? '/static/category/qita_in.png' : '/static/category/qita_out.png';
  }
  const filename = categoryIconMap[name];
  if (filename) {
    return `/static/category/${filename}.png`;
  }
  // 未找到映射时返回默认图标
  return '/static/category/qita_out.png';
}

/**
 * 根据分类名获取背景色
 * @param {string} name 分类名称
 * @returns {string} 背景色十六进制值
 */
export function getCategoryBgColor(name) {
  return categoryBgColorMap[name] || '#F3F4F8';
}

export default {
  getCategoryIconPath,
  getCategoryBgColor
};
