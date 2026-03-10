<template>
  <view class="page">
    <!-- 顶部状态栏占位 -->
    <view :style="{ height: statusBarHeight + 'px' }"></view>
    
    <!-- 导航标题 -->
    <view class="nav-bar">
      <text class="nav-title">统计分析</text>
    </view>

    <!-- 月份选择器 -->
    <view class="month-selector">
      <view class="month-arrow" @click="prevMonth">
        <text>‹</text>
      </view>
      <text class="month-text">{{ monthName }}</text>
      <view class="month-arrow" @click="nextMonth">
        <text>›</text>
      </view>
    </view>

    <!-- 收支类型切换 - 蓝紫渐变胶囊 -->
    <view class="type-switch">
      <view class="type-item" :class="{ active: type === 1 }" @click="type = 1; loadData()">
        <text>支出</text>
      </view>
      <view class="type-item" :class="{ active: type === 2 }" @click="type = 2; loadData()">
        <text>收入</text>
      </view>
    </view>

    <scroll-view scroll-y class="content-scroll">
      <!-- 总额汇总卡片 - 带蓝紫渐变顶部装饰线 -->
      <view class="total-card">
        <view class="total-accent"></view>
        <view class="total-body">
          <text class="total-label">本月总{{ type === 1 ? '支出' : '收入' }}</text>
          <text class="total-amount">¥{{ formatMoney(type === 1 ? totalExpense : totalIncome) }}</text>
          <view class="total-sub">
            <text class="total-sub-text">共 {{ categoryList.length }} 个分类</text>
            <text class="total-sub-text">日均 ¥{{ formatMoney(dailyAvg) }}</text>
          </view>
        </view>
      </view>

      <!-- 分类占比 - 环形饼状图 -->
      <view class="chart-card" v-if="categoryList.length > 0">
        <text class="chart-title">分类占比</text>
        
        <view class="pie-area">
          <canvas canvas-id="pieChart" class="pie-canvas" :style="{ width: '400rpx', height: '400rpx' }"></canvas>
          <view class="pie-center">
            <text class="pie-center-amount">¥{{ formatMoney(type === 1 ? totalExpense : totalIncome) }}</text>
            <text class="pie-center-label">{{ categoryList.length }}个分类</text>
          </view>
        </view>

        <!-- 图例 2列 -->
        <view class="legend-grid">
          <view class="legend-item" v-for="(item, index) in categoryList.slice(0, 6)" :key="index">
            <view class="legend-dot" :style="{ background: pieColors[index] }"></view>
            <text class="legend-name">{{ item.categoryName }}</text>
            <text class="legend-percent">{{ item.percent }}</text>
            <text class="legend-amount">¥{{ formatMoney(item.amount) }}</text>
          </view>
        </view>
      </view>

      <!-- 每日趋势 - 蓝紫渐变柱状图 -->
      <view class="chart-card" v-if="trendList.length > 0">
        <text class="chart-title">每日趋势</text>
        <view class="bar-chart">
          <view class="bar-item" v-for="(item, index) in trendList" :key="index">
            <view class="bar-value-wrap">
              <text class="bar-value" v-if="item.amount > 0">{{ item.amount > 999 ? (item.amount / 1000).toFixed(1) + 'k' : Math.round(item.amount) }}</text>
            </view>
            <view class="bar-col-wrap">
              <view class="bar-col" :style="{ height: getBarHeight(item.amount) + 'rpx' }" :class="index % 2 === 0 ? 'bar-blue' : 'bar-purple'"></view>
            </view>
            <text class="bar-label" v-if="(index + 1) % 5 === 0 || index === 0">{{ index + 1 }}</text>
            <text class="bar-label" v-else></text>
          </view>
        </view>
      </view>

      <!-- 分类排行 -->
      <view class="chart-card" v-if="categoryList.length > 0">
        <text class="chart-title">分类排行</text>
        <view class="rank-list">
          <view class="rank-item" v-for="(item, index) in categoryList" :key="item.categoryId">
            <view class="rank-left">
              <view class="rank-icon-wrap" :style="{ background: getIconBgColor(item.categoryName) }">
                <image class="rank-icon-img" :src="getIconPath(item.categoryName, type)" mode="aspectFit"></image>
              </view>
              <view class="rank-info">
                <view class="rank-name-row">
                  <text class="rank-name">{{ item.categoryName }}</text>
                  <text class="rank-percent-text">{{ item.percent }}</text>
                </view>
                <view class="rank-bar-bg">
                  <view class="rank-bar" :style="{ width: item.percent, background: pieColors[index % pieColors.length] }"></view>
                </view>
              </view>
            </view>
            <text class="rank-amount">¥{{ formatMoney(item.amount) }}</text>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="categoryList.length === 0">
        <image class="empty-icon-img" src="/static/category/qita_out.png" mode="aspectFit" style="width: 120rpx; height: 120rpx; opacity: 0.5;"></image>
        <text class="empty-text">本月暂无{{ type === 1 ? '支出' : '收入' }}记录</text>
      </view>

      <view style="height: 120rpx;"></view>
    </scroll-view>
  </view>
