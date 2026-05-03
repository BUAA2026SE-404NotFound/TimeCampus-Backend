<template>
  <section>
    <div class="page-head">
      <div>
        <h2>运营首页</h2>
        <p>快速查看地点、官方内容、待审核 UGC 与最近审计记录。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="router.push('/pois')">新增 POI</el-button>
    </div>

    <el-row :gutter="16">
      <el-col v-for="item in stats" :key="item.label" :xs="24" :sm="12" :lg="6">
        <el-card class="stat" shadow="never">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="workbench">
      <el-col :xs="24" :lg="15">
        <el-card class="panel" shadow="never">
          <template #header>
            <div class="card-header">
              <span>待审核 UGC</span>
              <el-button text @click="router.push('/content/ugc')">查看全部</el-button>
            </div>
          </template>
          <el-table v-loading="loading" :data="pendingUgc" height="360">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="poiId" label="POI" width="90" />
            <el-table-column prop="year" label="年份" width="90" />
            <el-table-column prop="description" label="说明" min-width="180" show-overflow-tooltip />
            <el-table-column prop="createTime" label="提交时间" min-width="170" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="9">
        <el-card class="panel" shadow="never">
          <template #header>
            <div class="card-header">
              <span>最近审计日志</span>
              <el-button text @click="router.push('/logs')">查看全部</el-button>
            </div>
          </template>
          <div v-for="item in logs" :key="item.id" class="log-item">
            <b>{{ item.action }}</b>
            <span>{{ item.type }} / {{ item.targetType || '-' }}#{{ item.targetId || '-' }}</span>
            <small>{{ item.createTime }}</small>
          </div>
          <el-empty v-if="!logs.length" description="暂无日志" />
        </el-card>
      </el-col>
    </el-row>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { listPois } from '@/api/poi'
import { listMedia } from '@/api/media'
import { listUgc } from '@/api/ugc'
import { listLogs } from '@/api/log'

const router = useRouter()
const loading = ref(false)
const poiCount = ref(0)
const mediaCount = ref(0)
const pendingUgc = ref([])
const logs = ref([])

const stats = computed(() => [
  { label: 'POI 总数', value: poiCount.value },
  { label: '官方内容', value: mediaCount.value },
  { label: '待审核 UGC', value: pendingUgc.value.length },
  { label: '最近日志', value: logs.value.length }
])

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const [pois, media, ugc, auditLogs] = await Promise.all([
      listPois({}),
      listMedia({ type: 'official' }),
      listUgc('pending'),
      listLogs({ limit: 8 })
    ])
    poiCount.value = Array.isArray(pois) ? pois.length : 0
    mediaCount.value = Array.isArray(media) ? media.length : 0
    pendingUgc.value = Array.isArray(ugc) ? ugc.slice(0, 8) : []
    logs.value = Array.isArray(auditLogs) ? auditLogs : []
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.stat {
  min-height: 96px;
  margin-bottom: 16px;
}

.stat span {
  display: block;
  margin-bottom: 12px;
  color: var(--tc-muted);
}

.stat strong {
  color: var(--tc-primary);
  font-size: 30px;
}

.workbench {
  margin-top: 4px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.log-item {
  padding: 12px 0;
  border-bottom: 1px solid #edf1ef;
}

.log-item b,
.log-item span,
.log-item small {
  display: block;
}

.log-item span {
  margin: 5px 0;
  color: var(--tc-muted);
}

.log-item small {
  color: #98a4ad;
}
</style>
