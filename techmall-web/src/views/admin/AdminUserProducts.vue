<template>
  <div>
    <AppHeader />
    <div class="page-container" style="margin-top: var(--space-xl); min-height: calc(100vh - 320px)">
      <router-link to="/admin/users" class="back-link">← 返回用户管理</router-link>
      <h2 class="section-title">📦 {{ targetName }} 的商品</h2>
      <div class="table-wrap">
        <el-table :data="products" v-loading="loading">
          <el-table-column prop="id" label="ID" width="70" align="center" />
          <el-table-column prop="name" label="商品名称" min-width="240" show-overflow-tooltip />
          <el-table-column label="价格" width="110" align="right"><template #default="{row}"><span class="price-cell">¥{{ Number(row.price).toLocaleString() }}</span></template></el-table-column>
          <el-table-column prop="stock" label="库存" width="70" align="center" />
          <el-table-column label="状态" width="90" align="center">
            <template #default="{row}">
              <span class="status-dot" :class="row.status === 1 ? 'on' : 'off'" />{{ row.status === 1 ? '上架' : '下架' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center">
            <template #default="{row}">
              <el-button size="small" text :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
                {{ row.status === 1 ? '下架' : '上架' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <el-empty v-if="!loading && products.length === 0" description="暂无商品" />
    </div>
  </div>
  <AppFooter />
</template>

<script setup lang="ts">
import AppFooter from '@/components/AppFooter.vue'
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import AppHeader from '@/components/AppHeader.vue'

const route = useRoute()
const merchantId = Number(route.query.merchantId)
const targetName = ref(route.query.name as string || '')

const products = ref<any[]>([])
const loading = ref(false)

async function fetch() {
  loading.value = true
  const res: any = await request.get('/product/list', { params: { merchantId, includeOffShelf: true, page: 1, size: 100 } })
  products.value = res.data?.records || []
  loading.value = false
}

async function toggleStatus(row: any) {
  await request.put(`/product/${row.id}/status`, { status: row.status === 1 ? 0 : 1 })
  ElMessage.success('状态已更新')
  fetch()
}

onMounted(fetch)
</script>
