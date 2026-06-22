<template>
  <div>
    <AppHeader />
    <div class="page-container" style="margin-top: var(--space-xl); max-width: 640px">
      <h2 class="section-title">✏️ 编辑商品</h2>
      <el-form :model="form" label-width="80px" v-if="loaded">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" placeholder="商品详细介绍" /></el-form-item>
        <el-form-item label="规格标签"><el-input v-model="form.tags" placeholder="逗号分隔，如: 骁龙 8 Gen5,200MP,120W 快充" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" /></el-form-item>
        <el-form-item><el-button type="primary" @click="submit" :loading="submitting" style="border-radius:20px">保存修改</el-button></el-form-item>
      </el-form>
    </div>
  </div>
  <AppFooter />
</template>

<script setup lang="ts">
import AppFooter from '@/components/AppFooter.vue'
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import AppHeader from '@/components/AppHeader.vue'


const route = useRoute()
const router = useRouter()

const submitting = ref(false)
const loaded = ref(false)
const form = reactive({ name: '', description: '', tags: '', price: 0, stock: 0 })

onMounted(async () => {
  const res: any = await request.get(`/product/${route.params.id}`)
  Object.assign(form, res.data)
  loaded.value = true
})

async function submit() {
  submitting.value = true
  try {
    await request.put(`/product/${route.params.id}`, { ...form })
    ElMessage.success('已保存')
    router.push('/merchant/products')
  } catch (e: any) { ElMessage.error(e?.response?.data?.message || '保存失败') } finally { submitting.value = false }
}
</script>
