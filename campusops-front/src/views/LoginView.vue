<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const loginForm = reactive({
  username: 'admin',
  password: '123456',
})

function handleLogin() {
  router.push('/dashboard')
}
</script>

<template>
  <div class="login-page">
    <section class="login-panel">
      <div class="login-intro">
        <div class="brand-mark large">C</div>
        <h1>CampusOps</h1>
        <p>面向校园 IT 服务的 AI 增强型工单管理平台</p>
      </div>

      <el-card class="login-card" shadow="never">
        <template #header>
          <span>账号登录</span>
        </template>
        <el-form label-position="top" :model="loginForm">
          <el-form-item label="用户名">
            <el-input v-model="loginForm.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="loginForm.password" type="password" show-password placeholder="请输入密码" />
          </el-form-item>
          <el-button type="primary" class="full-button" @click="handleLogin">登录</el-button>
        </el-form>
      </el-card>
    </section>
  </div>
</template>
