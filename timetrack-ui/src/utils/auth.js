import Cookies from 'js-cookie'

const TOKEN_KEY = 'TimeCampus-Admin-Token'
const ADMIN_KEY = 'timecampus_admin'

export function getToken() {
  return Cookies.get(TOKEN_KEY)
}

export function setToken(token) {
  return Cookies.set(TOKEN_KEY, token)
}

export function removeToken() {
  return Cookies.remove(TOKEN_KEY)
}

export function setAdmin(admin) {
  localStorage.setItem(ADMIN_KEY, JSON.stringify(admin || {}))
}

export function getAdmin() {
  const raw = localStorage.getItem(ADMIN_KEY)
  if (!raw) return {}
  try {
    return JSON.parse(raw)
  } catch (error) {
    return {}
  }
}

export function clearAdmin() {
  localStorage.removeItem(ADMIN_KEY)
}
