<template>
  <view class="page">
    <!-- 顶部状态栏占位 -->
    <view :style="{ height: statusBarHeight + 'px' }"></view>
    
    <!-- 头部区域 -->
    <view class="header">
      <view class="header-top">
        <view class="greeting">
          <text class="greeting-text">{{ greetingText }}</text>
          <text class="greeting-emoji">{{ greetingEmoji }}</text>
        </view>
        <view class="header-date">{{ todayStr }}</view>
      </view>
      
      <!-- 总览卡片 -->
      <view class="overview-card">
        <view class="overview-header">
          <text class="overview-month" @click="showMonthPicker = true">{{ monthName }}</text>
          <text class="overview-eye" @click="toggleAmount">{{ showAmount ? '👁' : '👁‍🗨' }}</text>
        </view>
        <view class="overview-body">
          <view class="overview-item">
            <text class="overview-label">月支出</text>
            <text class="overview-amount expense">{{ showAmount ? formatMoney(monthExpense) : '****' }}</text>
          </view>
          <view class="overview-divider"></view>
          <view class="overview-item">
            <text class="overview-label">月收入</text>
            <text class="overview-amount income">{{ showAmount ? formatMoney(monthIncome) : '****' }}</text>
          </view>
          <view class="overview-divider"></view>
          <view class="overview-item">
            <text class="overview-label">月结余</text>
            <text class="overview-amount">{{ showAmount ? formatMoney(monthBalance) : '****' }}</text>
          </view>
        </view>
        <!-- 预算进度 -->
        <view class="budget-bar" v-if="budget > 0">
          <view class="budget-info">
            <text class="budget-text">月预算 ¥{{ formatMoney(budget) }}</text>
            <text class="budget-remain" :class="{ 'over': budgetRemain < 0 }">
              {{ budgetRemain >= 0 ? '剩余 ¥' + formatMoney(budgetRemain) : '超支 ¥' + formatMoney(Math.abs(budgetRemain)) }}
            </text>
          </view>
          <view class="progress-bg">
            <view class="progress-bar" :style="{ width: budgetPercent + '%' }" :class="{ 'over': budgetPercent > 100 }"></view>
          </view>
        </view>
      </view>
    </view>

    <!-- 今日账单 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">今日账单</text>
        <text class="section-more" @click="goToBill">查看全部</text>
      </view>
      
      <view class="record-list" v-if="todayRecords.length > 0">
        <view class="record-item" v-for="item in todayRecords" :key="item.id" @longpress="onRecordLongPress(item)">
          <view class="record-icon">{{ item.categoryIcon }}</view>
          <view class="record-info">
            <text class="record-category">{{ item.categoryName }}</text>
            <text class="record-remark" v-if="item.remark">{{ item.remark }}</text>
          </view>
          <text class="record-amount" :class="item.type === 1 ? 'expense' : 'income'">
            {{ item.type === 1 ? '-' : '+' }}{{ formatMoney(item.amount) }}
          </text>
        </view>
      </view>
      
      <view class="empty-state" v-else>
        <text class="empty-icon">📝</text>
        <text class="empty-text">今天还没有记账哦</text>
        <view class="empty-btn" @click="goToAdd">记一笔</view>
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
import { formatMoney, getCurrentYearMonth, getCurrentDate, getMonthName } from '../../utils/util';

