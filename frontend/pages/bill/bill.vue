<template>
  <view class="page">
    <!-- 顶部状态栏占位 -->
    <view :style="{ height: statusBarHeight + 'px' }"></view>
    
    <!-- 导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text>‹</text>
      </view>
      <text class="nav-title">月度账单</text>
      <view class="nav-placeholder"></view>
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

    <!-- 月度汇总 - 蓝紫渐变卡片 -->
    <view class="summary-card">
      <view class="summary-row">
        <view class="summary-col">
          <text class="summary-label">收入</text>
          <text class="summary-value">¥{{ formatMoney(totalIncome) }}</text>
        </view>
        <view class="summary-divider"></view>
        <view class="summary-col">
          <text class="summary-label">支出</text>
          <text class="summary-value">¥{{ formatMoney(totalExpense) }}</text>
        </view>
        <view class="summary-divider"></view>
        <view class="summary-col">
          <text class="summary-label">结余</text>
          <text class="summary-value">¥{{ formatMoney(balance) }}</text>
        </view>
      </view>
    </view>

    <!-- 日账单列表 -->
    <scroll-view scroll-y class="bill-scroll">
      <view class="day-group" v-for="day in dailyList" :key="day.date">
        <view class="day-header">
          <view class="day-left">
            <text class="day-date">{{ formatDateDisplay(day.date) }}</text>
            <text class="day-week">{{ day.weekDay }}</text>
          </view>
          <view class="day-right">
            <text class="day-expense" v-if="day.dayExpense > 0">支出 ¥{{ formatMoney(day.dayExpense) }}</text>
            <text class="day-income" v-if="day.dayIncome > 0">  收入 ¥{{ formatMoney(day.dayIncome) }}</text>
          </view>
        </view>
        
        <view class="record-card">
          <view class="record-item" v-for="item in day.records" :key="item.id" @longpress="onRecordLongPress(item)">
            <view class="record-icon-wrap" :style="{ background: getIconBg(item.categoryName) }">
              <image class="record-icon-img" :src="getIconPath(item.categoryName, item.type)" mode="aspectFit"></image>
            </view>
            <view class="record-info">
              <text class="record-category">{{ item.categoryName }}</text>
              <text class="record-remark" v-if="item.remark">{{ item.remark }}</text>
            </view>
            <text class="record-amount" :class="item.type === 1 ? 'expense' : 'income'">
              {{ item.type === 1 ? '-' : '+' }}¥{{ formatMoney(item.amount) }}
            </text>
          </view>
        </view>
      </view>

      <view class="empty-state" v-if="dailyList.length === 0">
        <image class="empty-icon-img" src="/static/category/qita_out.png" mode="aspectFit" style="width: 120rpx; height: 120rpx; opacity: 0.5;"></image>
        <text class="empty-text">本月暂无记录</text>
      </view>

      <view style="height: 40rpx;"></view>
    </scroll-view>
  </view>
</template>

<script>
import { getMonthBill, deleteRecord } from '../../api/index';
import { formatMoney, getCurrentYearMonth, getMonthName, getPrevMonth, getNextMonth, formatDateDisplay } from '../../utils/util';
import { getCategoryIconPath, getCategoryBgColor } from '../../utils/icon';

