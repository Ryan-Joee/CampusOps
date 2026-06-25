<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { getCaptcha } from '../api/auth'
import { Refresh } from '@element-plus/icons-vue'
import logoImg from '../assets/logo.png'
import bgImg from '../assets/bgImage.png'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const errorMessage = ref('')
const captchaLoading = ref(false)
const captchaData = ref(null)

const formRef = ref(null)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  email: '',
  phone: '',
  department: '',
  captchaCode: '',
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]{4,64}$/, message: '4-64 位字母、数字或下划线', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度 6-32 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== form.password) {
          callback(new Error('两次密码输入不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' },
  ],
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

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  errorMessage.value = ''

  try {
    await userStore.registerAction({
      username: form.username,
      password: form.password,
      confirmPassword: form.confirmPassword,
      realName: form.realName,
      email: form.email,
      phone: form.phone,
      department: form.department || undefined,
      captchaId: captchaData.value?.captchaId,
      captchaCode: form.captchaCode,
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (err) {
    errorMessage.value = err.message || '注册失败，请重试'
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
        <h2 class="register-title">创建账号</h2>

        <el-form
          ref="formRef"
          label-position="top"
          :model="form"
          :rules="rules"
        >
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="用户名" prop="username">
                <el-input v-model="form.username" placeholder="4-64 位字母、数字或下划线" autocomplete="off" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="姓名" prop="realName">
                <el-input v-model="form.realName" placeholder="请输入真实姓名" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="密码" prop="password">
                <el-input
                  v-model="form.password"
                  type="password"
                  show-password
                  placeholder="6-32 位密码"
                  autocomplete="new-password"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input
                  v-model="form.confirmPassword"
                  type="password"
                  show-password
                  placeholder="再次输入密码"
                  autocomplete="new-password"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="form.email" placeholder="请输入邮箱" autocomplete="email" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="form.phone" placeholder="请输入手机号" autocomplete="tel" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="院系/部门" prop="department">
            <el-input v-model="form.department" placeholder="选填，如：计算机学院" />
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

          <el-button
            type="primary"
            class="full-button"
            :loading="loading"
            @click="handleSubmit"
          >
            注册
          </el-button>

          <p class="login-switch">
            已有账号？<router-link to="/login">去登录</router-link>
          </p>
        </el-form>
      </el-card>

      <p class="login-footer">© 2026 CampusOps</p>
    </div>
  </div>
</template>

<style scoped>
.register-title {
  margin: 0 0 20px;
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  text-align: center;
}
</style>
