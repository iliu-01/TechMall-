<template>
  <div>
    <AppHeader @toggleCart="showCart = true" />
    <div class="page-container" style="margin-top: var(--space-xl); max-width: 560px">
      <router-link to="/home" class="back-link">← 返回首页</router-link>
      <h2 class="section-title">👤 账户设置</h2>

      <!-- 账户信息卡片 -->
      <div class="info-card">
        <div class="info-row"><span class="info-label">用户名</span><span>{{ userStore.userInfo?.username }}</span></div>
        <div class="info-row"><span class="info-label">角色</span><span>{{ roleLabel }}</span></div>
        <div class="info-row"><span class="info-label">余额</span><span class="balance-val">¥{{ Number(userStore.userInfo?.balance || 0).toLocaleString() }}</span></div>
        <div class="info-row"><span class="info-label">注册时间</span><span>{{ userStore.userInfo?.createdAt || '—' }}</span></div>
      </div>

      <!-- 修改信息 -->
      <h3 style="margin-top:var(--space-xl);margin-bottom:var(--space-md);font-family:var(--font-display)">✏️ 修改信息</h3>
      <el-card style="background:var(--bg-surface);border-color:var(--border-subtle)">
        <el-form :model="form" label-width="80px">
          <el-form-item label="昵称"><el-input v-model="form.nickname" placeholder="你的昵称" /></el-form-item>
          <el-form-item label="手机号"><el-input v-model="form.phone" placeholder="手机号" /></el-form-item>
          <el-form-item label="邮箱"><el-input v-model="form.email" placeholder="邮箱地址" /></el-form-item>
          <el-form-item label="新密码"><el-input v-model="form.password" type="password" show-password placeholder="留空则不修改" /></el-form-item>
          <el-form-item>
            <el-button type="primary" @click="save" :loading="saving" style="border-radius:20px">保存修改</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
    <CartDrawer :visible="showCart" @close="showCart = false" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import AppHeader from '@/components/AppHeader.vue'
import CartDrawer from '@/components/CartDrawer.vue'

const userStore = useUserStore()
const showCart = ref(false)
const saving = ref(false)

const form = reactive({ nickname: '', phone: '', email: '', password: '' })

const roleLabel = computed(() => {
  const map: Record<string, string> = { USER: '普通用户', MERCHANT: '商家', ADMIN: '管理员' }
  return map[userStore.role] || ''
})

onMounted(async () => {
  // 从服务端拉取最新用户信息
  try {
    const res: any = await request.get('/user/me')
    if (res.data) {
      userStore.userInfo = { ...userStore.userInfo, ...res.data }
      form.nickname = res.data.nickname || ''
      form.phone = res.data.phone || ''
      form.email = res.data.email || ''
    }
  } catch { /* 忽略 */ }
})

async function save() {
  saving.value = true
  try {
    const body: any = {}
    if (form.nickname) body.nickname = form.nickname
    if (form.phone) body.phone = form.phone
    if (form.email) body.email = form.email
    if (form.password) body.password = form.password
    await request.put('/user/me', body)
    // 刷新本地缓存
    if (userStore.userInfo) {
      userStore.userInfo.nickname = form.nickname || userStore.userInfo.nickname
      userStore.userInfo.phone = form.phone || userStore.userInfo.phone
      userStore.userInfo.email = form.email || userStore.userInfo.email
      localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))
    }
    ElMessage.success('信息已更新')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '保存失败')
  } finally { saving.value = false }
}
</script>

<style scoped>
.info-card {
  background: var(--bg-surface); border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md); padding: var(--space-lg);
}
.info-row {
  display: flex; justify-content: space-between; align-items: center;
  padding: 10px 0; border-bottom: 1px solid var(--border-subtle);
}
.info-row:last-child { border-bottom: none; }
.info-label { color: var(--text-muted); font-size: 0.85rem; }
.balance-val { font-family: var(--font-display); font-weight: 700; color: var(--accent-amber); font-size: 1.1rem; }
</style>
