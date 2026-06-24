<template>
  <div class="navbar">
    <div class="navbar-left">
      <span class="navbar-logo">CloudBlog</span>
      <router-link to="/public" class="navbar-link" active-class="active">公共博客</router-link>
      <router-link to="/my" class="navbar-link" active-class="active">我的博客</router-link>
    </div>
    <div class="navbar-right">
      <span style="font-size:13px;color:#8c8c8c;">{{ userStore.userInfo?.nickname || '' }}</span>
      <el-button text size="small" @click="handleLogout">退出</el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

async function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>
