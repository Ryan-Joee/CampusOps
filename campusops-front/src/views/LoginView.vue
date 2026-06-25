<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { getCaptcha } from '../api/auth'
import { Refresh } from '@element-plus/icons-vue'
import logoImg from '../assets/logo.png'
import bgImg from '../assets/bgImage.png'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const captchaLoading = ref(false)
const captchaData = ref(null)

const formRef = ref(null)

const form = reactive({
  account: '',
  password: '',
  captchaCode: '',
})

const rules = {
  account: [{ required: true, message: '请输入用户名 / 手机号 / 邮箱', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
}

const bgStyle = { backgroundImage: `url(${bgImg})` }

async function refreshCaptcha() {
  captchaLoading.value = true
  try {
    const res = await getCaptcha()
    if (res.code === 'SUCCESS') {
      captchaData.value = res.data
    }
  } catch {
    captchaData.value = null
  } finally {
    captchaLoading.value = false
  }
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  errorMessage.value = ''

  try {
    await userStore.loginAction({
      account: form.account,
      password: form.password,
      captchaId: captchaData.value?.captchaId,
      captchaCode: form.captchaCode,
    })
    const redirect = route.query.redirect || '/dashboard'
    router.push(redirect)
  } catch (err) {
    errorMessage.value = err.message || '登录失败，请重试'
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (userStore.token && userStore.userInfo) {
    router.replace('/dashboard')
    return
  }
  refreshCaptcha()

  if (route.query.registered) {
    successMessage.value = '注册成功，请登录'
  }
})
</script>

<template>
  <div class="login-page" :style="bgStyle">
    <div class="login-container">
      <div class="login-brand">
        <img :src="logoImg" alt="CampusOps" class="login-logo" />
        <div class="login-brand-text">
          <h1>CampusOps</h1>
          <p class="login-tagline">校园 IT 服务工单平台</p>
        </div>
      </div>

      <el-card class="login-card standalone" shadow="never">
        <el-form
          ref="formRef"
          label-position="top"
          :model="form"
          :rules="rules"
          @keyup.enter="handleLogin"
        >
          <el-form-item label="账号" prop="account">
            <el-input
              v-model="form.account"
              placeholder="用户名 / 手机号 / 邮箱"
              autocomplete="username"
            />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              show-password
              placeholder="请输入密码"
              autocomplete="current-password"
            />
          </el-form-item>
          <el-form-item label="验证码" prop="captchaCode">
            <div class="captcha-row">
              <el-input
                v-model="form.captchaCode"
                placeholder="请输入验证码"
                maxlength="4"
              />
              <div class="captcha-img-wrap" :class="{ loading: captchaLoading, error: !captchaData && !captchaLoading }" @click="refreshCaptcha" title="换一张">
                <img
                  v-if="captchaData?.imageBase64"
                  :src="captchaData.imageBase64"
                  alt="验证码"
                  class="captcha-img"
                />
                <div v-else-if="captchaLoading" class="captcha-overlay">
                  <span class="captcha-placeholder">加载中...</span>
                </div>
                <div v-else class="captcha-overlay captcha-error-overlay">
                  <el-icon class="captcha-error-icon"><Refresh /></el-icon>
                  <span class="captcha-placeholder">点击重新获取</span>
                </div>
              </div>
            </div>
          </el-form-item>

          <div v-if="errorMessage" class="login-error">{{ errorMessage }}</div>
          <div v-if="successMessage" class="login-success">{{ successMessage }}</div>

          <el-button
            type="primary"
            class="full-button"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>

          <p class="login-switch">
            还没有账号？<router-link to="/register">创建账号</router-link>
          </p>
        </el-form>
      </el-card>

      <p class="login-footer">© 2026 CampusOps</p>
    </div>
  </div>
</template>
