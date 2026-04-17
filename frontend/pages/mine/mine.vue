<template>
  <view class="page">
    <!-- 蓝紫渐变头部 + 波浪底边 -->
    <view class="profile-header">
      <view :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="profile-content">
        <view class="avatar-circle">
          <image v-if="userInfo.avatarUrl" class="avatar-img" :src="userInfo.avatarUrl" mode="aspectFill"></image>
          <text v-else class="avatar-letter">{{ avatarText }}</text>
        </view>
        <text class="profile-name">{{ isLoggedIn ? (userInfo.nickName || '记账用户') : '未登录' }}</text>
        <text class="profile-sign">{{ isLoggedIn ? '记录生活，理清收支 ✨' : '登录后可同步你的记账数据' }}</text>
      </view>
      <!-- 波浪底边 -->
      <view class="wave-bottom">
        <view class="wave-shape"></view>
      </view>
    </view>

    <!-- 年度总结卡片 -->
    <view class="year-card" v-if="isLoggedIn">
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
    <view class="menu-card" v-if="isLoggedIn">
      <view class="menu-item" @click="openEditProfile">
        <view class="menu-left">
          <text class="menu-icon">👤</text>
          <text class="menu-name">编辑资料</text>
        </view>
        <text class="menu-arrow">›</text>
      </view>
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
    <view class="logout-btn" v-if="isLoggedIn" @click="logout">
      <text class="logout-text">退出登录</text>
    </view>

    <!-- 登录入口（保持在“我的”tab中） -->
    <view class="login-card login-card-guest" v-else>
      <text class="login-title">登录后开启完整体验</text>
      <text class="login-desc">请先授权头像并填写昵称</text>
      <!-- #ifdef MP-WEIXIN -->
      <button class="avatar-picker" open-type="chooseAvatar" @chooseavatar="onChooseAvatar">
        <image v-if="loginForm.avatarUrl" class="avatar-preview" :src="loginForm.avatarUrl" mode="aspectFill"></image>
        <text v-else class="avatar-picker-text">选择头像</text>
      </button>
      <input
        class="nick-input"
        :class="{ 'nick-input-active': loginNickFocus }"
        type="nickname"
        v-model="loginForm.nickName"
        placeholder="请输入昵称"
        placeholder-style="color:#B8BECC"
        :focus="loginNickFocus"
        @focus="onLoginNickFocus"
        @blur="loginNickFocus = false"
      />
      <text class="nick-tip" :class="{ 'nick-tip-active': loginNickFocus }">点击输入框后，底部会出现“用微信昵称”按钮</text>
      <!-- #endif -->
      <view class="login-btn" @click="handleLogin">
        <text class="login-btn-text">微信一键登录</text>
      </view>
    </view>

    <!-- 编辑资料弹层 -->
    <view class="edit-mask" v-if="showEditPanel" @click="closeEditProfile">
      <view class="edit-panel" @click.stop>
        <text class="edit-title">编辑资料</text>
        <!-- #ifdef MP-WEIXIN -->
        <button class="avatar-picker" open-type="chooseAvatar" @chooseavatar="onChooseEditAvatar">
          <image v-if="editForm.avatarUrl" class="avatar-preview" :src="editForm.avatarUrl" mode="aspectFill"></image>
          <text v-else class="avatar-picker-text">选择头像</text>
        </button>
        <input
          class="nick-input"
          type="nickname"
          v-model="editForm.nickName"
          placeholder="请输入昵称"
          placeholder-style="color:#B8BECC"
        />
        <!-- #endif -->
        <!-- #ifndef MP-WEIXIN -->
        <input
          class="nick-input"
          v-model="editForm.nickName"
          placeholder="请输入昵称"
          placeholder-style="color:#B8BECC"
        />
        <!-- #endif -->
        <view class="edit-actions">
          <view class="action-btn cancel-btn" @click="closeEditProfile">
            <text class="cancel-text">取消</text>
          </view>
          <view class="action-btn save-btn" @click="saveProfile">
            <text class="save-text">{{ editSaving ? '保存中...' : '保存' }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getUserInfo, getYearSummary, updateUserInfo } from '../../api/index';
