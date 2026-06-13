<template>
  <div>
    <AppHeader />
    <div class="page-container" style="margin-top: var(--space-xl)">
      <h2 class="section-title">👥 用户管理</h2>
        <div class="table-wrap">
        <el-table :data="users" v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="role" label="角色" width="100"><template #default="{row}"><el-tag>{{ row.role }}</el-tag></template></el-table-column>
        <el-table-column prop="status" label="状态" width="80"><template #default="{row}"><el-tag :type="row.status ? 'success' : 'danger'">{{ row.status ? '正常' : '禁用' }}</el-tag></template></el-table-column>
        <el-table-column label="操作">
          <template #default="{row}">
            <el-button v-if="row.role !== 'ADMIN'" :type="row.status ? 'warning' : 'success'" size="small" @click="toggle(row)">{{ row.status ? '禁用' : '启用' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import AppHeader from '@/components/AppHeader.vue'



const users = ref<any[]>([])
const loading = ref(false)

async function fetch() {
  loading.value = true
  const res: any = await request.get('/user/list', { params: { page: 1, size: 100 } })
  users.value = res.data?.records || []
  loading.value = false
}

async function toggle(row: any) {
  await request.put(`/user/${row.id}/status`, { status: row.status ? 0 : 1 })
  ElMessage.success('状态已更新')
  fetch()
}

onMounted(fetch)
</script>
