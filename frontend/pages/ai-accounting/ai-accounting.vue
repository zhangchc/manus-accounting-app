<template>
  <view class="page">
    <view class="hero-card">
      <text class="hero-title">AI 智能记账</text>
      <text class="hero-desc">输入一句话，自动识别金额、分类和备注</text>
      <text class="hero-example">例如：今天午餐花了38元</text>
    </view>

    <view class="input-card">
      <textarea
        v-model="rawText"
        class="raw-input"
        maxlength="120"
        placeholder="请输入记账内容..."
        placeholder-class="raw-placeholder"
      />
      <view class="action-row">
        <view class="ghost-btn" @click="mockVoiceInput">
          <text class="ghost-btn-text">语音输入</text>
        </view>
        <view class="main-btn" @click="analyzeText">
          <text class="main-btn-text">智能解析</text>
        </view>
      </view>
    </view>

    <view class="result-card" v-if="parsed">
      <text class="result-title">解析结果</text>
      <view class="field-row">
        <text class="field-label">类型</text>
        <text class="field-value">{{ parsed.type === 1 ? '支出' : '收入' }}</text>
      </view>
      <view class="field-row">
        <text class="field-label">金额</text>
        <text class="field-value">¥{{ parsed.amount }}</text>
      </view>
      <view class="field-row">
        <text class="field-label">分类</text>
        <text class="field-value">{{ parsed.categoryName }}</text>
      </view>
      <view class="field-row">
        <text class="field-label">备注</text>
        <text class="field-value">{{ parsed.remark || '-' }}</text>
      </view>
      <view class="save-btn" @click="saveRecordByAI">
        <text class="save-btn-text">一键入账</text>
      </view>
    </view>
  </view>
</template>

<script>
import { getCategoryList, addRecord } from '../../api/index';
import { getCurrentDate, getCurrentTime } from '../../utils/util';

export default {
  data() {
    return {
      rawText: '',
      parsed: null,
      expenseCategories: [],
      incomeCategories: []
    };
  },
  async onLoad() {
    await this.prepareCategories();
  },
  methods: {
    async prepareCategories() {
      const loggedIn = await getApp().ensureLogin();
      if (!loggedIn) {
        this.expenseCategories = this.getDefaultCategories(1);
        this.incomeCategories = this.getDefaultCategories(2);
        return;
      }
      try {
        const [expRes, incRes] = await Promise.all([
          getCategoryList(1),
          getCategoryList(2)
        ]);
        this.expenseCategories = expRes.data?.length ? expRes.data : this.getDefaultCategories(1);
        this.incomeCategories = incRes.data?.length ? incRes.data : this.getDefaultCategories(2);
      } catch (e) {
        this.expenseCategories = this.getDefaultCategories(1);
        this.incomeCategories = this.getDefaultCategories(2);
      }
    },
    getDefaultCategories(type) {
      const expenseNames = ['餐饮', '交通', '购物', '日用', '水果', '零食', '运动', '娱乐', '通讯', '服饰', '美容', '住房', '居家', '孩子', '长辈', '社交', '旅行', '宠物', '医疗', '学习', '其他'];
      const incomeNames = ['工资', '奖金', '兼职', '理财', '红包', '转账', '退款', '其他'];
      const names = type === 1 ? expenseNames : incomeNames;
      return names.map((name, idx) => ({ id: type * 1000 + idx + 1, name, type }));
    },
    mockVoiceInput() {
      uni.showToast({ title: '语音识别接入中，可先文本输入', icon: 'none' });
    },
    analyzeText() {
      const text = (this.rawText || '').trim();
      if (!text) {
        uni.showToast({ title: '请先输入内容', icon: 'none' });
        return;
      }

      const amountMatch = text.match(/(\d+(\.\d{1,2})?)/);
      const amount = amountMatch ? parseFloat(amountMatch[1]) : 0;
      if (!amount || amount <= 0) {
        uni.showToast({ title: '未识别到有效金额', icon: 'none' });
        return;
      }

      const isIncome = /(收入|工资|奖金|退款|红包|转账|理财|兼职|到账)/.test(text);
      const type = isIncome ? 2 : 1;
      const list = type === 1 ? this.expenseCategories : this.incomeCategories;

      const hit = list.find(item => text.includes(item.name));
      const category = hit || list.find(item => item.name === '其他') || list[0];

      this.parsed = {
        type,
        amount,
        categoryId: category?.id,
        categoryName: category?.name || '其他',
        remark: text
          .replace(/(\d+(\.\d{1,2})?)/, '')
          .replace(/(元|块|人民币)/g, '')
          .trim()
      };
    },
    async saveRecordByAI() {
      if (!this.parsed) {
        uni.showToast({ title: '请先解析内容', icon: 'none' });
        return;
      }
      const loggedIn = !!uni.getStorageSync('token');
      if (!loggedIn) {
        uni.showToast({ title: '请先登录后再保存', icon: 'none' });
        return;
      }
      try {
        await addRecord({
          categoryId: this.parsed.categoryId,
          type: this.parsed.type,
          amount: this.parsed.amount,
          remark: this.parsed.remark,
          recordDate: getCurrentDate(),
          recordTime: getCurrentTime()
        });
        uni.showToast({ title: 'AI记账成功', icon: 'success' });
        setTimeout(() => {
          uni.navigateBack();
        }, 400);
      } catch (e) {
        uni.showToast({ title: '保存失败，请重试', icon: 'none' });
      }
    }
  }
};
</script>

