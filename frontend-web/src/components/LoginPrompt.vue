<template>
  <el-dialog v-model="visible" title="需要登录" width="360px" align-center>
    <div class="prompt-body">
      <el-icon class="prompt-icon"><Lock /></el-icon>
      <p class="prompt-text">{{ message }}</p>
    </div>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="goLogin">去登录</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  message: { type: String, default: '该功能需要登录后使用' }
})
const emit = defineEmits(['update:modelValue'])
const router = useRouter()

const visible = computed({
  get: () => props.modelValue,
  set: val => emit('update:modelValue', val)
})

const goLogin = () => {
  visible.value = false
  router.push('/login')
}
</script>

<style scoped>
.prompt-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 0;
  gap: 12px;
}

.prompt-icon {
  font-size: 40px;
  color: var(--primary);
}

.prompt-text {
  font-size: 15px;
  color: #555;
  text-align: center;
}
</style>
