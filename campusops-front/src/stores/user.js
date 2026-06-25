import { defineStore } from 'pinia'
import { login, register, getCurrentUser } from '../api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('campusops_token') || '',
    userInfo: null,
  }),
  actions: {
    setToken(token) {
      this.token = token
      localStorage.setItem('campusops_token', token)
    },
    setUserInfo(userInfo) {
      this.userInfo = userInfo
    },
    clearSession() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('campusops_token')
    },

    async loginAction(credentials) {
      const res = await login(credentials)
      if (res.code !== 'SUCCESS') {
        throw new Error(res.message || '登录失败')
      }
      this.setToken(res.data.accessToken)
      await this.fetchCurrentUser()
    },

    async registerAction(payload) {
      const res = await register(payload)
      if (res.code !== 'SUCCESS') {
        throw new Error(res.message || '注册失败')
      }
      return res.data
    },

    async fetchCurrentUser() {
      try {
        const res = await getCurrentUser()
        if (res.code !== 'SUCCESS') {
          throw new Error(res.message || '获取用户信息失败')
        }
        this.setUserInfo(res.data)
      } catch {
        this.clearSession()
        throw new Error('获取用户信息失败')
      }
    },

    logoutAction() {
      this.clearSession()
    },
  },
})
