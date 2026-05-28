<template>
  <div class="page-layout">
    <AppHeader />
    <main class="main-content">
      <div class="content-wrap">
        <h2 class="page-title">发布闲置</h2>

        <div class="publish-card">
          <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
            <!-- 图片上传 -->
            <div class="form-section">
              <h3 class="section-title">商品图片</h3>
              <el-upload
                :file-list="fileList"
                list-type="picture-card"
                :http-request="customUpload"
                :before-upload="beforeUpload"
                :on-remove="handleRemove"
                :limit="9"
                :on-exceed="handleExceed"
                accept="image/*"
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
              <p class="upload-tip">最多上传9张图片，首张作为封面</p>
            </div>

            <!-- 基本信息 -->
            <div class="form-section">
              <h3 class="section-title">基本信息</h3>
              <el-form-item label="商品标题" prop="title">
                <el-input v-model="form.title" placeholder="请输入商品标题" maxlength="30" show-word-limit />
              </el-form-item>
              <el-form-item label="商品描述" prop="description">
                <el-input v-model="form.description" type="textarea" :rows="4" placeholder="详细描述一下你的闲置吧..." maxlength="500" show-word-limit />
              </el-form-item>
              <el-form-item label="商品分类" prop="categoryId">
                <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
                  <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
                </el-select>
              </el-form-item>
            </div>

            <!-- 定价 -->
            <div class="form-section">
              <h3 class="section-title">定价</h3>
              <el-form-item label="价格（元）" prop="price">
                <el-input v-model="form.price" placeholder="0.00" type="number" step="0.01">
                  <template #prefix>
                    <span class="price-prefix">¥</span>
                  </template>
                </el-input>
              </el-form-item>
            </div>

            <!-- 面交地点 -->
            <div class="form-section">
              <h3 class="section-title">面交地点</h3>
              <el-form-item label="地点名称" prop="pickupPlaceName">
                <el-input v-model="form.pickupPlaceName" placeholder="如：教学楼B座门口" />
              </el-form-item>
              <el-form-item label="详细地址">
                <el-input v-model="form.pickupAddress" placeholder="可选，补充详细地址" />
              </el-form-item>
            </div>

            <!-- 提交 -->
            <div class="submit-row">
              <el-button type="primary" size="large" class="submit-btn" :loading="submitting" @click="handleSubmit">
                确认发布
              </el-button>
            </div>
          </el-form>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AppHeader from '@/components/AppHeader.vue'
import { publishProduct, uploadFile } from '@/api/publish'
import { getCategoryList } from '@/api/category'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)
const categories = ref([])
const fileList = ref([])
const uploadedUrls = ref([])

const form = reactive({
  title: '',
  description: '',
  price: '',
  categoryId: null,
  pickupPlaceName: '',
  pickupAddress: ''
})

const rules = {
  title: [{ required: true, message: '请输入商品标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  pickupPlaceName: [{ required: true, message: '请输入面交地点', trigger: 'blur' }]
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isImage) { ElMessage.error('只能上传图片文件'); return false }
  if (!isLt10M) { ElMessage.error('图片大小不能超过10MB'); return false }
  return true
}

const customUpload = async ({ file, onSuccess, onError }) => {
  try {
    const url = await uploadFile(file)
    uploadedUrls.value.push(url)
    onSuccess(url)
  } catch (e) {
    onError(e)
    ElMessage.error('图片上传失败，请重试')
  }
}

const handleRemove = (file) => {
  const url = file.response || file.url
  uploadedUrls.value = uploadedUrls.value.filter(u => u !== url)
}

const handleExceed = () => {
  ElMessage.warning('最多上传9张图片')
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch { return }

  if (uploadedUrls.value.length === 0) {
    ElMessage.warning('请至少上传1张商品图片')
    return
  }

  const price = Number(form.price)
  if (isNaN(price) || price <= 0) {
    ElMessage.warning('请输入有效的价格')
    return
  }

  submitting.value = true
  try {
    await publishProduct({
      title: form.title.trim(),
      description: form.description,
      images: uploadedUrls.value.join(','),
      price,
      categoryId: form.categoryId,
      pickupPlaceName: form.pickupPlaceName,
      pickupAddress: form.pickupAddress,
      pickupLat: 0,
      pickupLng: 0,
      stock: 1
    })
    ElMessage.success('发布成功')
    router.push('/')
  } catch {
    // error handled by interceptor
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  try {
    const res = await getCategoryList()
    categories.value = res || []
  } catch {}
})
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
  max-width: 720px;
  margin: 0 auto;
  padding: 28px 24px 72px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 24px;
}

.publish-card {
  background: #fff;
  border-radius: 20px;
  padding: 36px;
  box-shadow: var(--shadow);
}

.form-section {
  margin-bottom: 32px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 18px;
  padding-left: 12px;
  border-left: 3px solid var(--primary);
}

.upload-tip {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 8px;
}

.price-prefix {
  color: var(--accent);
  font-weight: 700;
  font-size: 16px;
}

.submit-row {
  padding-top: 16px;
}

.submit-btn {
  width: 100%;
  height: 52px;
  font-size: 17px;
  font-weight: 700;
  border-radius: 50px;
  background: linear-gradient(135deg, #FF6600, #FF8533);
  border: none;
  letter-spacing: 1px;
  box-shadow: 0 8px 24px rgba(255, 102, 0, 0.3);
}

.submit-btn:hover {
  background: linear-gradient(135deg, #FF8533, #FFA366);
}

:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
  border-radius: 12px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 100px;
  height: 100px;
  border-radius: 12px;
}
</style>