</template>

<script>
import { getStatistics } from '../../api/index';
import { formatMoney, getCurrentYearMonth, getMonthName, getPrevMonth, getNextMonth } from '../../utils/util';
import { getCategoryIconPath, getCategoryBgColor } from '../../utils/icon';

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
      maxTrendAmount: 0,
      pieColors: ['#F5707A', '#7B9EF5', '#5CC9A7', '#B8A0F5', '#F5C07C', '#F5A0B5']
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
  async onShow() {
    try {
      await getApp().ensureLogin();
      this.loadData();
    } catch (e) {
      console.error('登录未完成', e);
    }
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
        
        this.$nextTick(() => {
          if (this.categoryList.length > 0) {
            this.drawPieChart();
          }
        });
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
    drawPieChart() {
      const ctx = uni.createCanvasContext('pieChart', this);
      const centerX = 100;
      const centerY = 100;
      const radius = 85;
      const innerRadius = 55;
      
      // 清空画布
      ctx.clearRect(0, 0, 200, 200);
      
      let startAngle = -Math.PI / 2;
      const total = this.type === 1 ? this.totalExpense : this.totalIncome;
      
      for (let i = 0; i < this.categoryList.length && i < 6; i++) {
        const item = this.categoryList[i];
        const percentNum = parseFloat(item.percent) || 0;
        const sliceAngle = (percentNum / 100) * 2 * Math.PI;
        
        if (sliceAngle <= 0) continue;
        
        ctx.beginPath();
        ctx.moveTo(
          centerX + innerRadius * Math.cos(startAngle),
          centerY + innerRadius * Math.sin(startAngle)
        );
        ctx.arc(centerX, centerY, radius, startAngle, startAngle + sliceAngle);
        ctx.arc(centerX, centerY, innerRadius, startAngle + sliceAngle, startAngle, true);
        ctx.closePath();
        ctx.setFillStyle(this.pieColors[i]);
        ctx.fill();
        
        startAngle += sliceAngle;
      }
      
      ctx.draw();
    },
    getBarHeight(amount) {
      if (this.maxTrendAmount <= 0) return 4;
      return Math.max(4, (amount / this.maxTrendAmount) * 200);
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
  justify-content: center;
  height: 88rpx;
}

.nav-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #2D3142;
}

/* 月份选择器 */
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

/* 类型切换 - 蓝紫渐变胶囊 */
.type-switch {
  display: flex;
  margin: 0 32rpx 20rpx;
  background: #F0F1F5;
  border-radius: 24rpx;
  padding: 6rpx;
}

.type-item {
  flex: 1;
  text-align: center;
  padding: 18rpx 0;
  border-radius: 20rpx;
  font-size: 28rpx;
  color: #9CA3AF;
  font-weight: 500;
}

