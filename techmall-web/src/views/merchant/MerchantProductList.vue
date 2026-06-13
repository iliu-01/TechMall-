<template>
  <div>
    <AppHeader />
    <div class="page-container" style="margin-top: var(--space-xl)">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:var(--space-lg)">
        <h2 class="section-title" style="margin-bottom:0">🏪 商品管理</h2>
        <el-button type="primary" @click="$router.push('/merchant/product/add')" style="border-radius:20px">+ 添加商品</el-button>
      </div>
      <el-table :data="products" v-loading="loading" stripe style="width:100%;background:var(--bg-surface)">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="名称" min-width="200" />
        <el-table-column prop="price" label="价格" width="120"><template #default="{row}">¥{{ row.price }}</template></el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="status" label="状态" width="80"><template #default="{row}"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{row}">
            <el-button size="small" @click="$router.push(`/merchant/product/${row.id}`)">编辑</el-button>
            <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">{{ row.status === 1 ? '下架' : '上架' }}</el-button>
            <el-popconfirm title="确定删除？" @confirm="del(row.id)"><template #reference><el-button size="small" type="danger">删除</el-button></template></el-popconfirm>
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

async function del(id: number) {
  await request.delete(`/product/${id}`)
  ElMessage.success('已删除')
  fetch()
}

onMounted(fetch)
</script>
