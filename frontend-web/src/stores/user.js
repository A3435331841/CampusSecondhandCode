import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('user_token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('user_info') || 'null'))
  const isGuest = ref(!token.value)

  const isLoggedIn = computed(() => !!token.value)

  function setUser(data) {
    token.value = data.token
    userInfo.value = {
      userId: data.userId,
      nickname: data.nickname,
      avatar: data.avatar,
      role: data.role,
      verifyStatus: data.verifyStatus
    }
    isGuest.value = false
    localStorage.setItem('user_token', data.token)
    localStorage.setItem('user_info', JSON.stringify(userInfo.value))
  }

  function setGuest() {
    token.value = ''
    userInfo.value = null
    isGuest.value = true
    localStorage.removeItem('user_token')
    localStorage.removeItem('user_info')
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    isGuest.value = true
    localStorage.removeItem('user_token')
    localStorage.removeItem('user_info')
  }

  return { token, userInfo, isGuest, isLoggedIn, setUser, setGuest, logout }
})
