<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const errorMessage = ref('')

const loginForm = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  errorMessage.value = ''

  try {
    await userStore.loginAction({
      username: loginForm.username,
      password: loginForm.password,
    })
    const redirect = route.query.redirect || '/dashboard'
    router.push(redirect)
  } catch (err) {
    errorMessage.value = err.message || '登录失败，请重试'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (userStore.token && userStore.userInfo) {
    router.replace('/dashboard')
  }
})
</script>

<template>
  <div class="login-page">
    <div class="login-panel">
      <div class="login-intro">
        <div class="brand-mark large">C</div>
        <h1>CampusOps</h1>
        <p>校园 IT 服务工单平台</p>
      </div>

      <el-card class="login-card" shadow="hover">
        <template #header>
          <span>账号登录</span>
        </template>

        <el-form
          ref="formRef"
          label-position="top"
          :model="loginForm"
          :rules="rules"
          @keyup.enter="handleLogin"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              autocomplete="username"
            />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              show-password
              placeholder="请输入密码"
              autocomplete="current-password"
            />
          </el-form-item>

          <div v-if="errorMessage" class="login-error">{{ errorMessage }}</div>

          <el-button
            type="primary"
            class="full-button"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form>

        <div class="login-hint">
          <p class="login-hint-title">测试账号</p>
          <p class="login-hint-item">student001 / 123456</p>
          <p class="login-hint-item">admin / admin123</p>
          <p class="login-hint-item">service_admin / admin123</p>
        </div>
      </el-card>
    </div>
  </div>
</template>
