<template>
  <div class="cart-overlay" :class="{ open: visible }" @click="$emit('close')"></div>
  <div class="cart-drawer" :class="{ open: visible }">
    <div class="cart-header">
      <h3>🛒 购物车 ({{ cartStore.totalCount }})</h3>
      <button class="cart-close" @click="$emit('close')">✕</button>
    </div>
    <div class="cart-body">
      <div v-if="cartStore.items.length === 0" class="cart-empty">购物车是空的</div>
      <div v-for="item in cartStore.items" :key="item.productId" class="cart-item">
        <div class="cart-item-img">{{ itemIcon(item.name) }}</div>
        <div class="cart-item-info">
          <div class="ci-name">{{ item.name }}</div>
          <div class="ci-qty">
            <button @click="cartStore.updateQuantity(item.productId, item.quantity - 1)">−</button>
            <span>{{ item.quantity }}</span>
            <button @click="cartStore.updateQuantity(item.productId, item.quantity + 1)">+</button>
            <span class="ci-price">¥{{ (item.price * item.quantity).toLocaleString() }}</span>
          </div>
        </div>
        <button class="ci-del" @click="cartStore.removeItem(item.productId)">🗑</button>
      </div>
    </div>
    <div class="cart-footer" v-if="cartStore.totalCount > 0">
      <div class="cart-total">
        <span class="label">合计</span>
        <span class="amount">¥{{ cartStore.totalPrice.toLocaleString() }}</span>
      </div>
      <button class="btn-primary" @click="$router.push('/cart'); $emit('close')">去结算 →</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useCartStore } from '@/stores/cart'

defineProps<{ visible: boolean }>()
defineEmits(['close'])

const cartStore = useCartStore()

function itemIcon(name: string) {
  if (name.includes('手机')) return '📱'
  if (name.includes('笔记本') || name.includes('ROG')) return '💻'
  if (name.includes('耳机')) return '🎧'
  if (name.includes('手表')) return '⌚'
  if (name.includes('键盘')) return '⌨️'
  return '📦'
}
</script>

<style scoped>
.cart-overlay {
  position: fixed; inset: 0; z-index: 200;
  background: rgba(0,0,0,0.55); backdrop-filter: blur(4px);
  opacity: 0; pointer-events: none;
  transition: opacity var(--duration-normal);
}
.cart-overlay.open { opacity: 1; pointer-events: auto; }
.cart-drawer {
  position: fixed; top: 0; right: 0; z-index: 201;
  width: 400px; max-width: 100vw; height: 100vh;
  background: var(--bg-primary);
  border-left: 1px solid var(--border-subtle);
  display: flex; flex-direction: column;
  transform: translateX(100%);
  transition: transform var(--duration-slow) var(--ease-smooth);
}
.cart-drawer.open { transform: translateX(0); }
.cart-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: var(--space-lg); border-bottom: 1px solid var(--border-subtle);
}
.cart-header h3 { font-family: var(--font-display); font-weight: 700; font-size: 1.1rem; }
.cart-close { background: none; border: none; color: var(--text-secondary); font-size: 1.2rem; cursor: pointer; }
.cart-close:hover { color: var(--text-primary); }
.cart-body { flex: 1; overflow-y: auto; padding: var(--space-md); }
.cart-empty { text-align: center; color: var(--text-muted); padding: var(--space-3xl); }
.cart-item {
  display: flex; gap: var(--space-md); padding: var(--space-md);
  background: var(--bg-surface); border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md); margin-bottom: var(--space-sm);
}
.cart-item-img { width: 56px; height: 56px; background: var(--bg-elevated); border-radius: var(--radius-sm); display: flex; align-items: center; justify-content: center; font-size: 1.5rem; flex-shrink: 0; }
.cart-item-info { flex: 1; min-width: 0; }
.ci-name { font-weight: 500; font-size: 0.85rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-bottom: 8px; }
.ci-qty { display: flex; align-items: center; gap: 8px; }
.ci-qty button {
  width: 24px; height: 24px; border-radius: 50%;
  border: 1px solid var(--border-subtle); background: var(--bg-elevated);
  color: var(--text-primary); cursor: pointer; font-size: 0.8rem;
  display: flex; align-items: center; justify-content: center;
}
.ci-qty button:hover { border-color: var(--accent-cyan); }
.ci-price { font-family: var(--font-display); font-weight: 700; color: var(--accent-amber); margin-left: auto; font-size: 0.9rem; }
.ci-del { background: none; border: none; cursor: pointer; font-size: 1rem; color: var(--text-muted); padding: 0 4px; }
.ci-del:hover { color: var(--accent-rose); }
.cart-footer { padding: var(--space-lg); border-top: 1px solid var(--border-subtle); }
.cart-total { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: var(--space-md); }
.cart-total .label { font-size: 0.9rem; color: var(--text-secondary); }
.cart-total .amount { font-family: var(--font-display); font-weight: 700; font-size: 1.25rem; color: var(--accent-amber); }
.btn-primary {
  width: 100%; justify-content: center; padding: 12px;
  background: var(--accent-cyan); color: #060b14; border: none;
  border-radius: 24px; font-family: var(--font-display);
  font-weight: 600; font-size: 0.9rem; cursor: pointer;
  display: flex; align-items: center; gap: 8px;
}
.btn-primary:hover { box-shadow: 0 0 24px rgba(0,198,242,0.35); }
</style>
