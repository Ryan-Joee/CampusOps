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
  (error) => Promise.reject(error)
)

export default service
