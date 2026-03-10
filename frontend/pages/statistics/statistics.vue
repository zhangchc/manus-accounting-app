<template>
  <view class="page">
    <!-- 顶部状态栏占位 -->
    <view :style="{ height: statusBarHeight + 'px' }"></view>
    
    <!-- 导航栏 -->
    <view class="nav-bar">
      <text class="nav-title">统计分析</text>
    </view>

    <!-- 月份选择 -->
    <view class="month-selector">
      <view class="month-arrow" @click="prevMonth">
        <text>‹</text>
      </view>
      <text class="month-text">{{ monthName }}</text>
      <view class="month-arrow" @click="nextMonth">
        <text>›</text>
      </view>
    </view>

    <!-- 收支类型切换 -->
    <view class="type-tabs">
      <view class="tab-item" :class="{ active: type === 1 }" @click="type = 1; loadData()">
        <text>支出</text>
      </view>
      <view class="tab-item" :class="{ active: type === 2 }" @click="type = 2; loadData()">
        <text>收入</text>
      </view>
    </view>

    <!-- 汇总数据 -->
    <view class="summary-card">
      <view class="summary-item">
        <text class="summary-label">{{ type === 1 ? '总支出' : '总收入' }}</text>
        <text class="summary-amount">¥{{ formatMoney(type === 1 ? totalExpense : totalIncome) }}</text>
      </view>
      <view class="summary-item">
        <text class="summary-label">{{ type === 1 ? '日均支出' : '日均收入' }}</text>
        <text class="summary-amount sub">¥{{ formatMoney(dailyAvg) }}</text>
      </view>
    </view>

    <!-- 分类排行 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">分类排行</text>
      </view>
      
      <view class="rank-list" v-if="categoryList.length > 0">
        <view class="rank-item" v-for="(item, index) in categoryList" :key="item.categoryId">
          <view class="rank-left">
            <text class="rank-icon">{{ item.categoryIcon }}</text>
            <view class="rank-info">
              <view class="rank-name-row">
                <text class="rank-name">{{ item.categoryName }}</text>
                <text class="rank-percent">{{ item.percent }}</text>
              </view>
              <view class="rank-bar-bg">
                <view class="rank-bar" :style="{ width: item.percent, background: getBarColor(index) }"></view>
              </view>
            </view>
          </view>
          <text class="rank-amount">¥{{ formatMoney(item.amount) }}</text>
        </view>
      </view>

      <view class="empty-state" v-else>
        <text class="empty-icon">📊</text>
        <text class="empty-text">暂无数据</text>
      </view>
    </view>

    <!-- 每日趋势 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">每日趋势</text>
      </view>
      <view class="trend-chart">
        <view class="chart-bars">
          <view class="chart-bar-wrapper" v-for="(item, index) in trendList" :key="index">
            <view class="chart-bar" :style="{ height: getBarHeight(item.amount) + 'rpx', background: type === 1 ? '#F5707A' : '#5CC9A7' }"></view>
            <text class="chart-label" v-if="(index + 1) % 5 === 0 || index === 0">{{ index + 1 }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getStatistics } from '../../api/index';
import { formatMoney, getCurrentYearMonth, getMonthName, getPrevMonth, getNextMonth } from '../../utils/util';

