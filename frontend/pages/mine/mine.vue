<template>
  <view class="page">
    <!-- 蓝紫渐变头部 + 波浪底边 -->
    <view class="profile-header">
      <view :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="profile-content">
        <view class="avatar-circle">
          <text class="avatar-letter">{{ avatarText }}</text>
        </view>
        <text class="profile-name">{{ userInfo.nickName || '记账用户' }}</text>
        <text class="profile-sign">记录生活，理清收支 ✨</text>
      </view>
      <!-- 波浪底边 -->
      <view class="wave-bottom">
        <view class="wave-shape"></view>
      </view>
    </view>

    <!-- 年度总结卡片 -->
    <view class="year-card">
      <text class="year-title">{{ currentYear }}年度总结</text>
      <view class="year-data">
        <view class="year-item">
          <text class="year-label">总收入</text>
          <text class="year-value income">¥{{ formatMoney(yearIncome) }}</text>
        </view>
        <view class="year-divider"></view>
        <view class="year-item">
          <text class="year-label">总支出</text>
          <text class="year-value expense">¥{{ formatMoney(yearExpense) }}</text>
        </view>
        <view class="year-divider"></view>
        <view class="year-item">
          <text class="year-label">结余</text>
          <text class="year-value">¥{{ formatMoney(yearBalance) }}</text>
        </view>
      </view>
    </view>

    <!-- 功能菜单 -->
    <view class="menu-card">
      <view class="menu-item" @click="goToBill">
        <view class="menu-left">
          <text class="menu-icon">📋</text>
          <text class="menu-name">月度账单</text>
        </view>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goToBudget">
        <view class="menu-left">
          <text class="menu-icon">💰</text>
          <text class="menu-name">预算管理</text>
        </view>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goToBooks">
        <view class="menu-left">
          <text class="menu-icon">📒</text>
          <text class="menu-name">我的账本</text>
        </view>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="exportData">
        <view class="menu-left">
          <text class="menu-icon">📤</text>
          <text class="menu-name">数据导出</text>
        </view>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="showAbout">
        <view class="menu-left">
          <text class="menu-icon">💡</text>
          <text class="menu-name">关于我们</text>
        </view>
        <text class="menu-arrow">›</text>
      </view>
    </view>

    <!-- 退出登录 -->
    <view class="logout-btn" @click="logout">
      <text class="logout-text">退出登录</text>
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
  async onShow() {
    try {
      await getApp().ensureLogin();
      this.loadUserInfo();
      this.loadYearSummary();
    } catch (e) {
      console.error('登录未完成', e);
    }
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
    },
    logout() {
      uni.showModal({
        title: '提示',
        content: '确定退出登录吗？',
        success: (res) => {
          if (res.confirm) {
            uni.removeStorageSync('token');
            uni.removeStorageSync('userInfo');
            uni.showToast({ title: '已退出' });
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

/* 蓝紫渐变头部 */
.profile-header {
  background: linear-gradient(135deg, #7B9EF5 0%, #9BB0F7 40%, #B8A0F5 100%);
  padding-bottom: 60rpx;
  position: relative;
}

.profile-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40rpx 0 48rpx;
}

.avatar-circle {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.25);
  border: 4rpx solid rgba(255, 255, 255, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20rpx;
}

.avatar-letter {
  font-size: 48rpx;
  color: #FFFFFF;
  font-weight: 700;
}

.profile-name {
  font-size: 36rpx;
  color: #FFFFFF;
  font-weight: 600;
  margin-bottom: 8rpx;
}

.profile-sign {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
}

/* 波浪底边 */
.wave-bottom {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 40rpx;
  overflow: hidden;
}

.wave-shape {
  width: 100%;
  height: 100%;
  background: #FAFBFE;
  border-radius: 50% 50% 0 0;
}

/* 年度总结卡片 */
.year-card {
  margin: -20rpx 32rpx 24rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 28rpx 32rpx;
  box-shadow: 0 4rpx 24rpx rgba(123, 158, 245, 0.12);
  position: relative;
  z-index: 2;
}

.year-title {
  display: block;
  font-size: 28rpx;
  color: #9CA3AF;
  margin-bottom: 20rpx;
  text-align: center;
}

.year-data {
  display: flex;
  align-items: center;
}

.year-item {
  flex: 1;
  text-align: center;
}

.year-label {
  display: block;
  font-size: 24rpx;
  color: #9CA3AF;
  margin-bottom: 8rpx;
}

.year-value {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #2D3142;
}

.year-value.income {
  color: #5CC9A7;
}

.year-value.expense {
  color: #F5707A;
}

.year-divider {
  width: 2rpx;
  height: 56rpx;
  background: #EEEEF3;
}

/* 功能菜单 */
.menu-card {
  margin: 0 32rpx 24rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 16rpx rgba(123, 158, 245, 0.08);
}

.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 32rpx;
  border-bottom: 1rpx solid #F3F4F8;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:active {
  background: #F8F9FE;
}

.menu-left {
  display: flex;
  align-items: center;
}

.menu-icon {
  font-size: 36rpx;
  margin-right: 20rpx;
}

.menu-name {
  font-size: 30rpx;
  color: #2D3142;
  font-weight: 500;
}

.menu-arrow {
  font-size: 32rpx;
  color: #D1D5DB;
}

/* 退出登录 */
.logout-btn {
  margin: 24rpx 32rpx;
  padding: 28rpx 0;
  text-align: center;
  background: #FFFFFF;
  border-radius: 24rpx;
  box-shadow: 0 2rpx 16rpx rgba(123, 158, 245, 0.08);
}

.logout-btn:active {
  background: #FFF0F0;
}

.logout-text {
  font-size: 30rpx;
  color: #F5707A;
  font-weight: 500;
}
</style>
