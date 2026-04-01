<template>
  <view class="publish-container">
    <!-- 图片上传区 -->
    <view class="upload-section">
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

    <!-- 基础信息表单 -->
    <view class="form-section">
      <u-form :model="form" ref="uForm">
        <u-form-item borderBottom>
          <u-input v-model="form.title" placeholder="请输入商品标题（限30字）" border="none" maxlength="30" fontSize="16"></u-input>
        </u-form-item>
        
        <u-form-item borderBottom>
          <u-textarea v-model="form.desc" placeholder="详细描述一下你的闲置吧，比如购买渠道、使用感受、新旧程度..." border="none" count></u-textarea>
        </u-form-item>
        
        <u-form-item label="分类" borderBottom @click="showCategory = true">
          <u-input v-model="form.categoryName" disabled placeholder="请选择商品分类" border="none" inputAlign="right"></u-input>
          <template #right>
            <u-icon name="arrow-right"></u-icon>
          </template>
        </u-form-item>
        
        <u-form-item label="价格" borderBottom>
          <u-input v-model="form.price" type="digit" placeholder="0.00" border="none" inputAlign="right">
            <template #prefix>
              <text style="margin-right: 5px; color: #FF4D4F; font-weight: bold;">¥</text>
            </template>
          </u-input>
        </u-form-item>
      </u-form>
    </view>

    <!-- 底部发布按钮 -->
    <view class="bottom-btn">
      <u-button type="primary" shape="circle" text="确认发布" @click="submitPublish"></u-button>
    </view>

    <!-- 分类选择器 -->
    <u-picker :show="showCategory" :columns="columns" @confirm="confirmCategory" @cancel="showCategory = false"></u-picker>
    <u-toast ref="uToast"></u-toast>
  </view>
</template>

<script setup>
import { ref, getCurrentInstance } from 'vue'

const { proxy } = getCurrentInstance()
const uToast = ref(null)
const fileList = ref([])
const showCategory = ref(false)

const form = ref({
  title: '',
  description: '', // 改为和后端实体类一致的字段名
  price: '',
  categoryName: '',
  categoryId: ''
})

const columns = [
  ['数码产品', '书籍教材', '衣物鞋帽', '代步工具', '生活日用', '其他闲置']
]

// 简单的分类映射字典
const categoryMap = {
  '数码产品': 1,
  '书籍教材': 2,
  '衣物鞋帽': 3,
  '代步工具': 4,
  '生活日用': 5,
  '其他闲置': 6
}

// 图片上传后触发
const afterRead = (event) => {
  // 当设置 mutiple 为 true 时, file 为数组格式，否则为对象格式
  let lists = [].concat(event.file)
  let fileListLen = fileList.value.length
  lists.map((item) => {
    fileList.value.push({
      ...item,
      status: 'uploading',
      message: '上传中'
    })
  })
  // 模拟上传成功
  setTimeout(() => {
    for (let i = 0; i < lists.length; i++) {
      fileList.value[fileListLen].status = 'success'
      fileList.value[fileListLen].message = ''
      fileListLen++
    }
  }, 1000)
}

// 删除图片
const deletePic = (event) => {
  fileList.value.splice(event.index, 1)
}

// 确认选择分类
const confirmCategory = (e) => {
  form.value.categoryName = e.value[0]
  form.value.categoryId = categoryMap[e.value[0]] // 记录对应的分类ID
  showCategory.value = false
}

// 提交发布
const submitPublish = async () => {
  if (!form.value.title) {
    if (uToast.value) uToast.value.show({ type: 'error', message: '请输入商品标题' })
    else uni.showToast({ title: '请输入商品标题', icon: 'none' })
    return
  }
  if (!form.value.categoryId) {
    if (uToast.value) uToast.value.show({ type: 'error', message: '请选择商品分类' })
    else uni.showToast({ title: '请选择商品分类', icon: 'none' })
    return
  }
  if (!form.value.price) {
    if (uToast.value) uToast.value.show({ type: 'error', message: '请输入价格' })
    else uni.showToast({ title: '请输入价格', icon: 'none' })
    return
  }

  uni.showLoading({ title: '发布中...' })

  try {
    const res = await proxy.$request({
      url: '/product/publish',
      method: 'POST',
      data: {
        title: form.value.title,
        description: form.value.description,
        price: Number(form.value.price),
        categoryId: form.value.categoryId,
        stock: 1 // 闲置物品默认库存为1
      }
    })

    uni.hideLoading()
    // 不再依赖 uToast 组件，直接使用原生的 uni.showToast 以避免 Cannot read property 'show' of null 错误
    uni.showToast({
      title: typeof res === 'string' ? res : '提交成功，等待审核',
      icon: 'none',
      duration: 2000
    })
    
    // 延迟跳转
    setTimeout(() => {
      // 这里的清空表单可以选择性执行
      form.value = { title: '', description: '', price: '', categoryName: '', categoryId: '' }
      fileList.value = []
      
      uni.switchTab({
        url: '/pages/index/index'
      })
    }, 1500)

  } catch (error) {
    uni.hideLoading()
    console.error('发布错误:', error)
    uni.showToast({
      title: '发布失败，请重试',
      icon: 'none'
    })
  }
}
</script>

<style lang="scss">
page {
  background-color: #F3F4F6;
}

.publish-container {
  padding-bottom: 80px;
}

.upload-section {
  background-color: #fff;
  padding: 15px;
  margin-bottom: 10px;
  
  .upload-tip {
    display: block;
    font-size: 12px;
    color: #999;
    margin-top: 10px;
  }
}

.form-section {
  background-color: #fff;
  padding: 0 15px;
  
  /* 深度覆盖 u-textarea 默认高度 */
  :deep(.u-textarea) {
    padding: 10px 0;
    min-height: 100px;
  }
}

.bottom-btn {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 10px 20px 25px;
  background-color: #fff;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
  z-index: 99;
}
</style>
