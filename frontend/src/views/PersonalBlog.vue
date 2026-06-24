<template>
  <div>
    <NavBar />
    <div class="blog-card profile-section">
      <img :src="userInfo.avatarUrl || defaultAvatar" class="profile-avatar" />
      <div class="profile-name">{{ userInfo.nickname || '未设置昵称' }}</div>
      <div class="profile-bio">{{ userInfo.signature || '这个人很懒，什么都没写...' }}</div>
      <el-button type="primary" plain size="small" @click="showEditProfile = true">编辑资料</el-button>
    </div>

    <!-- Publish Blog -->
    <div class="blog-card" style="text-align:center;padding:16px;">
      <el-button type="primary" :icon="Edit" size="large" @click="showPublish = !showPublish" round>
        {{ showPublish ? '收起' : '发布博客' }}
      </el-button>
    </div>

    <div v-if="showPublish" class="publish-form">
      <el-input v-model="publishForm.title" placeholder="博客标题" size="large" style="margin-bottom:16px;" />
      <el-input
        v-model="publishForm.content"
        type="textarea"
        :rows="8"
        placeholder="写点什么吧..."
        style="margin-bottom:16px;"
      />
      <div style="display:flex;justify-content:flex-end;gap:12px;">
        <el-button @click="showPublish = false">取消</el-button>
        <el-button type="primary" :loading="publishing" @click="handlePublish">提交</el-button>
      </div>
    </div>

    <!-- My Blogs -->
    <div v-if="blogs.length === 0" class="blog-card" style="text-align:center;padding:40px;color:#8c8c8c;">
      还没有博客，点击上方按钮发布第一篇吧
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
        <span style="margin-left:auto;display:flex;gap:8px;">
          <el-button text size="small" type="primary" @click="editBlog(blog)">编辑</el-button>
          <el-popconfirm title="确定删除这篇博客？" @confirm="handleDelete(blog.id)">
            <template #reference>
              <el-button text size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
          <span style="color:#bfbfbf;font-size:12px;line-height:28px;">{{ formatTime(blog.createdAt) }}</span>
        </span>
      </div>
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

    <!-- Edit Profile Dialog -->
    <el-dialog v-model="showEditProfile" title="编辑资料" width="450px">
      <el-form :model="profileForm" label-position="top">
        <el-form-item label="头像">
          <div style="display:flex;align-items:center;gap:16px;">
            <img :src="profileForm.avatarUrl || defaultAvatar" style="width:60px;height:60px;border-radius:50%;object-fit:cover;" />
            <div>
              <el-upload
                :show-file-list="false"
                :http-request="handleAvatarUpload"
                accept="image/*"
              >
                <el-button type="primary" plain size="small">选择图片上传</el-button>
              </el-upload>
              <div style="font-size:12px;color:#8c8c8c;margin-top:4px;">支持 JPG/PNG，建议 200x200</div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="profileForm.nickname" />
        </el-form-item>
        <el-form-item label="个人简介">
          <el-input v-model="profileForm.signature" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditProfile = false">取消</el-button>
        <el-button type="primary" :loading="savingProfile" @click="handleSaveProfile">保存</el-button>
      </template>
    </el-dialog>

    <!-- Edit Blog Dialog -->
    <el-dialog v-model="showEditBlog" title="编辑博客" width="600px">
      <el-input v-model="editForm.title" placeholder="标题" size="large" style="margin-bottom:16px;" />
      <el-input v-model="editForm.content" type="textarea" :rows="6" placeholder="内容" />
      <template #footer>
        <el-button @click="showEditBlog = false">取消</el-button>
        <el-button type="primary" :loading="savingBlog" @click="handleSaveBlog">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Edit } from '@element-plus/icons-vue'
import { getUserBlogs, createBlog, updateBlog, deleteBlog, likeBlog } from '@/api/blog'
import { uploadFile } from '@/api/file'
import { updateProfile } from '@/api/user'
import { useUserStore } from '@/store/user'
import NavBar from '@/components/NavBar.vue'

const router = useRouter()
const userStore = useUserStore()

const defaultAvatar = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><circle cx="50" cy="50" r="50" fill="%23e8e8e8"/><text x="50" y="55" text-anchor="middle" fill="%23bfbfbf" font-size="40">?</text></svg>'

const userInfo = ref({})
const blogs = ref([])
const loading = ref(true)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const showPublish = ref(false)
const publishing = ref(false)
const publishForm = reactive({ title: '', content: '' })

const showEditProfile = ref(false)
const savingProfile = ref(false)
const profileForm = reactive({ nickname: '', signature: '', avatarUrl: '' })

const showEditBlog = ref(false)
const savingBlog = ref(false)
const editForm = reactive({ id: null, title: '', content: '' })

async function loadBlogs() {
  const res = await getUserBlogs(userStore.userId, { page: currentPage.value, size: pageSize.value })
  blogs.value = res.data.records || []
  total.value = res.data.total || 0
}

async function loadProfile() {
  userInfo.value = await userStore.fetchUserInfo()
  profileForm.nickname = userInfo.value.nickname || ''
  profileForm.signature = userInfo.value.signature || ''
  profileForm.avatarUrl = userInfo.value.avatarUrl || ''
}

async function handlePublish() {
  if (!publishForm.title.trim() || !publishForm.content.trim()) {
    ElMessage.warning('标题和内容不能为空')
    return
  }
  publishing.value = true
  try {
    await createBlog({ title: publishForm.title, content: publishForm.content })
    ElMessage.success('发布成功')
    publishForm.title = ''
    publishForm.content = ''
    showPublish.value = false
    currentPage.value = 1
    await loadBlogs()
  } finally {
    publishing.value = false
  }
}

async function handleAvatarUpload(uploadOptions) {
  const formData = new FormData()
  formData.append('file', uploadOptions.file)
  try {
    const res = await uploadFile(formData)
    profileForm.avatarUrl = res.data.url
  } catch (_) {
    ElMessage.error('头像上传失败')
  }
}

async function handleSaveProfile() {
  savingProfile.value = true
  try {
    await updateProfile(profileForm)
    ElMessage.success('保存成功')
    showEditProfile.value = false
    await loadProfile()
  } finally {
    savingProfile.value = false
  }
}

function editBlog(blog) {
  editForm.id = blog.id
  editForm.title = blog.title
  editForm.content = blog.content
  showEditBlog.value = true
}

async function handleSaveBlog() {
  savingBlog.value = true
  try {
    await updateBlog(editForm.id, { title: editForm.title, content: editForm.content })
    ElMessage.success('更新成功')
    showEditBlog.value = false
    await loadBlogs()
  } finally {
    savingBlog.value = false
  }
}

async function handleDelete(id) {
  await deleteBlog(id)
  ElMessage.success('已删除')
  await loadBlogs()
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
  await loadProfile()
  await loadBlogs()
})
</script>
