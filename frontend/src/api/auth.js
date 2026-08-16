import http from './http'

export function login(userId, password) {
  return http.post('/auth/login', { userId, password })
}

export function fetchMe() {
  return http.get('/auth/me')
}
