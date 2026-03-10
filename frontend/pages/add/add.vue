<template>
  <view class="page">
    <!-- 顶部状态栏占位 -->
    <view :style="{ height: statusBarHeight + 'px' }"></view>
    
    <!-- 自定义导航栏 -->
    <view class="nav-bar">
      <text class="nav-title">{{ isEdit ? '编辑记录' : '记一笔' }}</text>
    </view>

    <!-- 收支类型切换 -->
    <view class="type-switch">
      <view class="type-item" :class="{ active: type === 1 }" @click="type = 1">
        <text>支出</text>
      </view>
      <view class="type-item income" :class="{ active: type === 2 }" @click="type = 2">
        <text>收入</text>
      </view>
    </view>

    <!-- 金额输入 -->
    <view class="amount-section">
      <text class="amount-symbol">¥</text>
      <text class="amount-value">{{ displayAmount }}</text>
      <view class="amount-cursor" v-if="!amount"></view>
    </view>

    <!-- 备注输入 -->
    <view class="remark-section">
      <input class="remark-input" v-model="remark" placeholder="添加备注..." placeholder-class="remark-placeholder" maxlength="100" />
    </view>

    <!-- 日期选择 -->
    <view class="date-section">
      <picker mode="date" :value="recordDate" @change="onDateChange">
        <view class="date-item">
          <text class="date-icon">📅</text>
          <text class="date-text">{{ dateDisplay }}</text>
        </view>
      </picker>
    </view>

    <!-- 分类选择 -->
    <view class="category-section">
      <scroll-view scroll-y class="category-scroll">
        <view class="category-grid">
          <view
            class="category-item"
            v-for="item in categoryList"
            :key="item.id"
            :class="{ active: selectedCategory === item.id }"
            @click="selectCategory(item)"
          >
            <view class="category-icon" :class="{ active: selectedCategory === item.id }">
              {{ item.icon }}
            </view>
            <text class="category-name">{{ item.name }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 数字键盘 -->
    <view class="keyboard">
      <view class="keyboard-row">
        <view class="key" @click="inputNumber('1')"><text>1</text></view>
        <view class="key" @click="inputNumber('2')"><text>2</text></view>
        <view class="key" @click="inputNumber('3')"><text>3</text></view>
        <view class="key key-back" @click="backspace"><text>⌫</text></view>
      </view>
      <view class="keyboard-row">
        <view class="key" @click="inputNumber('4')"><text>4</text></view>
        <view class="key" @click="inputNumber('5')"><text>5</text></view>
        <view class="key" @click="inputNumber('6')"><text>6</text></view>
        <view class="key key-plus" @click="inputNumber('+')"><text>+</text></view>
      </view>
      <view class="keyboard-row">
        <view class="key" @click="inputNumber('7')"><text>7</text></view>
        <view class="key" @click="inputNumber('8')"><text>8</text></view>
        <view class="key" @click="inputNumber('9')"><text>9</text></view>
        <view class="key key-confirm" @click="saveRecord">
          <text>{{ isEdit ? '保存' : '确定' }}</text>
        </view>
      </view>
      <view class="keyboard-row">
        <view class="key key-zero" @click="inputNumber('0')"><text>0</text></view>
        <view class="key" @click="inputNumber('.')"><text>.</text></view>
        <view class="key key-clear" @click="clearAmount"><text>C</text></view>
      </view>
    </view>
  </view>
</template>

<script>
import { getCategoryList, addRecord, updateRecord } from '../../api/index';
import { getCurrentDate, getCurrentTime, formatMoney } from '../../utils/util';

export default {
  data() {
    return {
      statusBarHeight: 20,
      isEdit: false,
      editId: null,
      type: 1,
      amount: '',
      remark: '',
      recordDate: getCurrentDate(),
      recordTime: getCurrentTime(),
      selectedCategory: null,
      categoryList: [],
      expenseCategories: [],
      incomeCategories: []
    };
  },
  computed: {
    displayAmount() {
      return this.amount || '0.00';
    },
    dateDisplay() {
      const today = getCurrentDate();
      if (this.recordDate === today) return '今天';
      const parts = this.recordDate.split('-');
      return `${parseInt(parts[1])}月${parseInt(parts[2])}日`;
    }
  },
  watch: {
    type(val) {
      this.categoryList = val === 1 ? this.expenseCategories : this.incomeCategories;
      if (this.categoryList.length > 0 && !this.isEdit) {
        this.selectedCategory = this.categoryList[0].id;
      }
    }
  },
  onLoad(options) {
    const sysInfo = uni.getSystemInfoSync();
    this.statusBarHeight = sysInfo.statusBarHeight || 20;
    
    if (options && options.edit === '1' && options.id) {
      this.isEdit = true;
      this.editId = parseInt(options.id);
    }
    this.loadCategories();
  },
  methods: {
    async loadCategories() {
      try {
        const expRes = await getCategoryList(1);
        this.expenseCategories = expRes.data || [];
        
        const incRes = await getCategoryList(2);
        this.incomeCategories = incRes.data || [];
        
        this.categoryList = this.type === 1 ? this.expenseCategories : this.incomeCategories;
        if (this.categoryList.length > 0 && !this.isEdit) {
          this.selectedCategory = this.categoryList[0].id;
        }
      } catch (e) {
        console.error('加载分类失败', e);
      }
    },
    selectCategory(item) {
      this.selectedCategory = item.id;
    },
    onDateChange(e) {
      this.recordDate = e.detail.value;
    },
    inputNumber(num) {
      // 处理加号（简单累加功能）
      if (num === '+') {
        return; // 暂不实现计算器功能，保留按钮位置
      }
      if (num === '.' && this.amount.includes('.')) return;
      if (num === '.' && !this.amount) {
        this.amount = '0.';
        return;
      }
      // 限制小数点后两位
      if (this.amount.includes('.')) {
        const parts = this.amount.split('.');
        if (parts[1].length >= 2) return;
      }
      // 限制整数部分长度
      if (!this.amount.includes('.') && num !== '.') {
        const intPart = this.amount.replace('.', '');
        if (intPart.length >= 8) return;
      }
      // 避免多个0开头
      if (this.amount === '0' && num !== '.') {
        this.amount = num;
        return;
      }
      this.amount += num;
    },
    backspace() {
      if (this.amount.length > 0) {
        this.amount = this.amount.slice(0, -1);
      }
    },
    clearAmount() {
      this.amount = '';
    },
    async saveRecord() {
      if (!this.amount || parseFloat(this.amount) <= 0) {
        uni.showToast({ title: '请输入金额', icon: 'none' });
        return;
      }
      if (!this.selectedCategory) {
        uni.showToast({ title: '请选择分类', icon: 'none' });
        return;
      }

      const data = {
        categoryId: this.selectedCategory,
        type: this.type,
        amount: parseFloat(this.amount),
        remark: this.remark,
        recordDate: this.recordDate,
        recordTime: this.recordTime
      };

      try {
        if (this.isEdit) {
          data.id = this.editId;
          await updateRecord(data);
          uni.showToast({ title: '修改成功' });
        } else {
          await addRecord(data);
          uni.showToast({ title: '记账成功' });
        }
        
        // 重置表单
        this.amount = '';
        this.remark = '';
        this.recordDate = getCurrentDate();
        this.recordTime = getCurrentTime();
        
        if (this.isEdit) {
          setTimeout(() => {
            uni.navigateBack();
          }, 500);
        }
      } catch (e) {
        console.error('保存失败', e);
      }
    }
  }
};
</script>

<style lang="scss">
.page {
  min-height: 100vh;
  background: #F8F9FE;
  display: flex;
  flex-direction: column;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 88rpx;
  padding: 0 32rpx;
}

.nav-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #2D3142;
}

