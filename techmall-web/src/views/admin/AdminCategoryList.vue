<template>
  <div>
    <AppHeader />
    <div class="page-container" style="margin-top: var(--space-xl)">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:var(--space-lg)">
        <h2 class="section-title" style="margin-bottom:0">📂 分类管理</h2>
      </div>
        <div class="table-wrap">
        <el-table :data="cats" v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="parentId" label="父级ID" width="80" />
        <el-table-column prop="sort" label="排序" width="80" />
      </el-table>
      </div>
      <el-card style="margin-top:var(--space-lg);background:var(--bg-surface);border-color:var(--border-subtle)">
        <el-form :model="form" inline>
          <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
          <el-form-item><el-button type="primary" @click="add" :loading="adding" style="border-radius:20px">添加分类</el-button></el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
  <AppFooter />
</template>

<script setup lang="ts">
import AppFooter from '@/components/AppFooter.vue'
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import AppHeader from '@/components/AppHeader.vue'



const cats = ref<any[]>([])
const loading = ref(false)
const adding = ref(false)
const form = reactive({ name: '' })

async function fetch() {
  loading.value = true
  const res: any = await request.get('/category/list')
  cats.value = res.data || []
  loading.value = false
}

async function add() {
  if (!form.name) return
  adding.value = true
  try {
    await request.post('/category', { name: form.name })
    ElMessage.success('添加成功')
    form.name = ''
    fetch()
  } catch (e: any) { ElMessage.error(e?.response?.data?.message || '添加失败') } finally { adding.value = false }
}

onMounted(fetch)
</script>
