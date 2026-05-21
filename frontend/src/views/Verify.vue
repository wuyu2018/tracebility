<template>
  <section class="container" style="padding:24px 0;">
    <div class="page-card">
      <h1 class="title">防伪查询</h1>
      <el-form inline @submit.prevent>
        <el-form-item>
          <el-input v-model="code" placeholder="请输入防伪码" style="width:280px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submit">查询</el-button>
        </el-form-item>
      </el-form>
      <pre v-if="result" class="json">{{ result }}</pre>
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { verifyAntiFakeCodeV2 } from '../services/api'

const code = ref('')
const result = ref('')

async function submit() {
  if (!code.value) return
  try {
    const data = await verifyAntiFakeCodeV2(code.value)
    result.value = JSON.stringify(data, null, 2)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '查询失败')
  }
}
</script>

<style scoped>
.json { white-space: pre-wrap; background:#0f172a; color:#e2e8f0; padding:12px; border-radius:10px; }
</style>
