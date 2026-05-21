<template>
  <section class="container" style="padding:24px 0;">
    <div class="page-card" style="max-width:520px;margin:0 auto;">
      <h1 class="title">后台登录</h1>
      <p class="subtitle">登录成功后进入录入后台，按后端权限控制菜单可见性。</p>

      <el-form :model="form" label-position="top" @submit.prevent="doLogin">
        <el-form-item label="账号">
          <el-input v-model.trim="form.username" placeholder="请输入账号" @keyup.enter="doLogin" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" @keyup.enter="doLogin" />
        </el-form-item>
        <el-form-item label="验证码">
          <el-input v-model.trim="form.captcha" placeholder="请输入验证码" @keyup.enter="doLogin" />
        </el-form-item>
        <p class="subtitle" style="margin: 0 0 12px; font-size: 13px;">
          对接后端流程：先调用 <code>POST /api/captcha</code>，再调用 <code>POST /api/login</code>。
        </p>
        <el-button type="primary" :loading="loading" @click="doLogin" style="width:100%;">登录</el-button>
      </el-form>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminLogin, storeCaptcha } from '../services/api'
import { setToken, setUsername, setRole, setAgentType, getDefaultHomeByRole } from '../utils/auth'

const router = useRouter()
const route = useRoute()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  captcha: '',
})

async function doLogin() {
  if (!form.username || !form.password || !form.captcha) {
    ElMessage.warning('请填写完整登录信息')
    return
  }

  loading.value = true
  try {
    await storeCaptcha(form.username, form.captcha)
    const data = await adminLogin(form.username, form.password, form.captcha)
    setToken(data.token, data.tokenType)
    setUsername(data.username)
    setRole(data.role)
    setAgentType(data.agentType || '')
    localStorage.setItem('expiresIn', String(data.expiresIn || ''))
    localStorage.setItem('loginAt', String(Date.now()))
    ElMessage.success('登录成功')
    router.push(route.query.redirect || getDefaultHomeByRole())
  } catch (error) {
    ElMessage.error(error?.response?.data || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>
