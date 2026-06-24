<template>
  <div>
    <NavBar />
    <div v-if="loading" style="text-align:center;padding:60px 0;color:#8c8c8c;">加载中...</div>
    <template v-else>
      <div class="blog-card">
        <div class="author-info" @click="goUser(blog.userId)">
          <img :src="blog.authorAvatar || defaultAvatar" class="author-avatar" />
          <span class="author-name">{{ blog.authorNickname || '匿名' }}</span>
        </div>
        <h1 style="font-size:22px;font-weight:700;color:#1a1a1a;margin-bottom:12px;">{{ blog.title }}</h1>
        <div style="font-size:15px;color:#333;line-height:1.8;white-space:pre-wrap;margin-bottom:16px;">{{ blog.content }}</div>
        <div class="blog-meta" style="padding-top:12px;border-top:1px solid #f0f0f0;">
          <span class="meta-item" @click="toggleLike">
            <template v-if="blog.liked"><ThumbsUpFilled style="color:#4a90d9;" /></template>
            <template v-else><ThumbsUp /></template>
            {{ blog.likeCount || 0 }}
          </span>
          <span class="meta-item"><ChatLineSquare /> {{ comments.length }}</span>
          <span style="margin-left:auto;color:#bfbfbf;">{{ formatTime(blog.createdAt) }}</span>
        </div>
      </div>

      <!-- Comment Form -->
      <div class="blog-card">
        <div style="display:flex;gap:12px;">
          <el-input
            v-model="commentContent"
            placeholder="写下你的评论..."
            @keyup.enter="submitComment"
          />
          <el-button type="primary" :loading="commenting" @click="submitComment">评论</el-button>
        </div>
      </div>

      <!-- Comments -->
      <div class="blog-card">
        <div style="font-size:15px;font-weight:600;color:#1a1a1a;margin-bottom:8px;">
          评论 ({{ comments.length }})
        </div>
        <div v-if="comments.length === 0" style="text-align:center;padding:20px;color:#bfbfbf;">暂无评论</div>
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <div class="comment-header">
            <img :src="comment.userAvatar || defaultAvatar" class="comment-avatar" />
            <span class="comment-nickname">{{ comment.userNickname || '匿名' }}</span>
            <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
          </div>
          <div class="comment-content">{{ comment.content }}</div>
          <!-- Replies -->
          <div v-if="comment.replies && comment.replies.length > 0">
            <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
              <div class="comment-header">
                <img :src="reply.userAvatar || defaultAvatar" class="comment-avatar" />
                <span class="comment-nickname">{{ reply.userNickname || '匿名' }}</span>
                <span class="comment-time">{{ formatTime(reply.createdAt) }}</span>
              </div>
              <div class="comment-content">{{ reply.content }}</div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getBlogDetail, likeBlog } from '@/api/blog'
import { getComments, createComment } from '@/api/comment'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import NavBar from '@/components/NavBar.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const defaultAvatar = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><circle cx="50" cy="50" r="50" fill="%23e8e8e8"/><text x="50" y="55" text-anchor="middle" fill="%23bfbfbf" font-size="40">?</text></svg>'

const blog = ref({})
const comments = ref([])
const loading = ref(true)
const commentContent = ref('')
const commenting = ref(false)

async function loadDetail() {
  loading.value = true
  try {
    const [blogRes, commentRes] = await Promise.all([
      getBlogDetail(route.params.id),
      getComments(route.params.id)
    ])
    blog.value = blogRes.data
    comments.value = commentRes.data || []
  } finally {
    loading.value = false
  }
}

async function toggleLike() {
  if (!userStore.isLoggedIn) return
  try {
    await likeBlog(blog.value.id)
    blog.value.liked = !blog.value.liked
    blog.value.likeCount += blog.value.liked ? 1 : -1
  } catch (_) {}
}

async function submitComment() {
  if (!commentContent.value.trim()) return
  commenting.value = true
  try {
    await createComment({ blogId: blog.value.id, content: commentContent.value })
    ElMessage.success('评论成功')
    commentContent.value = ''
    await loadDetail()
  } finally {
    commenting.value = false
  }
}

function goUser(id) {
  router.push(`/user/${id}`)
}

function formatTime(t) {
  if (!t) return ''
  return t.slice(0, 16).replace('T', ' ')
}

onMounted(loadDetail)
</script>
