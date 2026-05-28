export const API_BASE = 'http://localhost:8080/api'
export const API_ORIGIN = 'http://localhost:8080'

export const FALLBACK_IMG = 'https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?auto=format&fit=crop&w=400&q=80'

export const normalizeImage = (url) => {
  if (!url) return FALLBACK_IMG
  if (/^https?:\/\//.test(url)) return url
  return `${API_ORIGIN}${url.startsWith('/') ? url : '/' + url}`
}
