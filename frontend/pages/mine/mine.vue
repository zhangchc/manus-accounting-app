<template>
  <view class="page">
    <!-- 顶部状态栏占位 -->
    <view :style="{ height: statusBarHeight + 'px' }"></view>

    <!-- 用户信息卡片 -->
    <view class="user-card">
      <view class="user-avatar">
        <text class="avatar-text">{{ avatarText }}</text>
      </view>
      <view class="user-info">
        <text class="user-name">{{ userInfo.nickName || '记账用户' }}</text>
        <text class="user-desc">记录每一笔，生活更美好</text>
      </view>
    </view>

    <!-- 年度汇总 -->
    <view class="year-summary">
      <view class="year-header">
        <text class="year-title">{{ currentYear }}年汇总</text>
      </view>
      <view class="year-body">
        <view class="year-item">
          <text class="year-amount expense">¥{{ formatMoney(yearExpense) }}</text>
          <text class="year-label">总支出</text>
        </view>
        <view class="year-divider"></view>
        <view class="year-item">
          <text class="year-amount income">¥{{ formatMoney(yearIncome) }}</text>
          <text class="year-label">总收入</text>
        </view>
        <view class="year-divider"></view>
        <view class="year-item">
          <text class="year-amount">¥{{ formatMoney(yearBalance) }}</text>
          <text class="year-label">结余</text>
        </view>
      </view>
    </view>

    <!-- 功能菜单 -->
    <view class="menu-section">
      <view class="menu-card">
        <view class="menu-item" @click="goToBill">
          <text class="menu-icon">📋</text>
          <text class="menu-text">月度账单</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @click="goToBudget">
          <text class="menu-icon">🎯</text>
          <text class="menu-text">预算管理</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @click="goToBooks">
          <text class="menu-icon">📒</text>
          <text class="menu-text">我的账本</text>
          <text class="menu-arrow">›</text>
        </view>
      </view>

      <view class="menu-card">
        <view class="menu-item" @click="exportData">
          <text class="menu-icon">📤</text>
          <text class="menu-text">数据导出</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @click="showAbout">
          <text class="menu-icon">💡</text>
          <text class="menu-text">关于轻记账</text>
          <text class="menu-arrow">›</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getUserInfo, getYearSummary } from '../../api/index';
import { formatMoney } from '../../utils/util';

export default {
  data() {
    return {
      statusBarHeight: 20,
      userInfo: {},
      currentYear: new Date().getFullYear().toString(),
      yearIncome: 0,
      yearExpense: 0,
      yearBalance: 0
    };
  },
  computed: {
    avatarText() {
      const name = this.userInfo.nickName || '用户';
      return name.charAt(0);
    }
  },
  onLoad() {
    const sysInfo = uni.getSystemInfoSync();
    this.statusBarHeight = sysInfo.statusBarHeight || 20;
  },
  onShow() {
    this.loadUserInfo();
    this.loadYearSummary();
  },
  methods: {
    formatMoney,
    async loadUserInfo() {
      try {
        const res = await getUserInfo();
        this.userInfo = res.data || {};
      } catch (e) {
        console.error('加载用户信息失败', e);
      }
    },
    async loadYearSummary() {
      try {
        const res = await getYearSummary({ year: this.currentYear });
        const data = res.data;
        this.yearIncome = data.totalIncome || 0;
        this.yearExpense = data.totalExpense || 0;
        this.yearBalance = data.balance || 0;
      } catch (e) {
        console.error('加载年度汇总失败', e);
      }
    },
    goToBill() {
      uni.navigateTo({ url: '/pages/bill/bill' });
    },
    goToBudget() {
      uni.navigateTo({ url: '/pages/budget/budget' });
    },
    goToBooks() {
      uni.showToast({ title: '功能开发中', icon: 'none' });
    },
    exportData() {
      uni.showToast({ title: '功能开发中', icon: 'none' });
    },
    showAbout() {
      uni.showModal({
        title: '关于轻记账',
        content: '轻记账 v1.0.0\n\n一款简约清爽的记账小程序\n记录每一笔收支，让生活更有规划',
        showCancel: false,
        confirmText: '知道了'
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

/* 用户信息卡片 */
.user-card {
  display: flex;
  align-items: center;
  padding: 48rpx 32rpx 32rpx;
}

.user-avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #7C9FF5, #B8A0F5);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 28rpx;
  box-shadow: 0 8rpx 24rpx rgba(124, 159, 245, 0.3);
}

.avatar-text {
  font-size: 48rpx;
  color: #FFFFFF;
  font-weight: 600;
}

.user-info {
  flex: 1;
}

.user-name {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: #2D3142;
  margin-bottom: 8rpx;
}

.user-desc {
  display: block;
  font-size: 26rpx;
  color: #9CA3AF;
}

/* 年度汇总 */
.year-summary {
  margin: 16rpx 32rpx;
  background: linear-gradient(135deg, #7C9FF5 0%, #A8C0F7 50%, #B8A0F5 100%);
  border-radius: 24rpx;
  padding: 32rpx;
  color: #FFFFFF;
  box-shadow: 0 8rpx 40rpx rgba(124, 159, 245, 0.3);
}

.year-header {
  margin-bottom: 28rpx;
}

.year-title {
  font-size: 28rpx;
  opacity: 0.9;
}

.year-body {
  display: flex;
  align-items: center;
}

.year-item {
  flex: 1;
  text-align: center;
}

.year-amount {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  margin-bottom: 8rpx;
}

.year-label {
  display: block;
  font-size: 24rpx;
  opacity: 0.8;
}

.year-divider {
  width: 1rpx;
  height: 60rpx;
  background: rgba(255, 255, 255, 0.3);
}

/* 功能菜单 */
.menu-section {
  padding: 16rpx 32rpx;
}

.menu-card {
  background: #FFFFFF;
  border-radius: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(124, 159, 245, 0.08);
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1rpx solid #F3F4F8;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-icon {
  font-size: 36rpx;
  margin-right: 24rpx;
}

.menu-text {
  flex: 1;
  font-size: 30rpx;
  color: #2D3142;
}

.menu-arrow {
  font-size: 32rpx;
  color: #D1D5DB;
}
</style>