.type-item.active {
  background: linear-gradient(135deg, #7B9EF5, #B8A0F5);
  color: #FFFFFF;
  font-weight: 600;
  box-shadow: 0 4rpx 16rpx rgba(123, 158, 245, 0.3);
}

.content-scroll {
  flex: 1;
}

/* 总额卡片 - 带蓝紫渐变顶部装饰线 */
.total-card {
  margin: 0 32rpx 24rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 16rpx rgba(123, 158, 245, 0.08);
}

.total-accent {
  height: 6rpx;
  background: linear-gradient(90deg, #7B9EF5, #B8A0F5);
}

.total-body {
  padding: 28rpx 32rpx;
}

.total-label {
  display: block;
  font-size: 26rpx;
  color: #9CA3AF;
  margin-bottom: 8rpx;
}

.total-amount {
  display: block;
  font-size: 52rpx;
  font-weight: 700;
  color: #2D3142;
  margin-bottom: 12rpx;
}

.total-sub {
  display: flex;
  gap: 32rpx;
}

.total-sub-text {
  font-size: 24rpx;
  color: #9CA3AF;
}

/* 图表卡片 */
.chart-card {
  margin: 0 32rpx 24rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 28rpx;
  box-shadow: 0 2rpx 16rpx rgba(123, 158, 245, 0.08);
}

.chart-title {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: #2D3142;
  margin-bottom: 24rpx;
}

/* 饼状图区域 */
.pie-area {
  position: relative;
  display: flex;
  justify-content: center;
  margin-bottom: 28rpx;
}

.pie-canvas {
  width: 400rpx;
  height: 400rpx;
}

.pie-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.pie-center-amount {
  font-size: 28rpx;
  font-weight: 700;
  color: #2D3142;
}

.pie-center-label {
  font-size: 22rpx;
  color: #9CA3AF;
  margin-top: 4rpx;
}

/* 图例 2列 */
.legend-grid {
  display: flex;
  flex-wrap: wrap;
}

.legend-item {
  width: 50%;
  display: flex;
  align-items: center;
  padding: 10rpx 0;
}

.legend-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  margin-right: 10rpx;
  flex-shrink: 0;
}

.legend-name {
  font-size: 24rpx;
  color: #6B7280;
  margin-right: 8rpx;
}

.legend-percent {
  font-size: 24rpx;
  color: #2D3142;
  font-weight: 600;
  margin-right: 8rpx;
}

.legend-amount {
  font-size: 22rpx;
  color: #9CA3AF;
}

/* 柱状图 - 蓝紫渐变交替 */
.bar-chart {
  display: flex;
  align-items: flex-end;
  height: 320rpx;
  padding-top: 40rpx;
}

.bar-item {
  flex: 1;
  min-width: 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.bar-value-wrap {
  height: 36rpx;
  display: flex;
  align-items: flex-end;
  margin-bottom: 8rpx;
}

.bar-value {
  font-size: 16rpx;
  color: #9CA3AF;
}

.bar-col-wrap {
  width: 20rpx;
  height: 200rpx;
  display: flex;
  align-items: flex-end;
}

.bar-col {
  width: 100%;
  border-radius: 10rpx 10rpx 4rpx 4rpx;
  transition: height 0.3s ease;
}

.bar-blue {
  background: #7B9EF5;
}

.bar-purple {
  background: #B8A0F5;
}

.bar-label {
  font-size: 18rpx;
  color: #9CA3AF;
  margin-top: 8rpx;
  height: 24rpx;
}

/* 分类排行 */
.rank-list {
  display: flex;
  flex-direction: column;
}

.rank-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #F3F4F8;
}

.rank-item:last-child {
  border-bottom: none;
}

.rank-left {
  flex: 1;
  display: flex;
  align-items: center;
  overflow: hidden;
}

.rank-icon-wrap {
  width: 64rpx;
  height: 64rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.rank-icon-img {
  width: 40rpx;
  height: 40rpx;
}

.rank-info {
  flex: 1;
  overflow: hidden;
}

.rank-name-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10rpx;
}

.rank-name {
  font-size: 26rpx;
  color: #2D3142;
  font-weight: 500;
}

.rank-percent-text {
  font-size: 24rpx;
  color: #9CA3AF;
}

.rank-bar-bg {
  height: 8rpx;
  background: #F0F1F5;
  border-radius: 4rpx;
  overflow: hidden;
}

.rank-bar {
  height: 100%;
  border-radius: 4rpx;
  transition: width 0.3s ease;
}

.rank-amount {
  font-size: 28rpx;
  font-weight: 600;
  color: #2D3142;
  margin-left: 20rpx;
  flex-shrink: 0;
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
