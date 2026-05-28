<template>
  <view class="publish-container">
    <!-- 图片上传卡片 -->
    <view class="publish-card">
      <view class="card-title">商品图片</view>
      <u-upload
        :fileList="fileList"
        @afterRead="afterRead"
        @delete="deletePic"
        name="1"
        multiple
        :maxCount="9"
        width="100"
        height="100"
      ></u-upload>
      <text class="upload-tip">最多上传9张图片，首张作为封面</text>
    </view>

    <!-- 基本信息卡片 -->
    <view class="publish-card">
      <view class="card-title">基本信息</view>
      <u-form :model="form">
        <u-form-item borderBottom>
          <u-input v-model="form.title" placeholder="请输入商品标题" border="none" maxlength="30" fontSize="16"></u-input>
        </u-form-item>

        <u-form-item borderBottom>
          <u-textarea v-model="form.description" placeholder="详细描述一下你的闲置吧..." border="none" count></u-textarea>
        </u-form-item>

        <u-form-item label="分类" borderBottom @click="showCategory = true">
          <u-input v-model="form.categoryName" disabled placeholder="请选择商品分类" border="none" inputAlign="right"></u-input>
          <template #right>
            <u-icon name="arrow-right"></u-icon>
          </template>
        </u-form-item>
      </u-form>
    </view>

    <!-- 定价卡片 -->
    <view class="publish-card">
      <view class="card-title">定价</view>
      <u-form :model="form">
        <u-form-item label="价格">
          <u-input v-model="form.price" type="digit" placeholder="0.00" border="none" inputAlign="right">
            <template #prefix>
              <text class="price-prefix">¥</text>
            </template>
          </u-input>
        </u-form-item>
      </u-form>
    </view>

    <!-- 面交地点卡片 -->
    <view class="publish-card">
      <view class="card-title">面交地点</view>
      <u-form :model="form">
        <u-form-item @click="openLocationPicker">
          <u-input
            v-model="form.pickupPlaceName"
            disabled
            placeholder="请选择面交地点"
            border="none"
            inputAlign="right"
          ></u-input>
          <template #right>
            <u-icon name="arrow-right"></u-icon>
          </template>
        </u-form-item>
      </u-form>
    </view>

    <!-- 底部发布按钮 -->
    <view class="bottom-btn">
      <view class="publish-submit-btn" @click="submitPublish">
        <text class="publish-submit-text">确认发布</text>
      </view>
    </view>

    <u-picker
      :show="showCategory"
      :columns="pickerColumns"
      @confirm="confirmCategory"
      @cancel="showCategory = false"
    ></u-picker>

    <custom-tabbar :current="2" />
  </view>
</template>

<script setup>
import { computed, getCurrentInstance, onMounted, onUnmounted, ref } from 'vue'
import { API_ORIGIN, normalizeImage as normalizeImageUrl } from '@/utils/config'
import customTabbar from '@/components/custom-tabbar.vue'

const { proxy } = getCurrentInstance()
const fileList = ref([])
const categories = ref([])
const showCategory = ref(false)

const form = ref({
  title: '',
  description: '',
  price: '',
  categoryName: '',
  categoryId: null,
  pickupPlaceName: '',
  pickupAddress: '',
  pickupLat: null,
  pickupLng: null
})

const pickerColumns = computed(() => [categories.value.map(item => item.name)])


const loadCategories = async () => {
  categories.value = await proxy.$request({ url: '/category/list', method: 'GET' })
}

const doUploadFile = (filePath) => {
  const token = uni.getStorageSync('user_token') || ''
  if (!token) {
    return Promise.reject(new Error('请先登录'))
  }
  return new Promise((resolve, reject) => {
    const uploadTask = uni.uploadFile({
      url: `${API_ORIGIN}/api/file/upload`,
      filePath,
      name: 'file',
      timeout: 30000,
      header: {
        Authorization: token
      },
      success: (res) => {
        try {
          const body = JSON.parse(res.data || '{}')
          if (res.statusCode === 200 && body.code === 200) {
            resolve(body.data)
          } else if (res.statusCode === 401) {
            uni.removeStorageSync('user_token')
            reject(new Error('登录已过期，请重新登录'))
          } else {
            reject(new Error(body.msg || `上传失败(${res.statusCode})`))
          }
        } catch {
          reject(new Error('上传响应解析失败'))
        }
      },
      fail: (err) => {
        console.error('Upload fail:', filePath, err)
        reject(new Error(err?.errMsg || '网络连接失败，请检查「不校验合法域名」是否开启'))
      }
    })
  })
}

const compressImage = (filePath) => {
  return new Promise((resolve) => {
    const timer = setTimeout(() => resolve(filePath), 3000)
    uni.compressImage({
      src: filePath,
      quality: 80,
      success: (res) => { clearTimeout(timer); resolve(res.tempFilePath) },
      fail: () => { clearTimeout(timer); resolve(filePath) }
    })
  })
}

const uploadSingleFile = async (filePath) => {
  // 先压缩图片
  const compressedPath = await compressImage(filePath)
  try {
    return await doUploadFile(compressedPath)
  } catch (firstError) {
    // 失败后自动重试1次
    try {
      return await doUploadFile(compressedPath)
    } catch (retryError) {
      throw retryError
    }
  }
}

