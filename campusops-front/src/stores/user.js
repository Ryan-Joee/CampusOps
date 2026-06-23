import { defineStore } from 'pinia'

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
  },
})
