<template>
  <view class="page">
    <!-- 顶部状态栏占位 -->
    <view :style="{ height: statusBarHeight + 'px' }"></view>
    
    <!-- 导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text>‹</text>
      </view>
      <text class="nav-title">预算管理</text>
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

    <scroll-view scroll-y class="content-scroll">
      <!-- 环形进度图卡片 -->
      <view class="ring-card" v-if="totalBudget > 0">
        <view class="ring-area">
          <canvas canvas-id="ringChart" class="ring-canvas" :style="{ width: '360rpx', height: '360rpx' }"></canvas>
          <view class="ring-center">
            <text class="ring-percent">{{ budgetPercent }}%</text>
            <text class="ring-label">已使用</text>
          </view>
        </view>

        <!-- 三项数据 -->
        <view class="budget-data-row">
          <view class="budget-data-item">
            <text class="budget-data-label">预算</text>
            <text class="budget-data-value">¥{{ formatMoney(totalBudget) }}</text>
          </view>
          <view class="budget-data-item">
            <text class="budget-data-label">已花</text>
            <text class="budget-data-value expense">¥{{ formatMoney(monthExpense) }}</text>
          </view>
          <view class="budget-data-item">
            <text class="budget-data-label">{{ budgetRemain >= 0 ? '剩余' : '超支' }}</text>
            <text class="budget-data-value" :class="budgetRemain >= 0 ? 'safe' : 'over'">
              ¥{{ formatMoney(Math.abs(budgetRemain)) }}
            </text>
          </view>
        </view>
      </view>

      <!-- 设置预算按钮 -->
      <view class="set-budget-btn" @click="editBudget">
        <text class="set-budget-text">{{ totalBudget > 0 ? '修改预算' : '设置月度预算' }}</text>
      </view>

      <!-- 预算进度详情卡片 -->
      <view class="detail-card" v-if="totalBudget > 0">
        <text class="detail-title">预算使用详情</text>

        <!-- 进度条 -->
        <view class="progress-section">
          <view class="progress-header">
            <text class="progress-label">总体进度</text>
            <text class="progress-percent" :class="statusClass">{{ budgetPercent }}%</text>
          </view>
          <view class="progress-bar-bg">
            <view class="progress-bar" :style="{ width: Math.min(budgetPercent, 100) + '%' }" :class="statusClass"></view>
          </view>
          <text class="progress-hint">{{ dailyAvgHint }}</text>
        </view>

        <!-- 状态示例 -->
        <view class="status-legend">
          <view class="status-item">
            <view class="status-dot safe"></view>
            <text class="status-text">正常 (0-60%)</text>
          </view>
          <view class="status-item">
            <view class="status-dot warn"></view>
            <text class="status-text">警告 (60-80%)</text>
          </view>
          <view class="status-item">
            <view class="status-dot over"></view>
            <text class="status-text">超支 (80%+)</text>
          </view>
        </view>
      </view>

      <!-- 未设置预算时的空状态 -->
      <view class="empty-card" v-if="totalBudget <= 0">
        <text class="empty-icon">💰</text>
        <text class="empty-title">还没有设置预算</text>
        <text class="empty-desc">设置月度预算，合理规划每月开支</text>
      </view>

      <view style="height: 40rpx;"></view>
    </scroll-view>

    <!-- 预算设置弹窗 -->
    <view class="modal-mask" v-if="showModal" @click="showModal = false">
      <view class="modal-content" @click.stop>
        <text class="modal-title">设置月度预算</text>
        <view class="modal-input-wrap">
          <text class="modal-symbol">¥</text>
          <input class="modal-input" type="digit" v-model="budgetInput" placeholder="请输入预算金额" focus />
        </view>
        <view class="modal-btns">
          <view class="modal-btn cancel" @click="showModal = false">
            <text>取消</text>
          </view>
          <view class="modal-btn confirm" @click="saveBudget">
            <text>确定</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { setBudget, getMonthBudgets, getMonthBill } from '../../api/index';
import { formatMoney, getCurrentYearMonth, getMonthName, getPrevMonth, getNextMonth } from '../../utils/util';

