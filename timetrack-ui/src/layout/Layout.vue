<template>
  <el-container class="layout">
    <el-aside class="sidebar" width="232px">
      <div class="brand" @click="router.push('/dashboard')">
        <span class="brand-mark">TC</span>
        <div>
          <strong>时光航迹</strong>
          <small>TimeCampus Admin</small>
        </div>
      </div>
      <el-menu :default-active="activePath" router class="menu">
        <template v-for="item in menuItems" :key="item.path">
          <el-sub-menu v-if="item.children" :index="item.path">
            <template #title>
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.title }}</span>
            </template>
            <el-menu-item v-for="child in item.children" :key="child.path" :index="child.path">
              <el-icon><component :is="child.icon" /></el-icon>
              <span>{{ child.title }}</span>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="topbar" height="58px">
        <div>
          <h1>{{ route.meta.title || '管理端' }}</h1>
          <span>{{ today }}</span>
        </div>
        <el-dropdown>
          <button class="user-button">
            <el-avatar :size="32">{{ initials }}</el-avatar>
            <span>{{ admin.adminName || 'admin' }}</span>
            <el-icon><ArrowDown /></el-icon>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { menuItems } from '@/router'
import { logout } from '@/api/auth'
import { clearAdmin, getAdmin, removeToken } from '@/utils/auth'

const route = useRoute()
const router = useRouter()
const admin = getAdmin()
const today = new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })

const activePath = computed(() => route.path)
const initials = computed(() => (admin.adminName || 'A').slice(0, 1).toUpperCase())

async function handleLogout() {
  await ElMessageBox.confirm('确认退出管理端吗？', '退出登录', { type: 'warning' })
  try {
    await logout()
  } catch (error) {
    // 本地清理仍然执行，避免后端不可达时卡住退出。
  }
  removeToken()
  clearAdmin()
  router.replace('/login')
}
</script>
