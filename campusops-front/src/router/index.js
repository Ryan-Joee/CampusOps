import { createRouter, createWebHistory } from 'vue-router'
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
    meta: { title: '登录' },
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

router.beforeEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} - CampusOps` : 'CampusOps'
})

export default router