export default {
  data() {
    return {
      statusBarHeight: 20,
      currentMonth: getCurrentYearMonth(),
      totalBudget: 0,
      monthExpense: 0,
      showModal: false,
      budgetInput: ''
    };
  },
  computed: {
    monthName() {
      return getMonthName(this.currentMonth);
    },
    budgetRemain() {
      return this.totalBudget - this.monthExpense;
    },
    budgetPercent() {
      if (this.totalBudget <= 0) return 0;
      return Math.round((this.monthExpense / this.totalBudget) * 100);
    },
    statusClass() {
      if (this.budgetPercent > 80) return 'over';
      if (this.budgetPercent > 60) return 'warn';
      return 'safe';
    },
    dailyAvgHint() {
      if (this.totalBudget <= 0) return '';
      const now = new Date();
      const parts = this.currentMonth.split('-');
      const year = parseInt(parts[0]);
      const month = parseInt(parts[1]);
      const daysInMonth = new Date(year, month, 0).getDate();
      let remainDays;
      if (year === now.getFullYear() && month === now.getMonth() + 1) {
        remainDays = daysInMonth - now.getDate();
      } else {
        remainDays = daysInMonth;
      }
      if (remainDays <= 0 || this.budgetRemain <= 0) return '本月预算已用完';
      const dailyAvg = this.budgetRemain / remainDays;
      return '剩余 ' + remainDays + ' 天，日均可用 ¥' + formatMoney(dailyAvg);
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
    goBack() {
      uni.navigateBack();
    },
    async loadData() {
      try {
        const budgetRes = await getMonthBudgets(this.currentMonth);
        const budgets = budgetRes.data || [];
        const totalBudgetItem = budgets.find(b => !b.categoryId);
        this.totalBudget = totalBudgetItem ? totalBudgetItem.amount : 0;

        const billRes = await getMonthBill({ yearMonth: this.currentMonth });
        this.monthExpense = billRes.data.totalExpense || 0;

        this.$nextTick(() => {
          if (this.totalBudget > 0) {
            this.drawRingChart();
          }
        });
      } catch (e) {
        console.error('加载预算数据失败', e);
      }
    },
    drawRingChart() {
      const ctx = uni.createCanvasContext('ringChart', this);
      const centerX = 90;
      const centerY = 90;
      const radius = 78;
      const lineWidth = 16;

      ctx.clearRect(0, 0, 180, 180);

      // 背景圆环
      ctx.beginPath();
      ctx.arc(centerX, centerY, radius, 0, 2 * Math.PI);
      ctx.setStrokeStyle('#F0F1F5');
      ctx.setLineWidth(lineWidth);
      ctx.setLineCap('round');
      ctx.stroke();

      // 进度圆环
      const percent = Math.min(this.budgetPercent, 100) / 100;
      if (percent > 0) {
        const startAngle = -Math.PI / 2;
        const endAngle = startAngle + percent * 2 * Math.PI;

        // 渐变色
        let strokeColor;
        if (this.budgetPercent > 80) {
          strokeColor = '#F5707A';
        } else if (this.budgetPercent > 60) {
          strokeColor = '#F5C07C';
        } else {
          strokeColor = '#7B9EF5';
        }

        ctx.beginPath();
        ctx.arc(centerX, centerY, radius, startAngle, endAngle);
        ctx.setStrokeStyle(strokeColor);
        ctx.setLineWidth(lineWidth);
        ctx.setLineCap('round');
        ctx.stroke();
      }

      ctx.draw();
    },
    prevMonth() {
      this.currentMonth = getPrevMonth(this.currentMonth);
      this.loadData();
    },
    nextMonth() {
      this.currentMonth = getNextMonth(this.currentMonth);
      this.loadData();
    },
    editBudget() {
      this.budgetInput = this.totalBudget > 0 ? this.totalBudget.toString() : '';
      this.showModal = true;
    },
    async saveBudget() {
      const amount = parseFloat(this.budgetInput);
      if (isNaN(amount) || amount < 0) {
        uni.showToast({ title: '请输入有效金额', icon: 'none' });
        return;
      }
      try {
        await setBudget({
          amount: amount,
          yearMonth: this.currentMonth
        });
        this.showModal = false;
        uni.showToast({ title: '设置成功' });
        this.loadData();
      } catch (e) {
        console.error('设置预算失败', e);
      }
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

.content-scroll {
  flex: 1;
}

/* 环形进度图卡片 */
.ring-card {
  margin: 0 32rpx 24rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 36rpx 28rpx;
  box-shadow: 0 2rpx 16rpx rgba(123, 158, 245, 0.08);
}

.ring-area {
  position: relative;
  display: flex;
  justify-content: center;
  margin-bottom: 32rpx;
}

.ring-canvas {
  width: 360rpx;
  height: 360rpx;
}

.ring-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.ring-percent {
  font-size: 48rpx;
  font-weight: 700;
  color: #2D3142;
}

.ring-label {
  font-size: 24rpx;
  color: #9CA3AF;
  margin-top: 4rpx;
}

/* 三项数据 */
.budget-data-row {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 20rpx 0 0;
  border-top: 1rpx solid #F3F4F8;
}

.budget-data-item {
  text-align: center;
}

.budget-data-label {
  display: block;
  font-size: 24rpx;
  color: #9CA3AF;
  margin-bottom: 8rpx;
}

.budget-data-value {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: #2D3142;
}

.budget-data-value.expense {
  color: #F5707A;
}

.budget-data-value.safe {
  color: #5CC9A7;
}

.budget-data-value.over {
  color: #F5707A;
}

/* 设置预算按钮 */
.set-budget-btn {
  margin: 0 32rpx 24rpx;
  padding: 28rpx 0;
  text-align: center;
  background: linear-gradient(135deg, #7B9EF5, #B8A0F5);
  border-radius: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(123, 158, 245, 0.3);
}

.set-budget-btn:active {
  opacity: 0.9;
}

.set-budget-text {
  font-size: 30rpx;
  color: #FFFFFF;
  font-weight: 600;
}

/* 预算详情卡片 */
.detail-card {
  margin: 0 32rpx 24rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 28rpx;
  box-shadow: 0 2rpx 16rpx rgba(123, 158, 245, 0.08);
}

.detail-title {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: #2D3142;
  margin-bottom: 24rpx;
}

/* 进度条 */
.progress-section {
  margin-bottom: 24rpx;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.progress-label {
  font-size: 26rpx;
  color: #6B7280;
}

.progress-percent {
  font-size: 26rpx;
  font-weight: 600;
}

.progress-percent.safe {
  color: #7B9EF5;
}

.progress-percent.warn {
  color: #F5C07C;
}

.progress-percent.over {
  color: #F5707A;
}

.progress-bar-bg {
  height: 14rpx;
  background: #F0F1F5;
  border-radius: 7rpx;
  overflow: hidden;
  margin-bottom: 12rpx;
}

.progress-bar {
  height: 100%;
  border-radius: 7rpx;
  transition: width 0.3s ease;
}

.progress-bar.safe {
  background: linear-gradient(90deg, #7B9EF5, #9BB0F7);
}

.progress-bar.warn {
  background: linear-gradient(90deg, #F5C07C, #F5D49E);
}

.progress-bar.over {
  background: linear-gradient(90deg, #F5707A, #F5A0A8);
}

.progress-hint {
  font-size: 24rpx;
  color: #9CA3AF;
}

/* 状态图例 */
.status-legend {
  display: flex;
  gap: 24rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #F3F4F8;
}

.status-item {
  display: flex;
  align-items: center;
}

.status-dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  margin-right: 8rpx;
}

.status-dot.safe {
  background: #7B9EF5;
}

.status-dot.warn {
  background: #F5C07C;
}

.status-dot.over {
  background: #F5707A;
}

.status-text {
  font-size: 22rpx;
  color: #9CA3AF;
}

/* 空状态 */
.empty-card {
  margin: 40rpx 32rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 0;
  background: #FFFFFF;
  border-radius: 24rpx;
  box-shadow: 0 2rpx 16rpx rgba(123, 158, 245, 0.08);
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 24rpx;
}

.empty-title {
  font-size: 30rpx;
  color: #2D3142;
  font-weight: 600;
  margin-bottom: 12rpx;
}

.empty-desc {
  font-size: 26rpx;
  color: #9CA3AF;
}

/* 弹窗 */
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.modal-content {
  width: 600rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 48rpx 40rpx;
}

.modal-title {
  display: block;
  text-align: center;
  font-size: 32rpx;
  font-weight: 600;
  color: #2D3142;
  margin-bottom: 40rpx;
}

.modal-input-wrap {
  display: flex;
  align-items: center;
  background: #F8F9FE;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 40rpx;
}

.modal-symbol {
  font-size: 36rpx;
  font-weight: 600;
  color: #2D3142;
  margin-right: 12rpx;
}

.modal-input {
  flex: 1;
  font-size: 36rpx;
  font-weight: 600;
  color: #2D3142;
}

.modal-btns {
  display: flex;
  gap: 24rpx;
}

.modal-btn {
  flex: 1;
  text-align: center;
  padding: 24rpx 0;
  border-radius: 16rpx;
  font-size: 30rpx;
  font-weight: 500;
}

.modal-btn.cancel {
  background: #F3F4F8;
  color: #6B7280;
}

.modal-btn.confirm {
  background: linear-gradient(135deg, #7B9EF5, #B8A0F5);
  color: #FFFFFF;
}
</style>
