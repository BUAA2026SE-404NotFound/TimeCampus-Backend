import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'
import Layout from '@/layout/Layout.vue'

export const menuItems = [
  {
    path: '/dashboard',
    title: '运营首页',
    icon: 'DataBoard'
  },
  {
    path: '/pois',
    title: 'POI 管理',
    icon: 'Location'
  },
  {
    path: '/operation-map',
    title: '运营地图',
    icon: 'Guide'
  },
  {
    path: '/content',
    title: '内容管理',
    icon: 'Picture',
    children: [
      { path: '/content/media', title: '官方内容', icon: 'Collection' },
      { path: '/content/ugc', title: 'UGC 审核', icon: 'UploadFilled' },
      { path: '/content/comments', title: '评论审核', icon: 'ChatDotRound' }
    ]
  },
  {
    path: '/map-tools',
    title: '地图工具',
    icon: 'MapLocation'
  },
  {
    path: '/logs',
    title: '审计日志',
    icon: 'Tickets'
  }
]

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue')
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/DashboardView.vue'), meta: { title: '运营首页' } },
      { path: 'pois', name: 'PoiManage', component: () => import('@/views/PoiView.vue'), meta: { title: 'POI 管理' } },
      { path: 'operation-map', name: 'OperationMap', component: () => import('@/views/OperationMapView.vue'), meta: { title: '运营地图' } },
      { path: 'content/media', name: 'MediaManage', component: () => import('@/views/MediaView.vue'), meta: { title: '官方内容' } },
      { path: 'content/ugc', name: 'UgcReview', component: () => import('@/views/UgcView.vue'), meta: { title: 'UGC 审核' } },
      { path: 'content/comments', name: 'CommentReview', component: () => import('@/views/CommentView.vue'), meta: { title: '评论审核' } },
      { path: 'map-tools', name: 'MapTools', component: () => import('@/views/MapToolsView.vue'), meta: { title: '地图工具' } },
      { path: 'logs', name: 'AuditLog', component: () => import('@/views/LogView.vue'), meta: { title: '审计日志' } }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to) => {
  document.title = `${to.meta.title || '管理端'} - 时光航迹`
  if (to.path !== '/login' && !getToken()) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.path === '/login' && getToken()) {
    return { path: '/dashboard' }
  }
  return true
})

export default router
