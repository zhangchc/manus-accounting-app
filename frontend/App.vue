<script>
import { currentEnv } from './config/env';

export default {
  onLaunch() {
    console.log('App Launch');
    this.globalData.loggedOut = !!uni.getStorageSync('loggedOut');
    const token = uni.getStorageSync('token');
    if (!token && !this.globalData.loggedOut) {
      // 启动时即开始登录，各页通过 ensureLogin() 等待完成后再请求数据
      this.ensureLogin();
    }
  },
  methods: {
    /**
     * 自动登录：小程序用 wx 的 code 调 wxLogin，其他用开发登录
     * @returns {Promise<void>}
     */
    autoLogin(profile = {}) {
      // #ifdef MP-WEIXIN
      return new Promise((resolve, reject) => {
        uni.login({
          provider: 'weixin',
          success: (loginRes) => {
            if (!loginRes.code) {
              reject(new Error('获取微信 code 失败'));
              return;
            }
            uni.request({
              url: this.globalData.baseUrl + '/user/wxLogin',
              method: 'POST',
              data: (() => {
                const payload = {
                  code: loginRes.code
                };
                const nickName = (profile.nickName || '').trim();
                const avatarUrl = (profile.avatarUrl || '').trim();
                // 静默登录不传空字符串，避免覆盖后端已有资料
                if (nickName) payload.nickName = nickName;
                if (avatarUrl) payload.avatarUrl = avatarUrl;
                return payload;
              })(),
              success: (res) => {
                if (res.data && res.data.code === 200 && res.data.data) {
                  uni.setStorageSync('token', res.data.data.token);
                  uni.setStorageSync('userInfo', res.data.data.userInfo || {});
                  this.globalData.loggedOut = false;
                  uni.removeStorageSync('loggedOut');
                  resolve();
                } else {
                  reject(new Error(res.data?.message || '微信登录失败'));
                }
              },
              fail: (err) => reject(err)
            });
          },
          fail: (err) => reject(err)
        });
      });
      // #endif
      // #ifndef MP-WEIXIN
      return new Promise((resolve, reject) => {
        uni.request({
          url: this.globalData.baseUrl + '/user/login',
          method: 'POST',
          data: {
            code: 'test_user_001',
            nickName: '小明',
            avatarUrl: ''
          },
          success: (res) => {
            if (res.data && res.data.code === 200 && res.data.data) {
              uni.setStorageSync('token', res.data.data.token);
              uni.setStorageSync('userInfo', res.data.data.userInfo || {});
              this.globalData.loggedOut = false;
              uni.removeStorageSync('loggedOut');
              resolve();
            } else {
              reject(new Error(res.data?.message || '登录失败'));
            }
          },
          fail: (err) => reject(err)
        });
      });
      // #endif
    },

    validateSession(token) {
      return new Promise((resolve, reject) => {
        uni.request({
          url: this.globalData.baseUrl + '/user/info',
          method: 'GET',
          header: {
            'Content-Type': 'application/json',
            'Authorization': token ? 'Bearer ' + token : ''
          },
          success: (res) => {
            if (res?.data?.code === 200 && res?.data?.data) {
              uni.setStorageSync('userInfo', res.data.data);
              resolve(true);
              return;
            }
            reject(new Error('SESSION_INVALID'));
          },
          fail: () => reject(new Error('SESSION_INVALID'))
        });
      });
    },
    /**
     * 确保已登录：有 token 直接 resolve，否则执行并等待 autoLogin
     * 各页在请求前调用 await getApp().ensureLogin()
     */
    ensureLogin(force = false) {
      const token = uni.getStorageSync('token');
      if (token) {
        if (!this.globalData.sessionCheckPromise) {
          this.globalData.sessionCheckPromise = this.validateSession(token)
            .finally(() => {
              this.globalData.sessionCheckPromise = null;
            });
        }
        return this.globalData.sessionCheckPromise
          .then(() => true)
          .catch(() => {
            // token存在但会话无效（用户被删/过期），回退到未登录态
            this.logout();
            return false;
          });
      }
      if (this.globalData.loggedOut && !force) {
        return Promise.resolve(false);
      }
      if (!this.globalData.loginPromise) {
        this.globalData.loginPromise = this.autoLogin().finally(() => {
          this.globalData.loginPromise = null;
        });
      }
      return this.globalData.loginPromise
        .then(() => true)
        .catch(() => false);
    },
    logout() {
      uni.removeStorageSync('token');
      uni.removeStorageSync('userInfo');
      uni.setStorageSync('loggedOut', true);
      this.globalData.loggedOut = true;
      this.globalData.loginPromise = null;
      this.globalData.sessionCheckPromise = null;
    },
    relogin(profile = {}) {
      this.globalData.loggedOut = false;
      uni.removeStorageSync('loggedOut');
      this.globalData.loginPromise = this.autoLogin(profile).finally(() => {
        this.globalData.loginPromise = null;
      });
      return this.globalData.loginPromise;
    }
  },
  globalData: {
    baseUrl: currentEnv.baseUrl,
    loginPromise: null,
    sessionCheckPromise: null,
    loggedOut: false
  }
};
</script>

<style>
/* 全局样式 - 蓝紫色主题 */
page {
  background-color: #FAFBFE;
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