export default {
  data() {
    return {
      statusBarHeight: 20,
      showAmount: true,
      showMonthPicker: false,
      currentMonth: getCurrentYearMonth(),
      monthIncome: 0,
      monthExpense: 0,
      monthBalance: 0,
      budget: 0,
      budgetRemain: 0,
      todayRecords: [],
      allRecords: []
    };
  },
  computed: {
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
      const month = now.getMonth() + 1;
      const day = now.getDate();
      const weekDays = ['日', '一', '二', '三', '四', '五', '六'];
      return `${month}月${day}日 周${weekDays[now.getDay()]}`;
    },
    monthName() {
      return getMonthName(this.currentMonth);
    },
    budgetPercent() {
      if (this.budget <= 0) return 0;
      return Math.min((this.monthExpense / this.budget) * 100, 100);
    }
  },
  onShow() {
    this.loadData();
  },
  onLoad() {
    const sysInfo = uni.getSystemInfoSync();
    this.statusBarHeight = sysInfo.statusBarHeight || 20;
  },
  methods: {
    formatMoney,
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
    toggleAmount() {
      this.showAmount = !this.showAmount;
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
  background: #F8F9FE;
  padding-bottom: 120rpx;
}

.header {
  padding: 0 32rpx 32rpx;
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 0;
}

.greeting {
  display: flex;
  align-items: center;
}

.greeting-text {
  font-size: 36rpx;
  font-weight: 600;
  color: #2D3142;
}

.greeting-emoji {
  font-size: 36rpx;
  margin-left: 12rpx;
}

.header-date {
  font-size: 26rpx;
  color: #9CA3AF;
}

/* 总览卡片 */
.overview-card {
  background: linear-gradient(135deg, #7C9FF5 0%, #A8C0F7 50%, #B8A0F5 100%);
  border-radius: 24rpx;
  padding: 36rpx;
  color: #FFFFFF;
  box-shadow: 0 8rpx 40rpx rgba(124, 159, 245, 0.3);
}

.overview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32rpx;
}

.overview-month {
  font-size: 28rpx;
  opacity: 0.9;
}

.overview-eye {
  font-size: 32rpx;
  opacity: 0.8;
}

.overview-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.overview-item {
  flex: 1;
  text-align: center;
}

.overview-label {
  display: block;
  font-size: 24rpx;
  opacity: 0.8;
  margin-bottom: 12rpx;
}

.overview-amount {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
}

.overview-divider {
  width: 1rpx;
  height: 60rpx;
  background: rgba(255, 255, 255, 0.3);
}

/* 预算进度 */
.budget-bar {
  margin-top: 32rpx;
  padding-top: 24rpx;
  border-top: 1rpx solid rgba(255, 255, 255, 0.2);
}

.budget-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.budget-text {
  font-size: 24rpx;
  opacity: 0.8;
}

.budget-remain {
  font-size: 24rpx;
  opacity: 0.9;
}

.budget-remain.over {
  color: #FFD166;
}

.progress-bg {
  height: 8rpx;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 4rpx;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: #FFFFFF;
  border-radius: 4rpx;
  transition: width 0.3s ease;
}

.progress-bar.over {
  background: #FFD166;
}

/* 区块 */
.section {
  padding: 0 32rpx;
  margin-top: 32rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #2D3142;
}

.section-more {
  font-size: 26rpx;
  color: #7C9FF5;
}

/* 记录列表 */
.record-list {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 8rpx 0;
  box-shadow: 0 2rpx 12rpx rgba(124, 159, 245, 0.08);
}

.record-item {
  display: flex;
  align-items: center;
  padding: 28rpx 32rpx;
  border-bottom: 1rpx solid #F3F4F8;
}

.record-item:last-child {
  border-bottom: none;
}

.record-icon {
  width: 80rpx;
  height: 80rpx;
  background: #F8F9FE;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.record-info {
  flex: 1;
  overflow: hidden;
}

.record-category {
  display: block;
  font-size: 30rpx;
  color: #2D3142;
  font-weight: 500;
}

.record-remark {
  display: block;
  font-size: 24rpx;
  color: #9CA3AF;
  margin-top: 6rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.record-amount {
  font-size: 32rpx;
  font-weight: 600;
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
  box-shadow: 0 2rpx 12rpx rgba(124, 159, 245, 0.08);
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 24rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #9CA3AF;
  margin-bottom: 32rpx;
}

.empty-btn {
  padding: 16rpx 48rpx;
  background: linear-gradient(135deg, #7C9FF5, #A8C0F7);
  color: #FFFFFF;
  border-radius: 40rpx;
  font-size: 28rpx;
}

/* 浮动按钮 */
.fab-btn {
  position: fixed;
  right: 40rpx;
  bottom: 200rpx;
  width: 100rpx;
  height: 100rpx;
  background: linear-gradient(135deg, #7C9FF5, #B8A0F5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 32rpx rgba(124, 159, 245, 0.4);
  z-index: 100;
}

.fab-icon {
  font-size: 48rpx;
  color: #FFFFFF;
  font-weight: 300;
}
</style>
