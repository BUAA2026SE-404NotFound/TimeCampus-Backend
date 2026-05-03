<template>
  <section>
    <div class="page-head">
      <div>
        <h2>UGC 审核</h2>
        <p>处理用户上传内容，通过后发布，驳回时必须填写原因。</p>
      </div>
    </div>

    <el-card class="panel" shadow="never">
      <el-radio-group v-model="status" @change="loadData">
        <el-radio-button label="pending">待审核</el-radio-button>
        <el-radio-button label="approved">已通过</el-radio-button>
        <el-radio-button label="rejected">已驳回</el-radio-button>
      </el-radio-group>

      <el-table v-loading="loading" :data="rows" border class="ugc-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="poiId" label="POI" width="90" />
        <el-table-column prop="year" label="年份" width="90" />
        <el-table-column prop="imagePath" label="图片" min-width="180">
          <template #default="{ row }">
            <el-link :href="row.imagePath" target="_blank" type="primary">查看图片</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="220" show-overflow-tooltip />
        <el-table-column prop="uploadUserId" label="上传用户" width="110" />
        <el-table-column prop="reviewStatus" label="状态" width="110" />
        <el-table-column prop="rejectReason" label="驳回原因" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <template v-if="row.reviewStatus === 'pending'">
              <el-button link type="primary" :icon="Check" @click="approve(row)">通过</el-button>
              <el-button link class="danger" :icon="Close" @click="openReject(row)">驳回</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="rejectOpen" title="驳回 UGC" width="460px">
      <el-input v-model="rejectReason" type="textarea" :rows="4" placeholder="请填写驳回原因" />
      <template #footer>
        <el-button @click="rejectOpen = false">取消</el-button>
        <el-button type="danger" :loading="saving" @click="reject">确认驳回</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { Check, Close } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { approveUgc, listUgc, rejectUgc } from '@/api/ugc'

const status = ref('pending')
const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const current = ref(null)
const rejectOpen = ref(false)
const rejectReason = ref('')

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    rows.value = await listUgc(status.value)
  } finally {
    loading.value = false
  }
}

async function approve(row) {
  await ElMessageBox.confirm(`确认通过 UGC #${row.id} 吗？`, '审核确认', { type: 'warning' })
  await approveUgc(row.id)
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
    await rejectUgc(current.value.id, rejectReason.value)
    ElMessage.success('已驳回')
    rejectOpen.value = false
    loadData()
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.ugc-table {
  margin-top: 14px;
}
</style>
