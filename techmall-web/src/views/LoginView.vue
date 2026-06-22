<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-logo">
        <span class="mark">⚡</span>
        <span class="brand">TechMall</span>
      </div>

      <el-tabs v-model="mode" stretch>
        <el-tab-pane label="登录" name="login" />
        <el-tab-pane label="注册" name="register" />
      </el-tabs>

      <template v-if="mode === 'login'">
        <el-input v-model="loginForm.username" placeholder="用户名" size="large" style="margin-bottom:16px" />
        <el-input v-model="loginForm.password" type="password" placeholder="密码" size="large" show-password style="margin-bottom:8px" @keyup.enter="handleLogin" />
        <el-button type="primary" size="large" :loading="loading" @click="handleLogin" style="width:100%;margin-top:8px;border-radius:24px">
          登录
        </el-button>
      </template>

      <template v-else>
        <el-input v-model="regForm.username" placeholder="用户名" size="large" style="margin-bottom:16px" />
        <el-input v-model="regForm.password" type="password" placeholder="密码" size="large" show-password style="margin-bottom:16px" />
        <el-radio-group v-model="regForm.role" style="margin-bottom:16px;width:100%;display:flex">
          <el-radio-button value="USER" style="flex:1">普通用户</el-radio-button>
          <el-radio-button value="MERCHANT" style="flex:1">商家</el-radio-button>
        </el-radio-group>
        <el-button type="primary" size="large" :loading="loading" @click="handleRegister" style="width:100%;border-radius:24px">
          注册
        </el-button>
      </template>

      <div class="error-msg" v-if="error">{{ error }}</div>

      <div class="test-accounts">
        <p style="color:var(--text-muted);font-size:0.75rem;margin-bottom:8px">测试账号（密码: 123456）</p>
        <el-tag v-for="a in testAccounts" :key="a.user" style="margin:2px;cursor:pointer" @click="quickFill(a)">
          {{ a.role }}: {{ a.user }}
        </el-tag>
      </div>
    </div>
  </div>
  <AppFooter />
</template>

<script setup lang="ts">
import AppFooter from '@/components/AppFooter.vue'
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const mode = ref('login')
const loading = ref(false)
const error = ref('')

const loginForm = reactive({ username: '', password: '' })
const regForm = reactive({ username: '', password: '', role: 'USER' })

const testAccounts = [
  { user: 'admin', role: '管理员', roleCode: 'ADMIN' },
  { user: 'merchant1', role: '商家', roleCode: 'MERCHANT' },
  { user: 'user1', role: '用户', roleCode: 'USER' },
]

function quickFill(a: any) {
  loginForm.username = a.user
  loginForm.password = '123456'
  mode.value = 'login'
}

async function handleLogin() {
  if (!loginForm.username || !loginForm.password) {
    error.value = '请输入用户名和密码'
    return
  }
  loading.value = true; error.value = ''
  try {
    await userStore.login(loginForm.username, loginForm.password)
    ElMessage.success('登录成功')
    const redirect = (route.query.redirect as string) || '/home'
    router.push(redirect)
  } catch (e: any) {
    error.value = e?.response?.data?.message || e?.message || '登录失败'
  } finally { loading.value = false }
}

async function handleRegister() {
  if (!regForm.username || !regForm.password) {
    error.value = '请输入用户名和密码'
    return
  }
  loading.value = true; error.value = ''
  try {
    await userStore.register(regForm.username, regForm.password, regForm.role)
    ElMessage.success('注册成功，请登录')
    mode.value = 'login'
    loginForm.username = regForm.username
  } catch (e: any) {
    error.value = e?.response?.data?.message || e?.message || '注册失败'
  } finally { loading.value = false }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  background: var(--bg-deep);
}
.login-card {
  width: 400px; max-width: 90vw;
  background: var(--bg-surface); border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl); padding: var(--space-2xl);
}
.login-logo {
  display: flex; align-items: center; justify-content: center; gap: 10px;
  margin-bottom: var(--space-xl);
}
.login-logo .mark {
  width: 40px; height: 40px;
  background: linear-gradient(135deg, var(--accent-cyan), #0088cc);
  border-radius: var(--radius-sm);
  display: flex; align-items: center; justify-content: center;
  font-size: 1.1rem;
}
.login-logo .brand {
  font-family: var(--font-display); font-weight: 900; font-size: 1.5rem;
}
.error-msg { color: var(--accent-rose); font-size: 0.85rem; margin-top: var(--space-md); text-align: center; }
.test-accounts { margin-top: var(--space-xl); text-align: center; padding-top: var(--space-lg); border-top: 1px solid var(--border-subtle); }
</style>
