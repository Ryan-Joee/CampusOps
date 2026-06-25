import request from './request'

export function getTicketPage(params) {
  return request.get('/tickets', { params })
}

export function getTicketDetail(id) {
  return request.get(`/tickets/${id}`)
}

export function createTicket(data) {
  return request.post('/tickets', data)
}

export function getTicketCategories() {
  return request.get('/ticket-categories')
}
