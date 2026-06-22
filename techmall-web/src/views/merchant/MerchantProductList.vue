<template>
  <div>
    <AppHeader />
    <div class="page-container" style="margin-top: var(--space-xl); min-height: calc(100vh - 320px)">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:var(--space-lg)">
        <h2 class="section-title" style="margin-bottom:0">🏪 商品管理</h2>
        <el-button type="primary" @click="$router.push('/merchant/product/add')" style="border-radius:20px">+ 添加商品</el-button>
      </div>

      <div class="table-wrap">
        <el-table
          :data="products"
          v-loading="loading"
          :header-cell-style="headerStyle"
          :cell-style="cellStyle"
          row-class-name="product-row"
        >
          <el-table-column prop="id" label="ID" width="70" align="center" />
          <el-table-column prop="name" label="商品名称" min-width="220" show-overflow-tooltip />
          <el-table-column label="价格" width="110" align="right">
            <template #default="{row}">
              <span class="price-cell">¥{{ Number(row.price).toLocaleString() }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="stock" label="库存" width="70" align="center" />
          <el-table-column label="状态" width="90" align="center">
            <template #default="{row}">
              <span class="status-dot" :class="row.status === 1 ? 'on' : 'off'" />
              {{ row.status === 1 ? '上架' : '下架' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="230" align="center">
            <template #default="{row}">
              <el-button size="small" text @click="$router.push(`/merchant/product/${row.id}`)">编辑</el-button>
              <el-button size="small" text :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
                {{ row.status === 1 ? '下架' : '上架' }}
              </el-button>
              <el-popconfirm title="确定删除？" @confirm="del(row.id)">
                <template #reference>
                  <el-button size="small" text type="danger">删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
  <AppFooter />
</template>

<script setup lang="ts">
import AppFooter from '@/components/AppFooter.vue'
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import AppHeader from '@/components/AppHeader.vue'

const userStore = useUserStore()

const products = ref<any[]>([])
const loading = ref(false)

const headerStyle = {
  background: 'var(--bg-elevated)',
  color: 'var(--text-secondary)',
  fontSize: '0.8rem',
  fontWeight: '600',
  letterSpacing: '0.04em',
  borderBottom: '1px solid var(--border-subtle)',
}

const cellStyle = {
  background: 'transparent',
  borderBottom: '1px solid var(--border-subtle)',
  padding: '12px 0',
}

async function fetch() {
  loading.value = true
  const res: any = await request.get('/product/list', { params: { page: 1, size: 100, merchantId: userStore.userInfo.userId, includeOffShelf: true } })
  products.value = res.data?.records || []
  loading.value = false
}

async function toggleStatus(row: any) {
  await request.put(`/product/${row.id}/status`, { status: row.status === 1 ? 0 : 1 })
  ElMessage.success('状态已更新')
  fetch()
}

async function del(id: number) {
  await request.delete(`/product/${id}`)
  ElMessage.success('已删除')
  fetch()
}

onMounted(fetch)
</script>

<style scoped>
.table-wrap {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.table-wrap :deep(.el-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: transparent;
  --el-table-row-hover-bg-color: rgba(0, 198, 242, 0.04);
  --el-table-border-color: var(--border-subtle);
  --el-table-text-color: var(--text-primary);
}

.table-wrap :deep(.el-table__header th) {
  border-bottom: 1px solid rgba(0, 198, 242, 0.15);
}

.table-wrap :deep(.product-row:hover td) {
  background: rgba(0, 198, 242, 0.04) !important;
}

.table-wrap :deep(.el-loading-mask) {
  background: rgba(11, 17, 30, 0.7);
}

.price-cell {
  font-family: var(--font-display);
  font-weight: 600;
  color: var(--accent-amber);
}

.status-dot {
  display: inline-block; width: 6px; height: 6px; border-radius: 50%;
  margin-right: 6px; vertical-align: middle; margin-top: -1px;
}
.status-dot.on  { background: #22c55e; box-shadow: 0 0 6px rgba(34, 197, 94, 0.4); }
.status-dot.off { background: var(--text-muted); }
</style>
