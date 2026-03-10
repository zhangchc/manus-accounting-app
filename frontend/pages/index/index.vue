<template>
  <view class="page">
    <!-- 顶部状态栏占位 -->
    <view :style="{ height: statusBarHeight + 'px' }"></view>
    
    <!-- 头部区域 -->
    <view class="header">
      <view class="header-top">
        <view class="greeting-area">
          <text class="greeting-text">{{ greetingText }}，{{ userName }}</text>
          <view class="header-date">
            <text class="date-text">{{ todayStr }}</text>
            <text class="date-emoji">{{ greetingEmoji }}</text>
          </view>
        </view>
        <view class="user-avatar">
          <text class="avatar-text">{{ avatarText }}</text>
        </view>
      </view>
      
      <!-- 本月概览卡片 -->
      <view class="overview-card">
        <text class="overview-title">本月概览</text>
        <view class="overview-body">
          <view class="overview-item">
            <view class="overview-icon-wrap expense-icon">
              <text class="overview-icon-text">↓</text>
            </view>
            <text class="overview-label">支出</text>
            <text class="overview-amount">{{ showAmount ? '¥' + formatMoney(monthExpense) : '****' }}</text>
          </view>
          <view class="overview-item">
            <view class="overview-icon-wrap income-icon">
              <text class="overview-icon-text">↑</text>
            </view>
            <text class="overview-label">收入</text>
            <text class="overview-amount">{{ showAmount ? '¥' + formatMoney(monthIncome) : '****' }}</text>
          </view>
          <view class="overview-item">
            <view class="overview-icon-wrap balance-icon">
              <text class="overview-icon-text">⚖</text>
            </view>
            <text class="overview-label">结余</text>
            <text class="overview-amount">{{ showAmount ? '¥' + formatMoney(monthBalance) : '****' }}</text>
          </view>
        </view>
      </view>

      <!-- 预算进度 -->
      <view class="budget-section" v-if="budget > 0">
        <view class="budget-info">
          <text class="budget-label">月预算 ¥{{ formatMoney(budget) }}</text>
          <text class="budget-remain" :class="{ over: budgetRemain < 0 }">
            {{ budgetRemain >= 0 ? '剩余 ¥' + formatMoney(budgetRemain) : '超支 ¥' + formatMoney(Math.abs(budgetRemain)) }}
          </text>
        </view>
        <view class="progress-bg">
          <view class="progress-bar" :style="{ width: budgetPercent + '%' }" :class="{ warning: budgetPercent > 65, over: budgetPercent > 90 }"></view>
        </view>
      </view>
    </view>

    <!-- 今日账单 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">今日账单</text>
        <view class="section-more" @click="goToBill">
          <text class="more-text">查看全部</text>
          <text class="more-arrow">›</text>
        </view>
      </view>
      
      <view class="record-list" v-if="todayRecords.length > 0">
        <view class="record-item" v-for="item in todayRecords" :key="item.id" @longpress="onRecordLongPress(item)">
          <view class="record-icon-wrap" :style="{ background: getIconBgColor(item.categoryName) }">
            <image class="record-icon-img" :src="getIconPath(item.categoryName, item.type)" mode="aspectFit"></image>
          </view>
          <view class="record-info">
            <text class="record-category">{{ item.categoryName }}</text>
            <text class="record-remark" v-if="item.remark">{{ item.remark }}</text>
            <text class="record-time" v-if="item.recordTime">{{ item.recordTime ? item.recordTime.substring(0, 5) : '' }}</text>
          </view>
          <text class="record-amount" :class="item.type === 1 ? 'expense' : 'income'">
            {{ item.type === 1 ? '-' : '+' }}¥{{ formatMoney(item.amount) }}
          </text>
        </view>
      </view>
      
      <view class="empty-state" v-else>
        <image class="empty-icon-img" src="/static/category/qita_out.png" mode="aspectFit"></image>
        <text class="empty-text">今天还没有记账哦</text>
        <view class="empty-btn" @click="goToAdd">
          <text class="empty-btn-text">记一笔</text>
        </view>
      </view>
    </view>

    <!-- 快捷记账按钮 -->
    <view class="fab-btn" @click="goToAdd">
      <text class="fab-icon">+</text>
    </view>
  </view>
</template>

<script>
import { getMonthBill, deleteRecord } from '../../api/index';
import { formatMoney, getCurrentYearMonth, getCurrentDate } from '../../utils/util';
import { getCategoryIconPath, getCategoryBgColor } from '../../utils/icon';

