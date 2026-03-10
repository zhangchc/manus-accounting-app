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

    <!-- 总预算设置 -->
    <view class="budget-card">
      <view class="budget-header">
        <text class="budget-title">月度总预算</text>
        <view class="budget-edit" @click="editBudget">
          <text>{{ totalBudget > 0 ? '修改' : '设置' }}</text>
        </view>
      </view>
      
      <view class="budget-amount" v-if="totalBudget > 0">
        <text class="budget-value">¥{{ formatMoney(totalBudget) }}</text>
        <view class="budget-progress">
          <view class="progress-info">
            <text class="progress-used">已用 ¥{{ formatMoney(monthExpense) }}</text>
            <text class="progress-remain" :class="{ over: budgetRemain < 0 }">
              {{ budgetRemain >= 0 ? '剩余 ¥' + formatMoney(budgetRemain) : '超支 ¥' + formatMoney(Math.abs(budgetRemain)) }}
            </text>
          </view>
          <view class="progress-bar-bg">
            <view class="progress-bar" :style="{ width: budgetPercent + '%' }" :class="{ over: budgetPercent > 80, danger: budgetPercent > 100 }"></view>
          </view>
        </view>
      </view>

      <view class="budget-empty" v-else>
        <text class="budget-empty-text">还没有设置预算哦</text>
        <text class="budget-empty-hint">设置预算，合理规划每月开支</text>
      </view>
    </view>

    <!-- 预算设置弹窗 -->
    <view class="modal-mask" v-if="showModal" @click="showModal = false">
      <view class="modal-content" @click.stop>
        <text class="modal-title">设置月度预算</text>
        <view class="modal-input-wrap">
          <text class="modal-symbol">¥</text>
          <input class="modal-input" type="digit" v-model="budgetInput" placeholder="请输入预算金额" focus />
        </view>
        <view class="modal-btns">
          <view class="modal-btn cancel" @click="showModal = false">取消</view>
          <view class="modal-btn confirm" @click="saveBudget">确定</view>
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
      return Math.min((this.monthExpense / this.totalBudget) * 100, 100);
    }
  },
  onLoad() {
    const sysInfo = uni.getSystemInfoSync();
    this.statusBarHeight = sysInfo.statusBarHeight || 20;
    this.loadData();
  },
  methods: {
    formatMoney,
    goBack() {
      uni.navigateBack();
    },
    async loadData() {
      try {
        // 获取预算
        const budgetRes = await getMonthBudgets(this.currentMonth);
        const budgets = budgetRes.data || [];
        const totalBudgetItem = budgets.find(b => !b.categoryId);
        this.totalBudget = totalBudgetItem ? totalBudgetItem.amount : 0;

        // 获取月支出
        const billRes = await getMonthBill({ yearMonth: this.currentMonth });
        this.monthExpense = billRes.data.totalExpense || 0;
      } catch (e) {
        console.error('加载预算数据失败', e);
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
  background: #F8F9FE;
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

/* 预算卡片 */
.budget-card {
  margin: 24rpx 32rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 2rpx 12rpx rgba(124, 159, 245, 0.08);
}

.budget-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.budget-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #2D3142;
}

.budget-edit {
  padding: 8rpx 24rpx;
  background: #F3F4F8;
  border-radius: 24rpx;
  font-size: 24rpx;
  color: #7C9FF5;
}

.budget-value {
  display: block;
  font-size: 48rpx;
  font-weight: 700;
  color: #2D3142;
  margin-bottom: 24rpx;
}

.budget-progress {
  margin-top: 16rpx;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.progress-used {
  font-size: 24rpx;
  color: #6B7280;
}

.progress-remain {
  font-size: 24rpx;
  color: #5CC9A7;
}

.progress-remain.over {
  color: #F5707A;
}

.progress-bar-bg {
  height: 12rpx;
  background: #F3F4F8;
  border-radius: 6rpx;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: #7C9FF5;
  border-radius: 6rpx;
  transition: width 0.3s ease;
}

.progress-bar.over {
  background: #F5C07C;
}

.progress-bar.danger {
  background: #F5707A;
}

.budget-empty {
  text-align: center;
  padding: 32rpx 0;
}

.budget-empty-text {
  display: block;
  font-size: 30rpx;
  color: #6B7280;
  margin-bottom: 12rpx;
}

.budget-empty-hint {
  display: block;
  font-size: 24rpx;
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
  background: linear-gradient(135deg, #7C9FF5, #A8C0F7);
  color: #FFFFFF;
}
</style>