/* 类型切换 */
.type-switch {
  display: flex;
  margin: 16rpx 32rpx;
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 6rpx;
  box-shadow: 0 2rpx 12rpx rgba(124, 159, 245, 0.08);
}

.type-item {
  flex: 1;
  text-align: center;
  padding: 20rpx 0;
  border-radius: 16rpx;
  font-size: 30rpx;
  color: #9CA3AF;
  transition: all 0.3s;
}

.type-item.active {
  background: linear-gradient(135deg, #F5707A, #F5A0B5);
  color: #FFFFFF;
  font-weight: 600;
  box-shadow: 0 4rpx 16rpx rgba(245, 112, 122, 0.3);
}

.type-item.income.active {
  background: linear-gradient(135deg, #5CC9A7, #8DD5B0);
  box-shadow: 0 4rpx 16rpx rgba(92, 201, 167, 0.3);
}

/* 金额输入 */
.amount-section {
  display: flex;
  align-items: baseline;
  justify-content: center;
  padding: 40rpx 32rpx 20rpx;
}

.amount-symbol {
  font-size: 40rpx;
  color: #2D3142;
  margin-right: 8rpx;
  font-weight: 500;
}

.amount-value {
  font-size: 72rpx;
  font-weight: 700;
  color: #2D3142;
  letter-spacing: 2rpx;
}

/* 备注输入 */
.remark-section {
  padding: 0 64rpx;
  margin-bottom: 16rpx;
}

.remark-input {
  text-align: center;
  font-size: 26rpx;
  color: #6B7280;
  padding: 16rpx 0;
  border-bottom: 2rpx solid #EEEEF3;
}

.remark-placeholder {
  color: #D1D5DB;
}

/* 日期选择 */
.date-section {
  display: flex;
  justify-content: center;
  padding: 12rpx 32rpx;
}

.date-item {
  display: flex;
  align-items: center;
  padding: 12rpx 28rpx;
  background: #FFFFFF;
  border-radius: 32rpx;
  box-shadow: 0 2rpx 8rpx rgba(124, 159, 245, 0.06);
}

.date-icon {
  font-size: 28rpx;
  margin-right: 8rpx;
}

.date-text {
  font-size: 26rpx;
  color: #6B7280;
}

/* 分类选择 */
.category-section {
  flex: 1;
  padding: 20rpx 32rpx;
  overflow: hidden;
}

.category-scroll {
  max-height: 360rpx;
}

.category-grid {
  display: flex;
  flex-wrap: wrap;
}

.category-item {
  width: 20%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16rpx 0;
}

.category-icon {
  width: 88rpx;
  height: 88rpx;
  background: #FFFFFF;
  border-radius: 22rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  margin-bottom: 8rpx;
  box-shadow: 0 2rpx 8rpx rgba(124, 159, 245, 0.06);
  transition: all 0.2s;
}

.category-icon.active {
  background: linear-gradient(135deg, #7C9FF5, #A8C0F7);
  box-shadow: 0 4rpx 16rpx rgba(124, 159, 245, 0.3);
  transform: scale(1.05);
}

.category-name {
  font-size: 22rpx;
  color: #6B7280;
}

/* 数字键盘 */
.keyboard {
  background: #FFFFFF;
  padding: 12rpx 16rpx;
  padding-bottom: calc(12rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.keyboard-row {
  display: flex;
  margin-bottom: 12rpx;
}

.keyboard-row:last-child {
  margin-bottom: 0;
}

.key {
  flex: 1;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F8F9FE;
  border-radius: 16rpx;
  margin: 0 6rpx;
  font-size: 34rpx;
  color: #2D3142;
  font-weight: 500;
}

.key:active {
  background: #EEEEF3;
}

.key-zero {
  flex: 2;
}

.key-back {
  background: #F3F4F8;
  color: #6B7280;
}

.key-plus {
  background: #F3F4F8;
  font-size: 34rpx;
  color: #7C9FF5;
  font-weight: 600;
}

.key-confirm {
  background: linear-gradient(135deg, #7C9FF5, #A8C0F7);
  color: #FFFFFF;
  font-size: 30rpx;
  font-weight: 600;
  box-shadow: 0 4rpx 16rpx rgba(124, 159, 245, 0.3);
}

.key-clear {
  color: #F5707A;
  font-weight: 600;
}
</style>