export default {
  data() {
    return {
      statusBarHeight: 20,
      showAmount: true,
      currentMonth: getCurrentYearMonth(),
      monthIncome: 0,
      monthExpense: 0,
      monthBalance: 0,
      budget: 0,
      budgetRemain: 0,
      todayRecords: [],
      userInfo: {}
    };
  },
  computed: {
    userName() {
      return this.userInfo.nickName || '小明';
    },
    avatarText() {
      const name = this.userName;
      return name.charAt(0);
    },
    greetingText() {
      const hour = new Date().getHours();
      if (hour < 6) return '夜深了';
      if (hour < 9) return '早上好';
      if (hour < 12) return '上午好';
      if (hour < 14) return '中午好';
      if (hour < 18) return '下午好';
      return '晚上好';
    },
    greetingEmoji() {
      const hour = new Date().getHours();
      if (hour < 6) return '🌙';
      if (hour < 9) return '🌅';
      if (hour < 12) return '☀️';
      if (hour < 14) return '🍱';
      if (hour < 18) return '🌤';
      return '🌆';
    },
    todayStr() {
      const now = new Date();
      const year = now.getFullYear();
      const month = now.getMonth() + 1;
      const day = now.getDate();
      const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];
      return `${year}年${month}月${day}日 ${weekDays[now.getDay()]}`;
    },
    budgetPercent() {
      if (this.budget <= 0) return 0;
      return Math.min((this.monthExpense / this.budget) * 100, 100);
    }
  },
  async onShow() {
    this.userInfo = uni.getStorageSync('userInfo') || {};
    try {
      await getApp().ensureLogin();
      this.loadData();
    } catch (e) {
      console.error('登录未完成', e);
    }
  },
  onLoad() {
    const sysInfo = uni.getSystemInfoSync();
    this.statusBarHeight = sysInfo.statusBarHeight || 20;
  },
  methods: {
    formatMoney,
    getIconPath(name, type) {
      return getCategoryIconPath(name, type);
    },
    getIconBgColor(name) {
      return getCategoryBgColor(name);
    },
    async loadData() {
      try {
        const res = await getMonthBill({
          yearMonth: this.currentMonth
        });
        const data = res.data;
        this.monthIncome = data.totalIncome || 0;
        this.monthExpense = data.totalExpense || 0;
        this.monthBalance = data.balance || 0;
        this.budget = data.budget || 0;
        this.budgetRemain = data.budgetRemain || 0;

        // 提取今日记录
        const today = getCurrentDate();
        this.todayRecords = [];
        if (data.dailyList) {
          for (const day of data.dailyList) {
            if (day.date === today) {
              this.todayRecords = day.records || [];
              break;
            }
          }
        }
      } catch (e) {
        console.error('加载数据失败', e);
      }
    },
    goToAdd() {
      uni.switchTab({ url: '/pages/add/add' });
    },
    goToBill() {
      uni.navigateTo({ url: '/pages/bill/bill' });
    },
    onRecordLongPress(item) {
      uni.showActionSheet({
        itemList: ['编辑', '删除'],
        success: (res) => {
          if (res.tapIndex === 0) {
            uni.navigateTo({
              url: `/pages/add/add?id=${item.id}&edit=1`
            });
          } else if (res.tapIndex === 1) {
            this.doDeleteRecord(item);
          }
        }
      });
    },
    doDeleteRecord(item) {
      uni.showModal({
        title: '提示',
        content: '确定删除这条记录吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await deleteRecord(item.id);
              uni.showToast({ title: '删除成功' });
              this.loadData();
            } catch (e) {
              console.error(e);
            }
          }
        }
      });
    }
  }
};
</script>

<style lang="scss">
.page {
  min-height: 100vh;
  background: #FAFBFE;
  padding-bottom: 120rpx;
}

/* 头部区域 */
.header {
  padding: 0 32rpx 24rpx;
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 24rpx 0 28rpx;
}

.greeting-area {
  flex: 1;
}

.greeting-text {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: #2D3142;
  margin-bottom: 8rpx;
}

.header-date {
  display: flex;
  align-items: center;
}

.date-text {
  font-size: 26rpx;
  color: #9CA3AF;
}

.date-emoji {
  font-size: 26rpx;
  margin-left: 8rpx;
}

.user-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #7B9EF5, #B8A0F5);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 16rpx rgba(123, 158, 245, 0.3);
  flex-shrink: 0;
  margin-left: 24rpx;
}

.avatar-text {
  font-size: 32rpx;
  color: #FFFFFF;
  font-weight: 600;
}