<style lang="scss">
.page {
  min-height: 100vh;
  background: #FAFBFE;
  padding: 24rpx 32rpx 36rpx;
}

.hero-card {
  background: linear-gradient(135deg, #7B9EF5 0%, #B8A0F5 100%);
  border-radius: 24rpx;
  padding: 28rpx;
  color: #FFFFFF;
  box-shadow: 0 8rpx 30rpx rgba(123, 158, 245, 0.25);
}

.hero-title {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
}

.hero-desc {
  display: block;
  margin-top: 10rpx;
  font-size: 26rpx;
  opacity: 0.95;
}

.hero-example {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  opacity: 0.85;
}

.input-card, .result-card {
  margin-top: 24rpx;
  background: #FFFFFF;
  border-radius: 22rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 16rpx rgba(123, 158, 245, 0.08);
}

.raw-input {
  width: 100%;
  min-height: 180rpx;
  font-size: 28rpx;
  color: #2D3142;
  background: #F5F7FC;
  border-radius: 18rpx;
  padding: 18rpx 20rpx;
}

.raw-placeholder {
  color: #B8BECC;
}

.action-row {
  display: flex;
  gap: 16rpx;
  margin-top: 20rpx;
}

.ghost-btn, .main-btn, .save-btn {
  height: 84rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ghost-btn {
  flex: 1;
  background: #F0F3FB;
}

.ghost-btn-text {
  font-size: 28rpx;
  color: #6B7280;
  font-weight: 600;
}

.main-btn, .save-btn {
  flex: 1;
  background: linear-gradient(135deg, #7B9EF5 0%, #B8A0F5 100%);
}

.main-btn-text, .save-btn-text {
  font-size: 28rpx;
  color: #FFFFFF;
  font-weight: 600;
}

.result-title {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: #2D3142;
  margin-bottom: 16rpx;
}

.field-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14rpx 0;
  border-bottom: 1rpx solid #F3F4F8;
}

.field-row:last-of-type {
  border-bottom: none;
}

.field-label {
  font-size: 26rpx;
  color: #9CA3AF;
}

.field-value {
  font-size: 28rpx;
  color: #2D3142;
  font-weight: 600;
}

.save-btn {
  margin-top: 24rpx;
}
</style>
