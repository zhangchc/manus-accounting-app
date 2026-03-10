<script>
export default {
  onLaunch() {
    console.log('App Launch');
    // 检查登录状态
    const token = uni.getStorageSync('token');
    if (!token) {
      this.autoLogin();
    }
  },
  methods: {
    autoLogin() {
      // 开发模式自动登录
      uni.request({
        url: this.globalData.baseUrl + '/user/login',
        method: 'POST',
        data: {
          code: 'test_user_001',
          nickName: '记账用户',
          avatarUrl: ''
        },
        success: (res) => {
          if (res.data.code === 200) {
            uni.setStorageSync('token', res.data.data.token);
            uni.setStorageSync('userInfo', res.data.data.userInfo);
          }
        }
      });
    }
  },
  globalData: {
    baseUrl: 'http://localhost:8080/api'
  }
};
</script>

<style>
/* 全局样式 */
page {
  background-color: #F8F9FE;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif;
  font-size: 28rpx;
  color: #2D3142;
  box-sizing: border-box;
}

view, text, image {
  box-sizing: border-box;
}

/* 清除按钮默认样式 */
button {
  padding: 0;
  margin: 0;
  background: none;
  border: none;
  line-height: inherit;
  border-radius: 0;
}

button::after {
  border: none;
}

/* 安全区域 */
.safe-area-bottom {
  padding-bottom: env(safe-area-inset-bottom);
}

/* 通用flex布局 */
.flex-row {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.flex-col {
  display: flex;
  flex-direction: column;
}

.flex-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.flex-center {
  display: flex;
  justify-content: center;
  align-items: center;
}

.flex-1 {
  flex: 1;
}

/* 文字省略 */
.text-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
