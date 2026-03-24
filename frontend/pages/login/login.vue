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
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32rpx;
  background: #FAFBFE;
}

.card {
  width: 100%;
  max-width: 560rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(123, 158, 245, 0.12);
  padding: 48rpx 36rpx;
  text-align: center;
}

.title {
  display: block;
  font-size: 44rpx;
  font-weight: 700;
  color: #2D3142;
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
}
</style>