import { formatMoney } from '../../utils/util';

export default {
  data() {
    return {
      statusBarHeight: 20,
      isLoggedIn: false,
      loginForm: {
        nickName: '',
        avatarUrl: ''
      },
      userInfo: {},
      currentYear: new Date().getFullYear().toString(),
      yearIncome: 0,
      yearExpense: 0,
      yearBalance: 0,
      showEditPanel: false,
      editSaving: false,
      loginNickFocus: false,
      editForm: {
        nickName: '',
        avatarUrl: ''
      }
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
      const loggedIn = await getApp().ensureLogin();
      if (!loggedIn) {
        this.isLoggedIn = false;
        this.userInfo = {};
        this.yearIncome = 0;
        this.yearExpense = 0;
        this.yearBalance = 0;
        return;
      }
      this.isLoggedIn = true;
      this.loadUserInfo();
      this.loadYearSummary();
    } catch (e) {
      this.isLoggedIn = false;
      this.userInfo = {};
      this.yearIncome = 0;
      this.yearExpense = 0;
      this.yearBalance = 0;
      if (e && e.message !== 'USER_LOGGED_OUT') {
        console.error('登录未完成', e);
      }
    }
  },
  methods: {
    formatMoney,
    async loadUserInfo() {
      try {
        const res = await getUserInfo();
        this.userInfo = res.data || {};
        uni.setStorageSync('userInfo', this.userInfo);
      } catch (e) {
        console.error('加载用户信息失败', e);
      }
    },
    openEditProfile() {
      this.editForm.nickName = this.userInfo.nickName || '';
      this.editForm.avatarUrl = this.userInfo.avatarUrl || '';
      this.showEditPanel = true;
    },
    closeEditProfile() {
      if (this.editSaving) return;
      this.showEditPanel = false;
    },
    onChooseEditAvatar(e) {
      const avatarUrl = e?.detail?.avatarUrl || '';
      if (avatarUrl) {
        this.editForm.avatarUrl = avatarUrl;
      }
    },
    async saveProfile() {
      if (this.editSaving) return;
      const nickName = (this.editForm.nickName || '').trim();
      if (!nickName) {
        uni.showToast({ title: '请输入昵称', icon: 'none' });
        return;
      }
      this.editSaving = true;
      try {
        await updateUserInfo({
          nickName,
          avatarUrl: this.editForm.avatarUrl || this.userInfo.avatarUrl || ''
        });
        await this.loadUserInfo();
        this.showEditPanel = false;
        uni.showToast({ title: '保存成功', icon: 'success' });
      } catch (e) {
        console.error('保存资料失败', e);
        uni.showToast({ title: '保存失败，请重试', icon: 'none' });
      } finally {
        this.editSaving = false;
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
    handleLogin() {
      // #ifdef MP-WEIXIN
      if (!this.loginForm.avatarUrl) {
        uni.showToast({ title: '请先选择头像', icon: 'none' });
        return;
      }
      if (!this.loginForm.nickName || !this.loginForm.nickName.trim()) {
        uni.showToast({ title: '请输入昵称', icon: 'none' });
        this.loginNickFocus = true;
        return;
      }
      getApp().relogin({
        nickName: this.loginForm.nickName.trim(),
        avatarUrl: this.loginForm.avatarUrl
      }).then(() => {
        this.isLoggedIn = true;
        this.loginForm.nickName = '';
        this.loginForm.avatarUrl = '';
        this.loadUserInfo();
        this.loadYearSummary();
        uni.showToast({ title: '登录成功', icon: 'success' });
      }).catch((e) => {
        console.error('登录失败', e);
        uni.showToast({ title: '登录失败，请重试', icon: 'none' });
      });
      // #endif
      // #ifndef MP-WEIXIN
      getApp().relogin().then(() => {
        this.isLoggedIn = true;
        this.loadUserInfo();
        this.loadYearSummary();
        uni.showToast({ title: '登录成功', icon: 'success' });
      }).catch(() => {
        uni.showToast({ title: '登录失败，请重试', icon: 'none' });
      });
      // #endif
    },
    onChooseAvatar(e) {
      const avatarUrl = e?.detail?.avatarUrl || '';
      if (avatarUrl) {
        this.loginForm.avatarUrl = avatarUrl;
      }
    },
    onLoginNickFocus() {
      this.loginNickFocus = true;
      this.$nextTick(() => {
        uni
          .createSelectorQuery()
          .in(this)
          .select('.login-card-guest')
          .boundingClientRect((rect) => {
            if (!rect) return;
            const targetTop = Math.max(rect.top - 24, 0);
            uni.pageScrollTo({
              scrollTop: targetTop,
              duration: 220
            });
          })
          .exec();
      });
    },
    logout() {
      uni.showModal({
        title: '提示',
        content: '确定退出登录吗？',
        success: (res) => {
          if (res.confirm) {
            getApp().logout();
            this.isLoggedIn = false;
            this.userInfo = {};
            this.yearIncome = 0;
            this.yearExpense = 0;
            this.yearBalance = 0;
            uni.showToast({ title: '已退出', icon: 'success' });
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

.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
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

/* 登录卡片 */
.login-card {
  margin: 24rpx 32rpx;
  padding: 36rpx 32rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 24rpx rgba(123, 158, 245, 0.12);
}

.login-card-guest {
  margin-top: -36rpx;
  position: relative;
  z-index: 2;
}

.login-title {
  display: block;
  font-size: 32rpx;
  color: #2D3142;
  font-weight: 600;
}

.login-desc {
  display: block;
  margin-top: 12rpx;
  margin-bottom: 20rpx;
  font-size: 26rpx;
  color: #9CA3AF;
}

.avatar-picker {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  margin: 0 auto 20rpx;
  background: #F2F4FA;
  border: 2rpx solid #E5E9F5;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.avatar-preview {
  width: 100%;
  height: 100%;
  border-radius: 50%;
}

.avatar-picker-text {
  font-size: 24rpx;
  color: #9CA3AF;
}

.nick-input {
  width: 100%;
  height: 84rpx;
  border-radius: 18rpx;
  background: #F5F7FC;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: #2D3142;
  margin-bottom: 22rpx;
  border: 2rpx solid transparent;
  transition: all 0.2s ease;
}

.nick-input-active {
  border-color: rgba(123, 158, 245, 0.5);
  box-shadow: 0 0 0 6rpx rgba(123, 158, 245, 0.08);
}

.nick-tip {
  display: block;
  margin: -8rpx 0 16rpx;
  font-size: 22rpx;
  color: #9CA3AF;
  transition: color 0.2s ease;
}

.nick-tip-active {
  color: #7B9EF5;
}

.login-btn {
  height: 88rpx;
  border-radius: 20rpx;
  background: linear-gradient(135deg, #7B9EF5 0%, #B8A0F5 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-btn-text {
  font-size: 30rpx;
  color: #FFFFFF;
  font-weight: 600;
}

.edit-mask {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.35);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.edit-panel {
  width: 100%;
  background: #FFFFFF;
  border-radius: 28rpx 28rpx 0 0;
  padding: 30rpx 32rpx 34rpx;
}

.edit-title {
  display: block;
  text-align: center;
  font-size: 32rpx;
  font-weight: 700;
  color: #2D3142;
  margin-bottom: 24rpx;
}

.edit-actions {
  display: flex;
  gap: 16rpx;
}

.action-btn {
  flex: 1;
  height: 84rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cancel-btn {
  background: #F3F4F8;
}

.save-btn {
  background: linear-gradient(135deg, #7B9EF5 0%, #B8A0F5 100%);
}

.cancel-text {
  font-size: 28rpx;
  color: #6B7280;
  font-weight: 600;
}

.save-text {
  font-size: 28rpx;
  color: #FFFFFF;
  font-weight: 600;
}
</style>