export default {
  data() {
    return {
      statusBarHeight: 20,
      currentMonth: getCurrentYearMonth(),
      type: 1,
      totalIncome: 0,
      totalExpense: 0,
      categoryList: [],
      trendList: [],
      maxTrendAmount: 0
    };
  },
  computed: {
    monthName() {
      return getMonthName(this.currentMonth);
    },
    dailyAvg() {
      const total = this.type === 1 ? this.totalExpense : this.totalIncome;
      const now = new Date();
      const parts = this.currentMonth.split('-');
      let days;
      if (parseInt(parts[0]) === now.getFullYear() && parseInt(parts[1]) === now.getMonth() + 1) {
        days = now.getDate();
      } else {
        days = new Date(parseInt(parts[0]), parseInt(parts[1]), 0).getDate();
      }
      return days > 0 ? (total / days) : 0;
    }
  },
  onLoad() {
    const sysInfo = uni.getSystemInfoSync();
    this.statusBarHeight = sysInfo.statusBarHeight || 20;
  },
  onShow() {
    this.loadData();
  },
  methods: {
    formatMoney,
    async loadData() {
      try {
        const res = await getStatistics({
          yearMonth: this.currentMonth,
          type: this.type
        });
        const data = res.data;
        this.totalIncome = data.totalIncome || 0;
        this.totalExpense = data.totalExpense || 0;
        this.categoryList = data.categoryList || [];
        this.trendList = data.trendList || [];
        
        this.maxTrendAmount = 0;
        for (const item of this.trendList) {
          if (item.amount > this.maxTrendAmount) {
            this.maxTrendAmount = item.amount;
          }
        }
      } catch (e) {
        console.error('加载统计数据失败', e);
      }
    },
    prevMonth() {
      this.currentMonth = getPrevMonth(this.currentMonth);
      this.loadData();
    },
    nextMonth() {
      this.currentMonth = getNextMonth(this.currentMonth);
      this.loadData();
    },
    getBarColor(index) {
      const colors = ['#7C9FF5', '#F5A0B5', '#F5C07C', '#8DD5B0', '#B8A0F5', '#5CC9A7', '#F5707A', '#A8C0F7'];
      return colors[index % colors.length];
    },
    getBarHeight(amount) {
      if (this.maxTrendAmount <= 0) return 4;
      return Math.max(4, (amount / this.maxTrendAmount) * 120);
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

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 88rpx;
}

.nav-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #2D3142;
}

/* 月份选择 */
.month-selector {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16rpx 0;
}

.month-arrow {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  color: #7C9FF5;
}

.month-text {
  font-size: 30rpx;
  font-weight: 600;
  color: #2D3142;
  margin: 0 32rpx;
}

/* 类型切换 */
.type-tabs {
  display: flex;
  justify-content: center;
  margin: 16rpx 32rpx;
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 6rpx;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 18rpx 0;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #9CA3AF;
}

.tab-item.active {
  background: #7C9FF5;
  color: #FFFFFF;
  font-weight: 600;
}

/* 汇总卡片 */
.summary-card {
  display: flex;
  margin: 24rpx 32rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 2rpx 12rpx rgba(124, 159, 245, 0.08);
}

.summary-item {
  flex: 1;
  text-align: center;
}

.summary-label {
  display: block;
  font-size: 24rpx;
  color: #9CA3AF;
  margin-bottom: 12rpx;
}

.summary-amount {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: #2D3142;
}

.summary-amount.sub {
  font-size: 30rpx;
  font-weight: 600;
}

/* 区块 */
.section {
  margin: 32rpx 32rpx;
}

.section-header {
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #2D3142;
}

/* 排行列表 */
.rank-list {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 16rpx 32rpx;
  box-shadow: 0 2rpx 12rpx rgba(124, 159, 245, 0.08);
}

.rank-item {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #F3F4F8;
}

.rank-item:last-child {
  border-bottom: none;
}

.rank-left {
  flex: 1;
  display: flex;
  align-items: center;
}

.rank-icon {
  font-size: 36rpx;
  margin-right: 20rpx;
  width: 48rpx;
  text-align: center;
}

.rank-info {
  flex: 1;
}

.rank-name-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10rpx;
}

.rank-name {
  font-size: 28rpx;
  color: #2D3142;
  font-weight: 500;
}

.rank-percent {
  font-size: 24rpx;
  color: #9CA3AF;
}

.rank-bar-bg {
  height: 8rpx;
  background: #F3F4F8;
  border-radius: 4rpx;
  overflow: hidden;
}

.rank-bar {
  height: 100%;
  border-radius: 4rpx;
  transition: width 0.3s ease;
}

.rank-amount {
  font-size: 30rpx;
  font-weight: 600;
  color: #2D3142;
  margin-left: 24rpx;
  flex-shrink: 0;
}

/* 趋势图 */
.trend-chart {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 2rpx 12rpx rgba(124, 159, 245, 0.08);
}

.chart-bars {
  display: flex;
  align-items: flex-end;
  height: 180rpx;
  padding-top: 20rpx;
}

.chart-bar-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  height: 100%;
}

.chart-bar {
  width: 60%;
  min-height: 4rpx;
  border-radius: 4rpx 4rpx 0 0;
  transition: height 0.3s ease;
}

.chart-label {
  font-size: 18rpx;
  color: #9CA3AF;
  margin-top: 8rpx;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 0;
  background: #FFFFFF;
  border-radius: 24rpx;
}

.empty-icon {
  font-size: 64rpx;
  margin-bottom: 16rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #9CA3AF;
}
</style>
