<template>
  <nav class="top-nav">
    <div class="nav-inner">
      <a class="logo" @click="goHome">
        <span class="mark">⚡</span>TechMall
      </a>

      <div class="nav-search">
        <input
          v-model="keyword"
          type="text"
          placeholder="搜索商品、品牌…"
          @keyup.enter="search"
        />
        <span class="search-icon" @click="search">🔍</span>
      </div>

      <div class="nav-actions">
        <template v-if="userStore.isLoggedIn">
          <!-- 用户/商家：角色标签 + 昵称 + 余额 -->
          <span v-if="userStore.role !== 'ADMIN'" class="nav-user-info">
            <span class="nav-user-top">
              <span class="nav-role" :class="roleClass">{{ roleLabel }}</span>
              <span class="nav-nickname">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
            </span>
            <span class="nav-balance">¥{{ Number(userStore.userInfo?.balance || 0).toLocaleString() }}</span>
          </span>
          <span v-if="userStore.role === 'ADMIN'" class="nav-role role-admin">管理员</span>
          <el-dropdown>
            <span class="user-avatar">👤</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-if="userStore.role === 'USER'" @click="$router.push('/orders')">📋 我的订单</el-dropdown-item>
                <el-dropdown-item v-if="userStore.role === 'MERCHANT'" @click="$router.push('/merchant/products')">📦 商品管理</el-dropdown-item>
                <el-dropdown-item v-if="userStore.role === 'MERCHANT'" @click="$router.push('/merchant/orders')">📊 订单管理</el-dropdown-item>
                <el-dropdown-item v-if="userStore.role === 'ADMIN'" @click="$router.push('/admin/users')">⚙️ 管理后台</el-dropdown-item>
                <el-dropdown-item v-if="userStore.role !== 'ADMIN'" @click="$router.push('/account')">👤 账户设置</el-dropdown-item>
                <el-dropdown-item divided @click="userStore.logout()">🚪 退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <router-link to="/login" class="login-btn">登录</router-link>
        </template>

        <button v-if="userStore.role === 'USER' || !userStore.isLoggedIn" class="cart-btn" @click="$emit('toggleCart')">
          🛒<span v-if="cartStore.totalCount" class="cart-badge">{{ cartStore.totalCount }}</span>
        </button>
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'

defineEmits(['toggleCart'])

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const keyword = ref('')

const roleLabel = computed(() => {
  const map: Record<string, string> = { USER: '用户', MERCHANT: '商家', ADMIN: '管理员' }
  return map[userStore.role] || ''
})

const roleClass = computed(() => {
  const map: Record<string, string> = { ADMIN: 'role-admin', MERCHANT: 'role-merchant', USER: 'role-user' }
  return map[userStore.role] || ''
})

function goHome() {
  sessionStorage.removeItem('homeScrollY')
  router.push('/home')
}

function search() {
  const term = keyword.value.trim()
  if (!term) return
  router.push({ path: '/products', query: { keyword: term } })
}
</script>

<style scoped>
.top-nav {
  position: sticky; top: 0; z-index: 100;
  background: rgba(11, 17, 30, 0.82);
  backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid var(--border-subtle);
}
.nav-inner {
  max-width: 1320px; margin: 0 auto; padding: 0 var(--space-lg);
  height: 60px; display: flex; align-items: center; gap: var(--space-lg);
}
.logo {
  font-family: var(--font-display); font-weight: 900; font-size: 1.3rem;
  color: var(--text-primary); display: flex; align-items: center; gap: 8px;
  flex-shrink: 0; text-decoration: none; cursor: pointer;
}
.logo .mark {
  width: 32px; height: 32px;
  background: linear-gradient(135deg, var(--accent-cyan), #0088cc);
  border-radius: var(--radius-sm);
  display: flex; align-items: center; justify-content: center;
  font-size: 0.9rem;
}
.nav-search { flex: 1; max-width: 420px; position: relative; }
.nav-search input {
  width: 100%; background: var(--bg-surface);
  border: 1px solid var(--border-subtle); border-radius: 24px;
  padding: 8px 44px 8px 16px; font-size: 0.85rem;
  color: var(--text-primary); outline: none;
  transition: all var(--duration-fast);
}
.nav-search input:focus {
  border-color: var(--border-active);
  box-shadow: var(--shadow-glow); background: var(--bg-elevated);
}
.nav-search input::placeholder { color: var(--text-muted); }
.search-icon { position: absolute; right: 14px; top: 50%; transform: translateY(-50%); cursor: pointer; }
.nav-actions { display: flex; align-items: center; gap: var(--space-md); flex-shrink: 0; }
.nav-role {
  font-family: var(--font-display); font-size: 0.7rem; font-weight: 700;
  padding: 2px 8px; border-radius: 10px; letter-spacing: 0.05em;
}
.role-admin { color: var(--accent-rose); background: rgba(244,63,94,0.1); }
.role-merchant { color: var(--accent-amber); background: rgba(245,158,11,0.1); }
.role-user { color: var(--accent-cyan); background: rgba(0,198,242,0.1); }
.nav-user-info {
  display: flex; flex-direction: column; align-items: flex-end; line-height: 1.3;
}
.nav-user-top { display: flex; align-items: center; gap: 6px; }
.nav-nickname { font-size: 0.8rem; font-weight: 600; color: var(--text-primary); }
.nav-balance {
  font-size: 0.72rem; color: var(--accent-amber);
  font-family: var(--font-display); font-weight: 600;
}
.user-avatar { cursor: pointer; font-size: 1.2rem; padding: 4px; }
.login-btn { font-size: 0.85rem; color: var(--accent-cyan); font-weight: 500; text-decoration: none; }
.cart-btn {
  background: none; border: none; font-size: 1.2rem; cursor: pointer;
  color: var(--text-secondary); padding: 4px; position: relative;
  transition: color var(--duration-fast);
}
.cart-btn:hover { color: var(--accent-cyan); }
.cart-badge {
  position: absolute; top: 0; right: 0;
  width: 18px; height: 18px; background: var(--accent-rose);
  border-radius: 50%; font-size: 0.65rem; font-weight: 700;
  color: #fff; display: flex; align-items: center; justify-content: center;
}
</style>
