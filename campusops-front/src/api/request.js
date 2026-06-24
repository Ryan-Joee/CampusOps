import axios from 'axios'

const service = axios.create({
  baseURL: '/campusops',
  timeout: 10000,
})

service.interceptors.request.use((config) => {
  const token = localStorage.getItem('campusops_token')

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})

service.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const message = error.response?.data?.message || error.message || '请求失败'
    const status = error.response?.status

    // 401 means token expired or invalid — clear local state and redirect to login
    if (status === 401) {
      localStorage.removeItem('campusops_token')
      // Avoid redirect loop when the failing request is the login page itself
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }

    return Promise.reject(new Error(message))
  },
)

export default service
