<template>
  <section>
    <div class="page-head">
      <div>
        <h2>官方内容</h2>
        <p>查看与批量导入官方历史影像，作为时间切换和地图封面的可信内容源。</p>
      </div>
      <el-button type="primary" :icon="Upload" @click="importOpen = true">批量导入</el-button>
    </div>

    <el-card class="panel" shadow="never">
      <div class="toolbar">
        <el-input v-model="query.poiId" clearable placeholder="POI ID" style="width: 140px" />
        <el-input-number v-model="query.yearFrom" :controls="false" placeholder="起始年份" style="width: 140px" />
        <el-input-number v-model="query.yearTo" :controls="false" placeholder="结束年份" style="width: 140px" />
        <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </div>

      <el-table v-loading="loading" :data="rows" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="poiId" label="POI" width="90" />
        <el-table-column label="影像" min-width="300">
          <template #default="{ row }">
            <div class="media-cell">
              <el-image
                :src="row.previewUrl"
                fit="cover"
                :preview-src-list="row.previewUrl ? [row.previewUrl] : []"
                preview-teleported
              >
                <template #error>
                  <div class="image-error">无法预览</div>
                </template>
              </el-image>
              <div>
                <b>{{ row.year || '-' }} · {{ row.type }}</b>
                <span>{{ row.imagePath?.startsWith('http') ? '远程 URL' : '本地文件' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="220" show-overflow-tooltip />
        <el-table-column prop="reviewStatus" label="审核状态" width="110" />
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button link class="danger" :icon="Delete" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="importOpen" title="批量导入官方内容" width="900px">
      <el-alert type="info" show-icon :closable="false" title="每行一条内容，图片路径可为 CDN URL 或后端可访问的本地上传 URL。" />
      <el-table :data="items" border class="import-table">
        <el-table-column label="POI ID" width="130">
          <template #default="{ row }">
            <el-input-number v-model="row.poiId" :controls="false" :min="1" />
          </template>
        </el-table-column>
        <el-table-column label="年份" width="130">
          <template #default="{ row }">
            <el-input-number v-model="row.year" :controls="false" :min="1953" :max="2100" />
          </template>
        </el-table-column>
        <el-table-column label="图片路径" min-width="260">
          <template #default="{ row }">
            <el-input v-model="row.imagePath" />
          </template>
        </el-table-column>
        <el-table-column label="说明" min-width="180">
          <template #default="{ row }">
            <el-input v-model="row.description" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ $index }">
            <el-button link class="danger" @click="items.splice($index, 1)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-button class="add-row" :icon="Plus" @click="addItem">添加一行</el-button>
      <template #footer>
        <el-button @click="importOpen = false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="submitImport">导入</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Delete, Plus, Refresh, Search, Upload } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteMedia, importOfficial, listMedia } from '@/api/media'

const loading = ref(false)
const importing = ref(false)
const importOpen = ref(false)
const rows = ref([])
const items = ref([])
const query = reactive({ poiId: undefined, yearFrom: undefined, yearTo: undefined })

onMounted(() => {
  loadData()
  addItem()
})

async function loadData() {
  loading.value = true
  try {
    rows.value = await listMedia({
      type: 'official',
      poiId: query.poiId || undefined,
      yearFrom: query.yearFrom,
      yearTo: query.yearTo
    })
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.poiId = undefined
  query.yearFrom = undefined
  query.yearTo = undefined
  loadData()
}

function addItem() {
  items.value.push({ poiId: undefined, year: undefined, imagePath: '', description: '', reviewStatus: 'approved' })
}

async function submitImport() {
  const payload = items.value.filter((item) => item.poiId && item.year && item.imagePath)
  if (!payload.length) {
    ElMessage.warning('请至少填写一条完整内容')
    return
  }
  importing.value = true
  try {
    const result = await importOfficial(payload)
    ElMessage.success(`导入完成：成功 ${result.successCount} 条，失败 ${result.failCount} 条`)
    importOpen.value = false
    items.value = []
    addItem()
    loadData()
  } finally {
    importing.value = false
  }
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除影像 #${row.id} 吗？`, '删除确认', { type: 'warning' })
  await deleteMedia(row.id)
  ElMessage.success('删除成功')
  loadData()
}
</script>

<style scoped>
.import-table {
  margin-top: 12px;
}

.add-row {
  margin-top: 12px;
}

.media-cell {
  display: grid;
  grid-template-columns: 96px 1fr;
  gap: 12px;
  align-items: center;
}

.media-cell .el-image {
  width: 96px;
  height: 68px;
  border: 1px solid var(--tc-border);
  border-radius: 6px;
  background: #f2f5fa;
}

.media-cell b,
.media-cell span {
  display: block;
}

.media-cell span {
  margin-top: 6px;
  color: var(--tc-muted);
  font-size: 12px;
}

.image-error {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--tc-muted);
  font-size: 12px;
}
</style>
