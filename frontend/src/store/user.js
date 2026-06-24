import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(null)

  const isLoggedIn = computed(() => !!localStorage.getItem('token'))
  const userId = computed(() => localStorage.getItem('userId'))
  const token = computed(() => localStorage.getItem('token'))

  function setLogin(data) {
    localStorage.setItem('token', data.token)
    localStorage.setItem('userId', data.user.id)
    userInfo.value = data.user
  }

  function logout() {
    localStorage.clear()
    userInfo.value = null
    window.location.href = '/login'
  }

  async function fetchUserInfo(id) {
    const res = await getUserInfo(id || userId.value)
    userInfo.value = res.data
    return res.data
  }

  return { userInfo, isLoggedIn, userId, token, setLogin, logout, fetchUserInfo }
})
