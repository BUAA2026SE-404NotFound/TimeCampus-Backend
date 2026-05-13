<template>
  <section>
    <div class="page-head">
      <div>
        <h2>评论审核</h2>
        <p>查看用户对 POI 与影像的评论，处理待审核内容并追踪驳回原因。</p>
      </div>
    </div>

    <el-card class="panel" shadow="never">
      <div class="toolbar">
        <el-radio-group v-model="query.status" @change="loadData">
          <el-radio-button label="pending">待审核</el-radio-button>
          <el-radio-button label="approved">已通过</el-radio-button>
          <el-radio-button label="rejected">已驳回</el-radio-button>
          <el-radio-button label="">全部</el-radio-button>
        </el-radio-group>
        <el-select v-model="query.targetType" clearable placeholder="目标类型" style="width: 140px" @change="loadData">
          <el-option label="POI" value="poi" />
          <el-option label="影像" value="media" />
        </el-select>
        <el-input-number v-model="query.targetId" :min="1" :controls="false" placeholder="目标 ID" style="width: 120px" />
        <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </div>

      <el-table v-loading="loading" :data="rows" border class="comment-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="目标" width="150">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ targetLabel(row.targetType) }}</el-tag>
            <span class="target-id">#{{ row.targetId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="用户" width="150">
          <template #default="{ row }">
            <span>{{ row.nickname || `用户 #${row.userId}` }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评论内容" min-width="280" show-overflow-tooltip />
        <el-table-column prop="reviewStatus" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.reviewStatus)" effect="light">{{ statusText(row.reviewStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="驳回原因" min-width="180" show-overflow-tooltip />
        <el-table-column prop="createTime" label="提交时间" width="180" />
        <el-table-column prop="reviewTime" label="审核时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <template v-if="row.reviewStatus === 'pending'">
              <el-button link type="primary" :icon="Check" @click="approve(row)">通过</el-button>
              <el-button link class="danger" :icon="Close" @click="openReject(row)">驳回</el-button>
            </template>
            <el-button v-else link :icon="View" @click="openDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="detailOpen" title="评论详情" width="560px">
      <el-descriptions v-if="current" :column="1" border>
        <el-descriptions-item label="评论 ID">#{{ current.id }}</el-descriptions-item>
        <el-descriptions-item label="目标">{{ targetLabel(current.targetType) }} #{{ current.targetId }}</el-descriptions-item>
        <el-descriptions-item label="用户">{{ current.nickname || `用户 #${current.userId}` }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusText(current.reviewStatus) }}</el-descriptions-item>
        <el-descriptions-item label="内容">{{ current.content }}</el-descriptions-item>
        <el-descriptions-item v-if="current.rejectReason" label="驳回原因">{{ current.rejectReason }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailOpen = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rejectOpen" title="驳回评论" width="460px">
      <el-input v-model="rejectReason" type="textarea" :rows="4" placeholder="请填写驳回原因" />
      <template #footer>
        <el-button @click="rejectOpen = false">取消</el-button>
        <el-button type="danger" :loading="saving" @click="reject">确认驳回</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Check, Close, Refresh, Search, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { approveComment, listComments, rejectComment } from '@/api/comment'

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const current = ref(null)
const detailOpen = ref(false)
const rejectOpen = ref(false)
const rejectReason = ref('')
const query = reactive({
  status: 'pending',
  targetType: undefined,
  targetId: undefined
})

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    rows.value = await listComments({
      status: query.status || undefined,
      targetType: query.targetType || undefined,
      targetId: query.targetId || undefined
    })
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.status = 'pending'
  query.targetType = undefined
  query.targetId = undefined
  loadData()
}

async function approve(row) {
  await ElMessageBox.confirm(`确认通过评论 #${row.id} 吗？`, '审核确认', { type: 'warning' })
  await approveComment(row.id)
  ElMessage.success('已通过')
  loadData()
}

function openReject(row) {
  current.value = row
  rejectReason.value = ''
  rejectOpen.value = true
}

async function reject() {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请填写驳回原因')
    return
  }
  saving.value = true
  try {
    await rejectComment(current.value.id, rejectReason.value)
    ElMessage.success('已驳回')
    rejectOpen.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

function openDetail(row) {
  current.value = row
  detailOpen.value = true
}

function targetLabel(type) {
  return type === 'media' ? '影像' : 'POI'
}

function statusText(status) {
  const map = { pending: '待审核', approved: '已通过', rejected: '已驳回' }
  return map[status] || status || '-'
}

function statusType(status) {
  const map = { pending: 'warning', approved: 'success', rejected: 'danger' }
  return map[status] || 'info'
}
</script>

<style scoped>
.comment-table {
  margin-top: 14px;
}

.target-id {
  margin-left: 6px;
  color: var(--el-text-color-secondary);
}
</style>
