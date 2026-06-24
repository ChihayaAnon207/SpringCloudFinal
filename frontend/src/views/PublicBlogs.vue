<template>
  <div>
    <NavBar />
    <div v-if="loading" style="text-align:center;padding:60px 0;color:#8c8c8c;">加载中...</div>
    <div v-else>
      <div v-for="blog in blogs" :key="blog.id" class="blog-card">
        <div class="author-info" @click="goUser(blog.userId)">
          <img :src="blog.authorAvatar || defaultAvatar" class="author-avatar" />
          <span class="author-name">{{ blog.authorNickname || '匿名' }}</span>
        </div>
        <div class="blog-title" @click="goDetail(blog.id)">{{ blog.title }}</div>
        <div class="blog-summary">{{ blog.summary || blog.content?.slice(0, 120) }}</div>
        <div class="blog-meta">
          <span class="meta-item" @click="goDetail(blog.id)">
            <ChatLineSquare /> {{ blog.commentCount || 0 }}
          </span>
          <span class="meta-item" @click="toggleLike(blog)">
            <template v-if="blog.liked"><ThumbsUpFilled style="color:#4a90d9;" /></template>
            <template v-else><ThumbsUp /></template>
            {{ blog.likeCount || 0 }}
          </span>
          <span style="margin-left:auto;color:#bfbfbf;">{{ formatTime(blog.createdAt) }}</span>
        </div>
      </div>
      <div v-if="blogs.length === 0" style="text-align:center;padding:60px 0;color:#8c8c8c;">
        暂无博客
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
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getBlogPage, likeBlog } from '@/api/blog'
import { useUserStore } from '@/store/user'
import NavBar from '@/components/NavBar.vue'

const router = useRouter()
const userStore = useUserStore()

const blogs = ref([])
const loading = ref(true)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const defaultAvatar = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><circle cx="50" cy="50" r="50" fill="%23e8e8e8"/><text x="50" y="55" text-anchor="middle" fill="%23bfbfbf" font-size="40">?</text></svg>'

async function loadBlogs() {
  loading.value = true
  try {
    const res = await getBlogPage({ page: currentPage.value, size: pageSize.value })
    blogs.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function goDetail(id) {
  router.push(`/blog/${id}`)
}

function goUser(id) {
  router.push(`/user/${id}`)
}

async function toggleLike(blog) {
  if (!userStore.isLoggedIn) return
  try {
    await likeBlog(blog.id)
    blog.liked = !blog.liked
    blog.likeCount += blog.liked ? 1 : -1
  } catch (_) {}
}

function formatTime(t) {
  if (!t) return ''
  return t.slice(0, 16).replace('T', ' ')
}

onMounted(loadBlogs)
</script>
