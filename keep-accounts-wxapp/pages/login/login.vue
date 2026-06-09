<template>
  <view class="page">
    <view class="card">
      <text class="title">轻记账</text>
      <text class="subtitle">退出后请重新登录</text>
      <button class="login-btn" @click="handleLogin">微信登录</button>
    </view>
  </view>
</template>

<script>
export default {
  methods: {
    async handleLogin() {
      try {
        await getApp().relogin();
        uni.showToast({ title: '登录成功', icon: 'success' });
        setTimeout(() => {
          uni.switchTab({ url: '/pages/index/index' });
        }, 300);
      } catch (e) {
        console.error('登录失败', e);
        uni.showToast({
          title: e?.message || '登录失败，请重试',
          icon: 'none'
        });
      }
    }
  }
};
</script>

<style lang="scss">
.page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32rpx;
  background: linear-gradient(180deg, #EFF2FB 0%, #F4F6FC 40%, #FAFBFE 100%);
  overflow: hidden;
}

.page::before {
  content: '';
  position: absolute;
  top: -160rpx;
  right: -120rpx;
  width: 480rpx;
  height: 480rpx;
  background: radial-gradient(circle, rgba(184, 160, 245, 0.30) 0%, rgba(184, 160, 245, 0) 70%);
  border-radius: 50%;
  pointer-events: none;
}

.page::after {
  content: '';
  position: absolute;
  bottom: -180rpx;
  left: -120rpx;
  width: 480rpx;
  height: 480rpx;
  background: radial-gradient(circle, rgba(123, 158, 245, 0.28) 0%, rgba(123, 158, 245, 0) 70%);
  border-radius: 50%;
  pointer-events: none;
}

.card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 560rpx;
  background: #FFFFFF;
  border-radius: 28rpx;
  box-shadow: 0 4rpx 8rpx rgba(45, 49, 66, 0.04), 0 20rpx 60rpx rgba(123, 158, 245, 0.20);
  padding: 48rpx 36rpx;
  text-align: center;
}

.card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 32rpx;
  right: 32rpx;
  height: 1rpx;
  background: linear-gradient(90deg, transparent 0%, rgba(255,255,255,0.9) 50%, transparent 100%);
}

.title {
  display: block;
  font-size: 44rpx;
  font-weight: 700;
  color: #2D3142;
  letter-spacing: 1rpx;
}

.subtitle {
  display: block;
  margin-top: 16rpx;
  margin-bottom: 40rpx;
  font-size: 28rpx;
  color: #9CA3AF;
}

.login-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 20rpx;
  border: none;
  background: linear-gradient(135deg, #7B9EF5 0%, #B8A0F5 100%);
  color: #FFFFFF;
  font-size: 30rpx;
  font-weight: 600;
  box-shadow:
    inset 0 1rpx 0 rgba(255, 255, 255, 0.3),
    inset 0 -2rpx 6rpx rgba(91, 130, 224, 0.18),
    0 8rpx 24rpx rgba(123, 158, 245, 0.32);
}

.login-btn:active {
  transform: scale(0.98);
  box-shadow:
    inset 0 1rpx 0 rgba(255, 255, 255, 0.25),
    0 4rpx 12rpx rgba(91, 130, 224, 0.25);
}
</style>
