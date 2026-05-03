<template>
  <main class="login-page">
    <section class="brand-copy">
      <div class="brand-mark large">TC</div>
      <h1>时光航迹管理端</h1>
      <p>管理校园历史 POI、官方影像、UGC 审核、地图辅助和审计日志。</p>
    </section>
    <el-card class="login-card" shadow="never">
      <h2>管理员登录</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="handleLogin">
        <el-form-item label="账号" prop="adminName">
          <el-input v-model="form.adminName" placeholder="admin" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" size="large" />
        </el-form-item>
        <el-button class="submit" type="primary" size="large" :loading="loading" @click="handleLogin">登录</el-button>
      </el-form>
    </el-card>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { setAdmin, setToken } from '@/utils/auth'

const router = useRouter()
const route = useRoute()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  adminName: '',
  password: ''
})

const rules = {
  adminName: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await login(form)
    setToken(res.token)
    setAdmin({ adminId: res.adminId, adminName: res.adminName })
    ElMessage.success('登录成功')
    router.replace(route.query.redirect || '/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: grid;
  grid-template-columns: minmax(320px, 1fr) 420px;
  gap: 56px;
  align-items: center;
  min-height: 100%;
  padding: 48px 10%;
  background: linear-gradient(135deg, #f7faf7 0%, #edf4f5 48%, #f8f3ed 100%);
}

.brand-copy {
  max-width: 560px;
}

.large {
  width: 64px;
  height: 64px;
  line-height: 64px;
  margin-bottom: 24px;
  color: #fff;
  font-size: 18px;
}

h1 {
  margin: 0 0 16px;
  font-size: 44px;
  letter-spacing: 0;
}

p {
  margin: 0;
  color: var(--tc-muted);
  font-size: 18px;
  line-height: 1.7;
}

.login-card {
  border-radius: var(--tc-card-radius);
}

.login-card h2 {
  margin: 0 0 24px;
}

.submit {
  width: 100%;
}

@media (max-width: 900px) {
  .login-page {
    grid-template-columns: 1fr;
    padding: 32px 20px;
  }

  h1 {
    font-size: 32px;
  }
}
</style>
