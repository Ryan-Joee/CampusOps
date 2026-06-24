import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import BasicLayout from '../layouts/BasicLayout.vue'
import LoginView from '../views/LoginView.vue'
import DashboardView from '../views/DashboardView.vue'
import TicketListView from '../views/ticket/TicketListView.vue'
import KnowledgeListView from '../views/knowledge/KnowledgeListView.vue'
import AiAnalysisView from '../views/ai/AiAnalysisView.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: { title: '登录', public: true },
  },
  {
    path: '/',
    component: BasicLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: DashboardView,
        meta: { title: '工作台' },
      },
      {
        path: 'tickets',
        name: 'TicketList',
        component: TicketListView,
        meta: { title: '工单管理' },
      },
      {
        path: 'knowledge',
        name: 'KnowledgeList',
        component: KnowledgeListView,
        meta: { title: '知识库' },
      },
      {
        path: 'ai-analysis',
        name: 'AiAnalysis',
        component: AiAnalysisView,
        meta: { title: 'AI 分析' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to) => {
  document.title = to.meta.title ? `${to.meta.title} - CampusOps` : 'CampusOps'

  if (to.meta.public) {
    return true
  }

  const token = localStorage.getItem('campusops_token')
  if (!token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  const userStore = useUserStore()
  if (!userStore.userInfo) {
    try {
      await userStore.fetchCurrentUser()
    } catch {
      userStore.clearSession()
      return { path: '/login', query: { redirect: to.fullPath } }
    }
  }

  return true
})

export default router
