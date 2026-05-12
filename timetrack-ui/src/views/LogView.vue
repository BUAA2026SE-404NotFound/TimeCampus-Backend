<template>
  <section>
    <div class="page-head">
      <div>
        <h2>审计日志</h2>
        <p>查询管理端与用户端产生的关键行为记录。</p>
      </div>
    </div>

    <el-card class="panel" shadow="never">
      <div class="toolbar">
        <el-select v-model="query.operatorType" clearable placeholder="操作者" style="width: 140px">
          <el-option label="ADMIN" value="ADMIN" />
          <el-option label="USER" value="USER" />
        </el-select>
        <el-select v-model="query.type" clearable placeholder="类型" style="width: 150px">
          <el-option v-for="type in types" :key="type" :label="type" :value="type" />
        </el-select>
        <el-input v-model="query.action" clearable placeholder="动作" style="width: 160px" />
        <el-input v-model="query.targetType" clearable placeholder="目标类型" style="width: 160px" />
        <el-input-number v-model="query.limit" :min="1" :max="200" :controls="false" style="width: 120px" />
        <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </div>

      <el-table v-loading="loading" :data="rows" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="operatorType" label="操作者" width="100" />
        <el-table-column prop="operatorId" label="操作者 ID" width="110" />
        <el-table-column prop="type" label="类型" width="110" />
        <el-table-column prop="action" label="动作" width="130" />
        <el-table-column label="目标" width="170">
          <template #default="{ row }">{{ row.targetType || '-' }} #{{ row.targetId || '-' }}</template>
        </el-table-column>
        <el-table-column prop="detail" label="详情" min-width="260" show-overflow-tooltip />
        <el-table-column prop="createTime" label="时间" width="180" />
      </el-table>
    </el-card>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Refresh, Search } from '@element-plus/icons-vue'
import { listLogs } from '@/api/log'

const types = ['auth', 'content', 'review', 'behavior', 'system']
const loading = ref(false)
const rows = ref([])
const query = reactive({
  operatorType: undefined,
  type: undefined,
  action: '',
  targetType: '',
  limit: 50
})

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    rows.value = await listLogs({
      operatorType: query.operatorType,
      type: query.type,
      action: query.action || undefined,
      targetType: query.targetType || undefined,
      limit: query.limit
    })
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.operatorType = undefined
  query.type = undefined
  query.action = ''
  query.targetType = ''
  query.limit = 50
  loadData()
}
</script>
