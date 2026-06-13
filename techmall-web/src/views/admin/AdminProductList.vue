<template>
  <div>
    <AppHeader />
    <div class="page-container" style="margin-top: var(--space-xl)">
      <h2 class="section-title">📦 全站商品管理</h2>
      <el-table :data="products" v-loading="loading" stripe style="width:100%;background:var(--bg-surface)">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="名称" min-width="200" />
        <el-table-column prop="price" label="价格" width="100"><template #default="{row}">¥{{ row.price }}</template></el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="merchantId" label="商家ID" width="80" />
        <el-table-column prop="status" label="状态" width="80"><template #default="{row}"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{row}">
            <el-button size="small" @click="toggleStatus(row)">{{ row.status === 1 ? '下架' : '上架' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import AppHeader from '@/components/AppHeader.vue'



const products = ref<any[]>([])
const loading = ref(false)

async function fetch() {
  loading.value = true
  const res: any = await request.get('/product/list', { params: { page: 1, size: 100 } })
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
