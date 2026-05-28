<template>
  <view class="verify-page">
    <view class="form-card">
      <view class="title">学号实名认证</view>

      <view class="field">
        <text class="label">真实姓名</text>
        <input v-model="form.realName" class="input" placeholder="请输入真实姓名" />
      </view>

      <view class="field">
        <text class="label">学号</text>
        <input v-model="form.studentNo" class="input" placeholder="请输入学号" />
      </view>

      <view class="field">
        <text class="label">学院</text>
        <input v-model="form.college" class="input" placeholder="请输入学院" />
      </view>

      <view class="field">
        <text class="label">专业</text>
        <input v-model="form.major" class="input" placeholder="请输入专业" />
      </view>

      <view class="field">
        <text class="label">年级</text>
        <input v-model="form.grade" class="input" placeholder="请输入年级" />
      </view>

      <view class="field">
        <text class="label">身份证后四位</text>
        <input v-model="form.idCardSuffix" class="input" placeholder="后四位数字" maxlength="4" />
      </view>

      <view class="submit-btn" :class="{ loading: loading }" @click="submitVerification">
        <text v-if="!loading">提交认证</text>
        <text v-else>提交中...</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { getCurrentInstance, reactive, ref } from 'vue'

const { proxy } = getCurrentInstance()
const loading = ref(false)
const form = reactive({
  realName: '',
  studentNo: '',
  college: '',
  major: '',
  grade: '',
  idCardSuffix: ''
})

const fieldLabels = {
  realName: '真实姓名', studentNo: '学号', college: '学院',
  major: '专业', grade: '年级', idCardSuffix: '身份证后四位'
}

const submitVerification = async () => {
  if (loading.value) {
    return
  }

  for (const [key, value] of Object.entries(form)) {
    if (!value || !String(value).trim()) {
      uni.showToast({ title: `请输入${fieldLabels[key] || key}`, icon: 'none' })
      return
    }
  }

  loading.value = true
  try {
    const data = await proxy.$request({
      url: '/verification/student',
      method: 'POST',
      data: { ...form }
    })

    const raw = uni.getStorageSync('user_info')
    let current = {}
    try {
      current = raw ? JSON.parse(raw) : {}
    } catch {}

    uni.setStorageSync('user_info', JSON.stringify({
      ...current,
      userId: data.userId,
      nickname: data.nickname,
      avatar: data.avatar,
      role: data.role,
      verifyStatus: data.verifyStatus,
      realName: data.realName,
      studentNo: data.studentNo,
      college: data.college,
      major: data.major,
      grade: data.grade
    }))

    uni.showToast({ title: '认证成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 800)
  } catch (error) {
    const message = error && error.message ? error.message : '认证失败'
    uni.showToast({ title: message, icon: 'none' })
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss">
page {
  background-color: #F7F8FA;
}

.verify-page {
  padding: 16px;
}

.form-card {
  background: #fff;
  border-radius: 14px;
  padding: 18px 16px 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);

  .title {
    font-size: 18px;
    font-weight: 700;
    color: #333;
    margin-bottom: 18px;
  }

  .field {
    margin-bottom: 14px;

    .label {
      display: block;
      font-size: 13px;
      color: #666;
      margin-bottom: 6px;
    }

    .input {
      height: 42px;
      border-radius: 10px;
      background: #f7f8fa;
      padding: 0 12px;
      font-size: 14px;
      color: #333;
    }
  }

  .submit-btn {
    height: 46px;
    background: linear-gradient(90deg, #1890ff, #36cfc9);
    border-radius: 23px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 15px;
    font-weight: 600;
    margin-top: 6px;

    &.loading {
      opacity: 0.7;
    }
  }
}
</style>
