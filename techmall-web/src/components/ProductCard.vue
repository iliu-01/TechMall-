<template>
  <div class="product-card" @click="$router.push(`/product/${product.id}`)">
    <div class="card-img">
      <span v-if="tag" class="card-tag" :class="`tag-${tag}`">{{ tagText }}</span>
      <span class="p-icon">{{ icon }}</span>
    </div>
    <div class="card-body">
      <div class="card-brand">{{ brand }}</div>
      <div class="card-name">{{ product.name }}</div>
      <div class="card-specs" v-if="specs?.length">
        <span v-for="s in specs" :key="s" class="spec-chip">{{ s }}</span>
      </div>
      <div class="card-footer">
        <span class="card-price"><span class="yen">¥</span>{{ product.price }}</span>
        <span class="card-rating">⭐ 4.8</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  product: any
  tag?: string
}>()

const icon = computed(() => {
  const name = props.product.name || ''
  if (name.includes('手机') || name.includes('Galaxy')) return '📱'
  if (name.includes('笔记本') || name.includes('ROG') || name.includes('ThinkPad')) return '💻'
  if (name.includes('耳机') || name.includes('AirPods') || name.includes('QuietComfort')) return '🎧'
  if (name.includes('手表') || name.includes('Watch')) return '⌚'
  if (name.includes('iPad') || name.includes('平板')) return '📋'
  if (name.includes('显示器')) return '🖥️'
  if (name.includes('键盘') || name.includes('MX')) return '⌨️'
  if (name.includes('充电') || name.includes('Anker')) return '🔌'
  return '📦'
})

const brand = computed(() => {
  const name = props.product.name || ''
  if (name.includes('小米')) return 'Xiaomi'
  if (name.includes('ROG') || name.includes('ASUS')) return 'ASUS'
  if (name.includes('Sony')) return 'Sony'
  if (name.includes('华为') || name.includes('HUAWEI')) return 'Huawei'
  if (name.includes('Apple') || name.includes('iPad') || name.includes('AirPods')) return 'Apple'
  if (name.includes('Samsung') || name.includes('Galaxy')) return 'Samsung'
  if (name.includes('Lenovo') || name.includes('ThinkPad')) return 'Lenovo'
  if (name.includes('Bose')) return 'Bose'
  if (name.includes('Logitech')) return 'Logitech'
  if (name.includes('Anker')) return 'Anker'
  return ''
})

const specs = computed(() => {
  const tags = props.product.tags || ''
  return tags.split(',').map((s: string) => s.trim()).filter(Boolean).slice(0, 3)
})

const tagText = computed(() => {
  if (props.tag === 'new') return 'NEW'
  if (props.tag === 'hot') return 'HOT'
  if (props.tag === 'limited') return '限量'
  return ''
})
</script>

<style scoped>
.product-card {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg); overflow: hidden;
  transition: all var(--duration-normal) var(--ease-smooth);
  cursor: pointer;
}
.product-card:hover {
  transform: translateY(-4px);
  border-color: var(--border-active);
  box-shadow: var(--shadow-glow), var(--shadow-elevated);
}
.card-img {
  aspect-ratio: 1/1;
  background: linear-gradient(145deg, #141e2e, #0f172a);
  display: flex; align-items: center; justify-content: center;
  position: relative;
}
.p-icon { font-size: 3rem; transition: transform var(--duration-normal); }
.product-card:hover .p-icon { transform: scale(1.08); }
.card-tag {
  position: absolute; top: 10px; left: 10px;
  font-family: var(--font-display); font-size: 0.65rem; font-weight: 700;
  padding: 3px 10px; border-radius: 12px; letter-spacing: 0.05em;
}
.tag-new { background: var(--accent-cyan); color: #060b14; }
.tag-hot { background: var(--accent-rose); color: #fff; }
.tag-limited { background: var(--accent-amber); color: #060b14; }
.card-body { padding: var(--space-md); }
.card-brand {
  font-family: var(--font-display); font-size: 0.7rem; font-weight: 600;
  letter-spacing: 0.08em; text-transform: uppercase;
  color: var(--text-muted); margin-bottom: 4px;
}
.card-name {
  font-weight: 500; font-size: 0.85rem; line-height: 1.4;
  margin-bottom: var(--space-sm);
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
  overflow: hidden;
}
.card-specs { display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: var(--space-md); }
.spec-chip {
  font-family: var(--font-display); font-size: 0.68rem; font-weight: 500;
  padding: 2px 8px; background: rgba(0,198,242,0.06);
  border: 1px solid rgba(0,198,242,0.12); border-radius: 10px;
  color: var(--accent-cyan-dim);
}
.card-footer { display: flex; align-items: center; justify-content: space-between; }
.card-price { font-family: var(--font-display); font-weight: 700; font-size: 1.1rem; color: var(--accent-amber); }
.card-price .yen { font-size: 0.75rem; }
.card-rating { font-size: 0.7rem; color: var(--text-muted); }
</style>