export default {
  data() {
    return {
      statusBarHeight: 20,
      currentMonth: getCurrentYearMonth(),
      totalIncome: 0,
      totalExpense: 0,
      balance: 0,
      dailyList: []
    };
  },
  computed: {
    monthName() {
      return getMonthName(this.currentMonth);
    }
  },
  async onLoad() {
    const sysInfo = uni.getSystemInfoSync();
    this.statusBarHeight = sysInfo.statusBarHeight || 20;
    try {
      await getApp().ensureLogin();
      this.loadData();
    } catch (e) {
      console.error('登录未完成', e);
    }
  },
  methods: {
    formatMoney,
    formatDateDisplay,
    getIconPath(name, type) {
      return getCategoryIconPath(name, type);
    },
    goBack() {
      uni.navigateBack();
    },
    getIconBg(name) {
      return getCategoryBgColor(name);
    },
    async loadData() {
      try {
        const res = await getMonthBill({
          yearMonth: this.currentMonth
        });
        const data = res.data;
        this.totalIncome = data.totalIncome || 0;
        this.totalExpense = data.totalExpense || 0;
        this.balance = data.balance || 0;
        this.dailyList = data.dailyList || [];
      } catch (e) {
        console.error('加载账单失败', e);
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
    onRecordLongPress(item) {
      uni.showActionSheet({
        itemList: ['删除'],
        success: async (res) => {
          if (res.tapIndex === 0) {
            uni.showModal({
              title: '提示',
              content: '确定删除这条记录吗？',
              success: async (modalRes) => {
                if (modalRes.confirm) {
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
      });
    }
  }
};
</script>

<style lang="scss">
.page {
  min-height: 100vh;
  background: #FAFBFE;
  display: flex;
  flex-direction: column;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 32rpx;
}

.nav-back {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 44rpx;
  color: #2D3142;
}

.nav-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #2D3142;
}

.nav-placeholder {
  width: 56rpx;
}

/* 月份选择 */
.month-selector {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8rpx 0 16rpx;
}

.month-arrow {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  color: #7B9EF5;
  font-weight: 600;
}

.month-text {
  font-size: 30rpx;
  font-weight: 600;
  color: #2D3142;
  margin: 0 32rpx;
}

/* 月度汇总 - 蓝紫渐变卡片 */
.summary-card {
  margin: 0 32rpx 24rpx;
  background: linear-gradient(135deg, #7B9EF5 0%, #9BB0F7 40%, #B8A0F5 100%);
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 8rpx 32rpx rgba(123, 158, 245, 0.25);
}

.summary-row {
  display: flex;
  align-items: center;
}

.summary-col {
  flex: 1;
  text-align: center;
}

.summary-label {
  display: block;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 8rpx;
}

.summary-value {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #FFFFFF;
}

.summary-divider {
  width: 2rpx;
  height: 56rpx;
  background: rgba(255, 255, 255, 0.3);
}

/* 日账单列表 */
.bill-scroll {
  flex: 1;
  padding: 0 32rpx;
}

.day-group {
  margin-bottom: 24rpx;
}

.day-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
}

.day-left {
  display: flex;
  align-items: center;
}

.day-date {
  font-size: 28rpx;
  font-weight: 600;
  color: #2D3142;
  margin-right: 12rpx;
}

.day-week {
  font-size: 24rpx;
  color: #9CA3AF;
}

.day-right {
  display: flex;
  align-items: center;
}

.day-expense {
  font-size: 24rpx;
  color: #F5707A;
}

.day-income {
  font-size: 24rpx;
  color: #5CC9A7;
}

/* 记录卡片 */
.record-card {
  background: #FFFFFF;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 16rpx rgba(123, 158, 245, 0.08);
}

.record-item {
  display: flex;
  align-items: center;
  padding: 24rpx 28rpx;
  border-bottom: 1rpx solid #F3F4F8;
}

.record-item:last-child {
  border-bottom: none;
}

.record-icon-wrap {
  width: 72rpx;
  height: 72rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.record-icon-img {
  width: 44rpx;
  height: 44rpx;
}

.record-info {
  flex: 1;
  overflow: hidden;
}

.record-category {
  display: block;
  font-size: 28rpx;
  color: #2D3142;
  font-weight: 500;
}

.record-remark {
  display: block;
  font-size: 24rpx;
  color: #9CA3AF;
  margin-top: 4rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.record-amount {
  font-size: 30rpx;
  font-weight: 600;
  flex-shrink: 0;
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
  padding: 120rpx 0;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 24rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #9CA3AF;
}
</style>
