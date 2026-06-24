<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <h1 class="login-logo">CloudBlog</h1>
        <p class="login-desc">简洁 · 清爽 · 个人博客</p>
      </div>

      <div class="login-tabs">
        <span :class="{ active: isLogin }" @click="isLogin = true">登录</span>
        <span :class="{ active: !isLogin }" @click="isLogin = false">注册</span>
      </div>

      <el-form v-if="isLogin" :model="form" @submit.prevent="handleLogin" class="login-form">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" size="large" clearable />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password />
        </el-form-item>
        <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleLogin">
          登 录
        </el-button>
      </el-form>

      <el-form v-else :model="regForm" @submit.prevent="handleRegister" class="login-form">
        <el-form-item>
          <el-input v-model="regForm.username" placeholder="用户名" size="large" clearable />
        </el-form-item>
        <el-form-item>
          <el-input v-model="regForm.password" type="password" placeholder="密码" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-input v-model="regForm.nickname" placeholder="昵称" size="large" clearable />
        </el-form-item>
        <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleRegister">
          注 册
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { login, register } from '@/api/user'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const isLogin = ref(true)
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const regForm = reactive({ username: '', password: '', nickname: '' })

async function handleLogin() {
  if (!form.username || !form.password) return
  loading.value = true
  try {
    const res = await login(form)
    userStore.setLogin(res.data)
    router.push('/public')
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  if (!regForm.username || !regForm.password || !regForm.nickname) return
  loading.value = true
  try {
    await register(regForm)
    isLogin.value = true
    form.username = regForm.username
    form.password = regForm.password
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px;
}

.login-card {
  background: #fff;
  border-radius: 16px;
  padding: 40px;
  width: 400px;
  max-width: 100%;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-logo {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.login-desc {
  font-size: 14px;
  color: #8c8c8c;
}

.login-tabs {
  display: flex;
  justify-content: center;
  gap: 32px;
  margin-bottom: 28px;
  font-size: 16px;
}

.login-tabs span {
  color: #8c8c8c;
  cursor: pointer;
  padding-bottom: 8px;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}

.login-tabs span.active {
  color: #1a1a1a;
  font-weight: 600;
  border-bottom-color: #4a90d9;
}

.login-form .el-form-item {
  margin-bottom: 18px;
}

.submit-btn {
  width: 100%;
  margin-top: 8px;
}
</style>