const afterRead = async (event) => {
  const lists = [].concat(event.file)
  const startIdx = fileList.value.length
  lists.forEach(item => {
    fileList.value.push({
      ...item,
      status: 'uploading',
      message: '上传中...',
      serverUrl: ''
    })
  })
  for (let i = 0; i < lists.length; i++) {
    const idx = startIdx + i
    try {
      const rawUrl = await uploadSingleFile(lists[i].url)
      fileList.value[idx].status = 'success'
      fileList.value[idx].message = ''
      fileList.value[idx].url = normalizeImageUrl(rawUrl)
      fileList.value[idx].serverUrl = rawUrl
    } catch (error) {
      fileList.value[idx].status = 'failed'
      fileList.value[idx].message = '点击重试'
      uni.showToast({ title: error.message || '上传失败，请重试', icon: 'none' })
    }
  }
}

const deletePic = (event) => {
  fileList.value.splice(event.index, 1)
}

const confirmCategory = (event) => {
  const selectedName = event.value[0]
  const selected = categories.value.find(item => item.name === selectedName)
  form.value.categoryName = selected?.name || ''
  form.value.categoryId = selected?.id ?? null
  showCategory.value = false
}

const openLocationPicker = () => {
  const token = uni.getStorageSync('user_token')
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  uni.navigateTo({
    url: '/pages/location-picker/location-picker'
  })
}

const onPickLocation = (location) => {
  form.value.pickupPlaceName = location.placeName || ''
  form.value.pickupAddress = location.address || ''
  form.value.pickupLat = location.lat ?? null
  form.value.pickupLng = location.lng ?? null
}

const submitPublish = async () => {
  if (!form.value.title.trim()) {
    uni.showToast({ title: '请输入商品标题', icon: 'none' })
    return
  }
  if (!form.value.categoryId) {
    uni.showToast({ title: '请选择商品分类', icon: 'none' })
    return
  }
  if (!form.value.price) {
    uni.showToast({ title: '请输入价格', icon: 'none' })
    return
  }
  if (!form.value.pickupPlaceName || form.value.pickupLat == null || form.value.pickupLng == null) {
    uni.showToast({ title: '请选择面交地点', icon: 'none' })
    return
  }

  const uploadedImages = fileList.value.map(item => item.serverUrl).filter(Boolean)
  if (uploadedImages.length === 0) {
    uni.showToast({ title: '请至少上传1张图片', icon: 'none' })
    return
  }
  if (fileList.value.some(item => item.status === 'uploading')) {
    uni.showToast({ title: '图片上传中，请稍后', icon: 'none' })
    return
  }

  uni.showLoading({ title: '发布中...' })
  try {
    const res = await proxy.$request({
      url: '/product/publish',
      method: 'POST',
      data: {
        title: form.value.title.trim(),
        description: form.value.description,
        images: uploadedImages.join(','),
        price: Number(form.value.price),
        categoryId: form.value.categoryId,
        pickupPlaceName: form.value.pickupPlaceName,
        pickupAddress: form.value.pickupAddress,
        pickupLat: form.value.pickupLat,
        pickupLng: form.value.pickupLng,
        stock: 1
      }
    })

    uni.hideLoading()
    uni.showToast({
      title: typeof res === 'string' ? res : '发布成功',
      icon: 'none'
    })
    setTimeout(() => {
      form.value = {
        title: '',
        description: '',
        price: '',
        categoryName: '',
        categoryId: null,
        pickupPlaceName: '',
        pickupAddress: '',
        pickupLat: null,
        pickupLng: null
      }
      fileList.value = []
      uni.switchTab({ url: '/pages/index/index' })
    }, 1200)
  } catch (error) {
    uni.hideLoading()
    uni.showToast({ title: error?.message || '发布失败', icon: 'none' })
  }
}

onMounted(() => {
  loadCategories()
  uni.$on('pickLocation', onPickLocation)
})

onUnmounted(() => {
  uni.$off('pickLocation', onPickLocation)
})
</script>

<style lang="scss">
page {
  background-color: #F7F8FA;
}

.publish-container {
  padding: 24rpx;
  padding-bottom: calc(320rpx + env(safe-area-inset-bottom));
}

.publish-card {
  background-color: #FFFFFF;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;

  .card-title {
    font-size: 30rpx;
    font-weight: bold;
    color: #1A1A1A;
    margin-bottom: 20rpx;
  }

  .upload-tip {
    display: block;
    font-size: 24rpx;
    color: #999;
    margin-top: 16rpx;
  }

  :deep(.u-textarea) {
    padding: 10px 0;
    min-height: 100px;
  }
}

.price-prefix {
  margin-right: 5px;
  color: #FF6633;
  font-weight: bold;
  font-size: 18px;
}

.bottom-btn {
  position: fixed;
  bottom: calc(150rpx + env(safe-area-inset-bottom));
  left: 0;
  right: 0;
  padding: 20rpx 40rpx;
  background-color: #FFFFFF;
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.06);
  z-index: 998;
}

.publish-submit-btn {
  height: 88rpx;
  background: linear-gradient(135deg, #00B578, #00D68F);
  border-radius: 50rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(0, 181, 120, 0.25);
}

.publish-submit-text {
  font-size: 32rpx;
  color: #fff;
  font-weight: bold;
  letter-spacing: 2rpx;
}
</style>
