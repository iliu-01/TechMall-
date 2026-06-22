<template>
  <div class="page-root">
    <AppHeader />
    <div class="page-container" style="margin-top: var(--space-xl); max-width: 640px">
      <h2 class="section-title">📦 添加商品</h2>
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" placeholder="商品详细介绍" /></el-form-item>
        <el-form-item label="规格标签"><el-input v-model="form.tags" placeholder="逗号分隔，如: 骁龙 8 Gen5,200MP,120W 快充" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId">
            <el-option v-for="c in cats" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="submit" :loading="submitting" style="border-radius:20px">发布商品</el-button></el-form-item>
      </el-form>
    </div>
  </div>
  <AppFooter />
</template>

<script setup lang="ts">
import AppFooter from '@/components/AppFooter.vue'
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import AppHeader from '@/components/AppHeader.vue'


const router = useRouter()

const submitting = ref(false)
const cats = ref<any[]>([])

const form = reactive({ name: '', description: '', tags: '', price: 0, stock: 0, categoryId: null })

async function submit() {
  if (!form.name.trim()) { ElMessage.warning('请输入商品名称'); return }
  if (!form.categoryId) { ElMessage.warning('请选择分类'); return }
  submitting.value = true
  try {
    await request.post('/product', { ...form })
    ElMessage.success('发布成功')
    router.push('/merchant/products')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '发布失败')
  } finally { submitting.value = false }
}

onMounted(async () => {
  const res: any = await request.get('/category/list')
  cats.value = res.data || []
})
</script>