/* 本月概览卡片 - 蓝紫渐变 */
.overview-card {
  background: linear-gradient(135deg, #7B9EF5 0%, #9BB0F7 40%, #B8A0F5 100%);
  border-radius: 28rpx;
  padding: 36rpx 32rpx 40rpx;
  color: #FFFFFF;
  box-shadow: 0 12rpx 48rpx rgba(123, 158, 245, 0.35);
  position: relative;
  overflow: hidden;
}

.overview-card::before {
  content: '';
  position: absolute;
  top: -40rpx;
  right: -40rpx;
  width: 200rpx;
  height: 200rpx;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

.overview-card::after {
  content: '';
  position: absolute;
  bottom: -60rpx;
  left: -30rpx;
  width: 160rpx;
  height: 160rpx;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 50%;
}

.overview-title {
  font-size: 28rpx;
  opacity: 0.9;
  margin-bottom: 32rpx;
  display: block;
  position: relative;
  z-index: 1;
}

.overview-body {
  display: flex;
  justify-content: space-around;
  align-items: center;
  position: relative;
  z-index: 1;
}

.overview-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.overview-icon-wrap {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12rpx;
}

.overview-icon-text {
  font-size: 32rpx;
  color: #FFFFFF;
  font-weight: 600;
}

.overview-label {
  font-size: 24rpx;
  opacity: 0.85;
  margin-bottom: 8rpx;
}

.overview-amount {
  font-size: 30rpx;
  font-weight: 700;
  letter-spacing: 1rpx;
}

/* 预算进度 */
.budget-section {
  margin-top: 24rpx;
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 24rpx 28rpx;
  box-shadow: 0 2rpx 16rpx rgba(123, 158, 245, 0.08);
}

.budget-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.budget-label {
  font-size: 26rpx;
  color: #6B7280;
}

.budget-remain {
  font-size: 26rpx;
  color: #5CC9A7;
  font-weight: 500;
}

.budget-remain.over {
  color: #F5707A;
}

.progress-bg {
  height: 12rpx;
  background: #F0F1F5;
  border-radius: 6rpx;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #7B9EF5, #B8A0F5);
  border-radius: 6rpx;
  transition: width 0.3s ease;
}

.progress-bar.warning {
  background: linear-gradient(90deg, #F5C07C, #F5A05C);
}

.progress-bar.over {
  background: linear-gradient(90deg, #F5707A, #F55070);
}

/* 区块 */
.section {
  padding: 0 32rpx;
  margin-top: 36rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #2D3142;
}

.section-more {
  display: flex;
  align-items: center;
}

.more-text {
  font-size: 26rpx;
  color: #7B9EF5;
}

.more-arrow {
  font-size: 28rpx;
  color: #7B9EF5;
  margin-left: 4rpx;
}

/* 记录列表 */
.record-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.record-item {
  display: flex;
  align-items: center;
  padding: 28rpx 28rpx;
  background: #FFFFFF;
  border-radius: 20rpx;
  box-shadow: 0 2rpx 16rpx rgba(123, 158, 245, 0.06);
}

.record-icon-wrap {
  width: 84rpx;
  height: 84rpx;
  border-radius: 22rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.record-icon-img {
  width: 52rpx;
  height: 52rpx;
}

.record-info {
  flex: 1;
  overflow: hidden;
}

.record-category {
  display: block;
  font-size: 30rpx;
  color: #2D3142;
  font-weight: 600;
}

.record-remark {
  display: block;
  font-size: 24rpx;
  color: #6B7280;
  margin-top: 4rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.record-time {
  display: block;
  font-size: 22rpx;
  color: #9CA3AF;
  margin-top: 4rpx;
}

.record-amount {
  font-size: 32rpx;
  font-weight: 700;
  flex-shrink: 0;
  margin-left: 16rpx;
}

.record-amount.expense {
  color: #F5707A;
}

.record-amount.income {
  color: #5CC9A7;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 0;
  background: #FFFFFF;
  border-radius: 24rpx;
  box-shadow: 0 2rpx 16rpx rgba(123, 158, 245, 0.06);
}

.empty-icon-img {
  width: 120rpx;
  height: 120rpx;
  margin-bottom: 24rpx;
  opacity: 0.6;
}

.empty-text {
  font-size: 28rpx;
  color: #9CA3AF;
  margin-bottom: 32rpx;
}

.empty-btn {
  padding: 18rpx 56rpx;
  background: linear-gradient(135deg, #7B9EF5, #B8A0F5);
  border-radius: 40rpx;
  box-shadow: 0 6rpx 20rpx rgba(123, 158, 245, 0.3);
}

.empty-btn-text {
  font-size: 28rpx;
  color: #FFFFFF;
  font-weight: 500;
}

/* 浮动按钮 */
.fab-btn {
  position: fixed;
  right: 40rpx;
  bottom: 200rpx;
  width: 108rpx;
  height: 108rpx;
  background: linear-gradient(135deg, #7B9EF5, #B8A0F5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 32rpx rgba(123, 158, 245, 0.45);
  z-index: 100;
}

.fab-icon {
  font-size: 52rpx;
  color: #FFFFFF;
  font-weight: 300;
  line-height: 1;
}
</style>
