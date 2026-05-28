<template>
  <div class="page-layout">
    <AppHeader />
    <main class="main-content">
      <div class="content-wrap">
        <div class="verify-card">
          <div class="verify-header">
            <span class="verify-icon">🎓</span>
            <h2 class="verify-title">学生认证</h2>
            <p class="verify-desc">完成认证后享受更多信任权益</p>
          </div>

          <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="verify-form">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入真实姓名" />
            </el-form-item>
            <el-form-item label="学号" prop="studentNo">
              <el-input v-model="form.studentNo" placeholder="请输入学号" />
            </el-form-item>
            <el-form-item label="学院" prop="college">
              <el-input v-model="form.college" placeholder="请输入学院" />
            </el-form-item>
            <el-form-item label="专业" prop="major">
              <el-input v-model="form.major" placeholder="请输入专业" />
            </el-form-item>
            <el-form-item label="年级" prop="grade">
              <el-input v-model="form.grade" placeholder="如：2023级" />
            </el-form-item>
            <el-form-item label="身份证后四位" prop="idCardSuffix">
              <el-input v-model="form.idCardSuffix" placeholder="后四位数字" maxlength="4" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleSubmit">
                提交认证
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AppHeader from '@/components/AppHeader.vue'
import { verifyStudent } from '@/api/verification'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const user = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  realName: '',
  studentNo: '',
  college: '',
  major: '',
  grade: '',
  idCardSuffix: ''
})

const rules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  college: [{ required: true, message: '请输入学院', trigger: 'blur' }],
  major: [{ required: true, message: '请输入专业', trigger: 'blur' }],
  grade: [{ required: true, message: '请输入年级', trigger: 'blur' }],
  idCardSuffix: [
    { required: true, message: '请输入身份证后四位', trigger: 'blur' },
    { len: 4, message: '请输入4位数字', trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch { return }

  loading.value = true
  try {
    await verifyStudent({ ...form })
    const newInfo = { ...user.userInfo, verifyStatus: 'VERIFIED', realName: form.realName }
    user.userInfo = newInfo
    localStorage.setItem('user_info', JSON.stringify(newInfo))
    ElMessage.success('认证提交成功')
    router.push('/profile')
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page-layout {
  min-height: 100vh;
  background: var(--bg);
}

.main-content {
  padding-top: var(--header-height);
}

.content-wrap {
  max-width: 520px;
  margin: 40px auto;
  padding: 0 24px;
}

.verify-card {
  background: #fff;
  border-radius: 20px;
  padding: 40px 36px;
  box-shadow: var(--shadow);
}

.verify-header {
  text-align: center;
  margin-bottom: 32px;
}

.verify-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 12px;
}

.verify-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.verify-desc {
  font-size: 14px;
  color: var(--text-secondary);
}

.verify-form :deep(.el-form-item__label) {
  font-weight: 500;
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 50px;
  background: linear-gradient(135deg, #1890ff, #36cfc9);
  border: none;
}

.submit-btn:hover {
  background: linear-gradient(135deg, #40a9ff, #5cdbd3);
}
</style>
