<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { DataAnalysis, Document, House, Tickets } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const displayName = computed(() => {
  return userStore.userInfo?.realName || userStore.userInfo?.username || '未登录'
})

const displayInfo = computed(() => {
  const parts = []
  if (userStore.userInfo?.department) parts.push(userStore.userInfo.department)
  if (userStore.userInfo?.roles?.length) {
    parts.push(userStore.userInfo.roles.join('、'))
  }
  return parts.join(' · ')
})

function handleCommand(command) {
  if (command === 'logout') {
    userStore.logoutAction()
    router.push('/login')
  }
}
</script>

<template>
  <el-container class="app-shell">
    <el-aside class="app-sidebar" width="232px">
      <div class="brand">
        <div class="brand-mark">C</div>
        <div>
          <div class="brand-name">CampusOps</div>
          <div class="brand-desc">校园 IT 服务平台</div>
        </div>
      </div>

      <el-menu :default-active="activeMenu" router class="side-menu">
        <el-menu-item index="/dashboard">
          <el-icon><House /></el-icon>
          <span>工作台</span>
        </el-menu-item>
        <el-menu-item index="/tickets">
          <el-icon><Tickets /></el-icon>
          <span>工单管理</span>
        </el-menu-item>
        <el-menu-item index="/knowledge">
          <el-icon><Document /></el-icon>
          <span>知识库</span>
        </el-menu-item>
        <el-menu-item index="/ai-analysis">
          <el-icon><DataAnalysis /></el-icon>
          <span>AI 分析</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="app-header">
        <div>
          <div class="page-title">{{ route.meta.title || 'CampusOps' }}</div>
          <div v-if="displayInfo" class="page-subtitle">{{ displayInfo }}</div>
        </div>
        <el-dropdown trigger="click" @command="handleCommand">
          <el-button>{{ displayName }}</el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <el-main class="app-main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>
