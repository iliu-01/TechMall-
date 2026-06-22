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

      <div class="nav-links">
        <router-link to="/home" class="nav-link">首页</router-link>
        <router-link v-if="userStore.role === 'USER'" to="/orders" class="nav-link">我的订单</router-link>
        <router-link v-if="userStore.role === 'USER'" to="/account" class="nav-link">充值</router-link>
        <router-link v-if="userStore.role === 'MERCHANT'" to="/merchant/products" class="nav-link">商品管理</router-link>
        <router-link v-if="userStore.role === 'MERCHANT'" to="/merchant/orders" class="nav-link">订单管理</router-link>
        <router-link v-if="userStore.role === 'ADMIN'" to="/admin/users" class="nav-link">用户管理</router-link>
        <router-link v-if="userStore.role === 'ADMIN'" to="/admin/orders" class="nav-link">订单管理</router-link>
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
          <el-popover v-if="userStore.role !== 'ADMIN'" placement="bottom" :width="280" trigger="click">
            <template #reference>
              <span class="bell-btn">
                🔔<span v-if="notifyCount" class="bell-badge">{{ notifyCount }}</span>
              </span>
            </template>
            <div v-if="notifications.length" class="notify-list">
              <div v-for="n in notifications" :key="n.id" class="notify-item" @click="dismissNotify(n)">
                <span class="notify-icon">{{ n.icon }}</span>
                <div class="notify-text">
                  <div>{{ n.text }}</div>
                  <div class="notify-time">{{ n.time }}</div>
                </div>
              </div>
            </div>
            <div v-else style="text-align:center;color:var(--text-muted);padding:12px">暂无新消息</div>
          </el-popover>
          <el-dropdown>
            <span class="user-avatar-wrap">
              <span class="user-avatar">👤</span>
            </span>
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
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

// 消息通知
const notifications = ref<any[]>(JSON.parse(sessionStorage.getItem('notify') || '[]'))
const notifyCount = computed(() => notifications.value.length)

async function loadNotifications() {
  if (userStore.role === 'ADMIN' || !userStore.isLoggedIn) return
  try {
    const url = userStore.role === 'MERCHANT' ? '/order/merchant' : '/order/my'
    const res: any = await request.get(url, { params: { page: 1, size: 20 } })
    const orders = res.data?.records || []
    const msgs: any[] = []
    for (const o of orders) {
      if (o.status === 'PAID' && userStore.role === 'MERCHANT') {
        msgs.push({ id: `s${o.id}`, orderId: o.id, icon: '📦', text: `新订单 ${o.orderNo} 待发货`, time: o.createdAt?.slice(0,10) || '' })
      }
      if (o.status === 'SHIPPED' && userStore.role === 'USER') {
        msgs.push({ id: `r${o.id}`, orderId: o.id, icon: '🚚', text: `订单 ${o.orderNo} 已发货`, time: o.createdAt?.slice(0,10) || '' })
      }
    }
    notifications.value = msgs
    sessionStorage.setItem('notify', JSON.stringify(msgs))
  } catch { /* 忽略 */ }
}

function dismissNotify(n: any) {
  notifications.value = notifications.value.filter(x => x.id !== n.id)
  sessionStorage.setItem('notify', JSON.stringify(notifications.value))
  router.push(`/orders/${n.orderId}`)
}

onMounted(() => { loadNotifications() })

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
.nav-links { display: flex; gap: 2px; flex-shrink: 0; }
.nav-link {
  padding: 6px 12px; border-radius: 6px; font-size: 0.78rem; font-weight: 500;
  color: var(--text-secondary); text-decoration: none;
  transition: all var(--duration-fast); white-space: nowrap;
}
.nav-link:hover { color: var(--accent-cyan); background: rgba(0,198,242,0.06); }
.nav-link.router-link-active { color: var(--accent-cyan); }

.nav-search { flex: 1; max-width: 380px; position: relative; }
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
.user-avatar-wrap {
  width: 32px; height: 32px; border-radius: 50%;
  border: 2px solid var(--border-subtle);
  display: flex; align-items: center; justify-content: center;
  transition: border-color var(--duration-fast);
}
.user-avatar-wrap:hover { border-color: var(--accent-cyan); }
.user-avatar { cursor: pointer; font-size: 1rem; }
.login-btn { font-size: 0.85rem; color: var(--accent-cyan); font-weight: 500; text-decoration: none; }

.bell-btn {
  position: relative; cursor: pointer; font-size: 1.1rem; color: var(--text-secondary);
  padding: 4px; transition: color var(--duration-fast);
}
.bell-btn:hover { color: var(--accent-cyan); }
.bell-badge {
  position: absolute; top: -2px; right: -4px;
  width: 16px; height: 16px; background: var(--accent-rose);
  border-radius: 50%; font-size: 0.6rem; font-weight: 700;
  color: #fff; display: flex; align-items: center; justify-content: center;
}
.notify-list { max-height: 240px; overflow-y: auto; }
.notify-item {
  display: flex; gap: 8px; padding: 8px; cursor: pointer;
  border-radius: 6px; transition: background var(--duration-fast);
}
.notify-item:hover { background: rgba(0,198,242,0.05); }
.notify-icon { font-size: 1.1rem; flex-shrink: 0; }
.notify-text { font-size: 0.8rem; line-height: 1.4; }
.notify-time { font-size: 0.7rem; color: var(--text-muted); margin-top: 2px; }
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
