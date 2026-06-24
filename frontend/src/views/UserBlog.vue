<template>
  <div>
    <NavBar />
    <div v-if="loading" style="text-align:center;padding:60px 0;color:#8c8c8c;">加载中...</div>
    <template v-else>
      <div class="blog-card profile-section">
        <img :src="userInfo.avatarUrl || defaultAvatar" class="profile-avatar" />
        <div class="profile-name">{{ userInfo.nickname || '未设置昵称' }}</div>
        <div class="profile-bio">{{ userInfo.signature || '这个人很懒，什么都没写...' }}</div>
      </div>

      <div v-for="blog in blogs" :key="blog.id" class="blog-card">
        <div class="blog-title" @click="goDetail(blog.id)">{{ blog.title }}</div>
        <div class="blog-summary">{{ blog.summary || blog.content?.slice(0, 120) }}</div>
        <div class="blog-meta">
          <span class="meta-item" @click="goDetail(blog.id)">
            <ChatLineSquare /> {{ blog.commentCount || 0 }} 评论
          </span>
          <span class="meta-item" @click="toggleLike(blog)">
            <template v-if="blog.liked"><ThumbsUpFilled style="color:#4a90d9;" /></template>
            <template v-else><ThumbsUp /></template>
            {{ blog.likeCount || 0 }} 点赞
          </span>
          <span style="margin-left:auto;color:#bfbfbf;">{{ formatTime(blog.createdAt) }}</span>
        </div>
      </div>
      <div v-if="blogs.length === 0" class="blog-card" style="text-align:center;padding:40px;color:#8c8c8c;">
        该用户暂无博客
      </div>

      <div class="pagination-wrap">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="pageSize"
          v-model:current-page="currentPage"
          @current-change="loadBlogs"
        />
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUserBlogs, likeBlog } from '@/api/blog'
import { getUserInfo } from '@/api/user'
import { useUserStore } from '@/store/user'
import NavBar from '@/components/NavBar.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const defaultAvatar = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><circle cx="50" cy="50" r="50" fill="%23e8e8e8"/><text x="50" y="55" text-anchor="middle" fill="%23bfbfbf" font-size="40">?</text></svg>'

const userInfo = ref({})
const blogs = ref([])
const loading = ref(true)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

async function loadUser() {
  const res = await getUserInfo(route.params.id)
  userInfo.value = res.data
}

async function loadBlogs() {
  loading.value = true
  try {
    const res = await getUserBlogs(route.params.id, { page: currentPage.value, size: pageSize.value })
    blogs.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

async function toggleLike(blog) {
  if (!userStore.isLoggedIn) return
  try {
    await likeBlog(blog.id)
    blog.liked = !blog.liked
    blog.likeCount += blog.liked ? 1 : -1
  } catch (_) {}
}

function goDetail(id) {
  router.push(`/blog/${id}`)
}

function formatTime(t) {
  if (!t) return ''
  return t.slice(0, 16).replace('T', ' ')
}

onMounted(async () => {
  await loadUser()
  await loadBlogs()
})
</script>
