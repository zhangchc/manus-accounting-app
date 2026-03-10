<template>
  <view class="page">
    <!-- 顶部状态栏占位 -->
    <view :style="{ height: statusBarHeight + 'px' }"></view>
    
    <!-- 自定义导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text class="nav-back-icon">‹</text>
      </view>
      <text class="nav-title">{{ isEdit ? '编辑记录' : '记一笔' }}</text>
      <view class="nav-placeholder"></view>
    </view>

    <!-- 收支类型切换 -->
    <view class="type-switch">
      <view class="type-item" :class="{ active: type === 1 }" @click="type = 1">
        <text>支出</text>
      </view>
      <view class="type-item" :class="{ active: type === 2 }" @click="type = 2">
        <text>收入</text>
      </view>
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
            <view class="category-icon-wrap" :style="{ background: getIconBgColor(item.name) }" :class="{ active: selectedCategory === item.id }">
              <image class="category-icon-img" :src="getIconPath(item.name, item.type)" mode="aspectFit"></image>
              <view class="category-check" v-if="selectedCategory === item.id">
                <text class="check-icon">✓</text>
              </view>
            </view>
            <text class="category-name">{{ item.name }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 日期 + 备注 横排 -->
    <view class="meta-row">
      <picker mode="date" :value="recordDate" @change="onDateChange">
        <view class="meta-item">
          <text class="meta-icon">📅</text>
          <text class="meta-text">{{ dateDisplay }}</text>
        </view>
      </picker>
      <view class="meta-divider"></view>
      <view class="meta-item meta-remark">
        <text class="meta-icon">✏️</text>
        <input class="remark-input" v-model="remark" placeholder="添加备注..." placeholder-class="remark-placeholder" maxlength="100" />
      </view>
    </view>

    <!-- 金额显示 -->
    <view class="amount-display">
      <view class="amount-line"></view>
      <view class="amount-row">
        <text class="amount-symbol">¥</text>
        <text class="amount-value">{{ displayAmount }}</text>
        <text class="amount-currency">CNY</text>
      </view>
    </view>

    <!-- 数字键盘 -->
    <view class="keyboard">
      <view class="keyboard-main">
        <view class="keyboard-row">
          <view class="key" @click="inputNumber('1')"><text>1</text></view>
          <view class="key" @click="inputNumber('2')"><text>2</text></view>
          <view class="key" @click="inputNumber('3')"><text>3</text></view>
        </view>
        <view class="keyboard-row">
          <view class="key" @click="inputNumber('4')"><text>4</text></view>
          <view class="key" @click="inputNumber('5')"><text>5</text></view>
          <view class="key" @click="inputNumber('6')"><text>6</text></view>
        </view>
        <view class="keyboard-row">
          <view class="key" @click="inputNumber('7')"><text>7</text></view>
          <view class="key" @click="inputNumber('8')"><text>8</text></view>
          <view class="key" @click="inputNumber('9')"><text>9</text></view>
        </view>
        <view class="keyboard-row">
          <view class="key" @click="clearAmount"><text class="key-special">C</text></view>
          <view class="key" @click="inputNumber('0')"><text>0</text></view>
          <view class="key" @click="inputNumber('.')"><text>.</text></view>
        </view>
      </view>
      <view class="keyboard-side">
        <view class="key key-back" @click="backspace">
          <text class="key-back-icon">⌫</text>
        </view>
        <view class="key key-confirm" @click="saveRecord">
          <text class="key-confirm-text">{{ isEdit ? '保存' : '完成' }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getCategoryList, addRecord, updateRecord } from '../../api/index';
import { getCurrentDate, getCurrentTime, formatMoney } from '../../utils/util';
import { getCategoryIconPath, getCategoryBgColor } from '../../utils/icon';

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
      return this.recordDate;
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
  async onLoad(options) {
    const sysInfo = uni.getSystemInfoSync();
    this.statusBarHeight = sysInfo.statusBarHeight || 20;
    
    if (options && options.edit === '1' && options.id) {
      this.isEdit = true;
      this.editId = parseInt(options.id);
    }
    try {
      await getApp().ensureLogin();
      this.loadCategories();
    } catch (e) {
      console.error('登录未完成', e);
    }
  },
  methods: {
    goBack() {
      uni.navigateBack({ fail: () => { uni.switchTab({ url: '/pages/index/index' }); } });
    },
    getIconPath(name, type) {
      return getCategoryIconPath(name, type);
    },
    getIconBgColor(name) {
      return getCategoryBgColor(name);
    },
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
      if (num === '.' && this.amount.includes('.')) return;
      if (num === '.' && !this.amount) {
        this.amount = '0.';
        return;
      }
      if (this.amount.includes('.')) {
        const parts = this.amount.split('.');
        if (parts[1].length >= 2) return;
      }
      if (!this.amount.includes('.') && num !== '.') {
        const intPart = this.amount.replace('.', '');
        if (intPart.length >= 8) return;
      }
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
        
        this.amount = '';
        this.remark = '';
        this.recordDate = getCurrentDate();
        this.recordTime = getCurrentTime();
        
        if (this.isEdit) {
          setTimeout(() => { uni.navigateBack(); }, 500);
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
  background: #FAFBFE;
  display: flex;
  flex-direction: column;
}

/* 导航栏 */
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
}

.nav-back-icon {
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

/* 类型切换 - 蓝紫渐变胶囊 */
.type-switch {
  display: flex;
  margin: 12rpx 32rpx 20rpx;
  background: #F0F1F5;
  border-radius: 24rpx;
  padding: 6rpx;
}

.type-item {
  flex: 1;
  text-align: center;
  padding: 20rpx 0;
  border-radius: 20rpx;
  font-size: 30rpx;
  color: #9CA3AF;
  transition: all 0.3s;
  font-weight: 500;
}

.type-item.active {
  background: linear-gradient(135deg, #7B9EF5, #B8A0F5);
  color: #FFFFFF;
  font-weight: 600;
  box-shadow: 0 4rpx 16rpx rgba(123, 158, 245, 0.3);
}

/* 分类选择 - 4列网格 + 图片图标 */
.category-section {
  padding: 0 32rpx;
  flex-shrink: 0;
}

.category-scroll {
  max-height: 520rpx;
}

.category-grid {
  display: flex;
  flex-wrap: wrap;
}

.category-item {
  width: 25%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16rpx 0;
}

.category-icon-wrap {
  width: 108rpx;
  height: 108rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10rpx;
  position: relative;
  border: 3rpx solid transparent;
  transition: all 0.2s;
}

.category-icon-wrap.active {
  border-color: #7B9EF5;
  box-shadow: 0 4rpx 16rpx rgba(123, 158, 245, 0.2);
}

.category-icon-img {
  width: 56rpx;
  height: 56rpx;
}

.category-check {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  width: 32rpx;
  height: 32rpx;
  background: #7B9EF5;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.check-icon {
  font-size: 18rpx;
  color: #FFFFFF;
  font-weight: 700;
}

.category-name {
  font-size: 24rpx;
  color: #6B7280;
}

/* 日期 + 备注 横排 */
.meta-row {
  display: flex;
  align-items: center;
  margin: 20rpx 32rpx;
  padding: 20rpx 24rpx;
  background: #F0F1F5;
  border-radius: 20rpx;
}

.meta-item {
  display: flex;
  align-items: center;
}

.meta-remark {
  flex: 1;
}

.meta-icon {
  font-size: 26rpx;
  margin-right: 8rpx;
}

.meta-text {
  font-size: 26rpx;
  color: #6B7280;
}

.meta-divider {
  width: 2rpx;
  height: 28rpx;
  background: #D1D5DB;
  margin: 0 24rpx;
}

.remark-input {
  flex: 1;
  font-size: 26rpx;
  color: #6B7280;
}

.remark-placeholder {
  color: #C4C8D0;
}

/* 金额显示 */
.amount-display {
  padding: 16rpx 32rpx 20rpx;
}

.amount-line {
  height: 2rpx;
  background: #EEEEF3;
  margin-bottom: 24rpx;
}

.amount-row {
  display: flex;
  align-items: baseline;
  justify-content: center;
}

.amount-symbol {
  font-size: 40rpx;
  color: #2D3142;
  font-weight: 600;
  margin-right: 8rpx;
}

.amount-value {
  font-size: 72rpx;
  font-weight: 700;
  color: #2D3142;
  letter-spacing: 2rpx;
}

.amount-currency {
  font-size: 24rpx;
  color: #9CA3AF;
  margin-left: 12rpx;
  font-weight: 500;
}

/* 数字键盘 */
.keyboard {
  display: flex;
  background: #F0F1F5;
  padding: 12rpx 12rpx;
  padding-bottom: calc(12rpx + env(safe-area-inset-bottom));
  margin-top: auto;
}

.keyboard-main {
  flex: 3;
}

.keyboard-side {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-left: 8rpx;
}

.keyboard-row {
  display: flex;
  margin-bottom: 10rpx;
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
  background: #FFFFFF;
  border-radius: 16rpx;
  margin: 0 4rpx;
  font-size: 36rpx;
  color: #2D3142;
  font-weight: 500;
}

.key:active {
  background: #E8E9ED;
}

.key-special {
  color: #F5707A;
  font-weight: 600;
  font-size: 32rpx;
}

.key-back {
  height: 88rpx;
  margin-bottom: 10rpx;
  background: #E4E5EA;
}

.key-back-icon {
  font-size: 32rpx;
  color: #6B7280;
}

.key-confirm {
  flex: 1;
  height: auto;
  background: linear-gradient(180deg, #7B9EF5, #B8A0F5);
  border-radius: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(123, 158, 245, 0.3);
}

.key-confirm:active {
  opacity: 0.9;
}

.key-confirm-text {
  font-size: 32rpx;
  color: #FFFFFF;
  font-weight: 600;
}
</style>
