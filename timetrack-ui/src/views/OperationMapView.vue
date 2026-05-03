<template>
  <section>
    <div class="page-head">
      <div>
        <h2>运营地图</h2>
        <p>在地图视图中查看所有 POI，并联动浏览收藏、评论、媒体和 UGC 数据。</p>
      </div>
      <el-button type="primary" :icon="Refresh" :loading="loading" @click="loadData">刷新</el-button>
    </div>

    <el-card class="panel" shadow="never">
      <div class="toolbar">
        <el-input v-model="query.keyword" clearable placeholder="POI 名称" style="width: 220px" @keyup.enter="loadData" />
        <el-select v-model="query.status" clearable placeholder="POI 状态" style="width: 140px">
          <el-option label="上架" :value="1" />
          <el-option label="下架" :value="0" />
        </el-select>
        <el-select v-model="query.commentStatus" clearable placeholder="评论状态" style="width: 150px">
          <el-option label="待审核" value="pending" />
          <el-option label="已通过" value="approved" />
          <el-option label="已驳回" value="rejected" />
        </el-select>
        <el-input-number v-model="query.limit" :min="1" :max="200" :controls="false" style="width: 120px" />
        <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
      </div>

      <el-row :gutter="16">
        <el-col :xs="24" :lg="16">
          <div class="map-shell">
            <tlbs-map
              v-if="mapKey"
              class="tencent-map"
              :api-key="mapKey"
              :center="mapCenter"
              :zoom="mapZoom"
              :min-zoom="14"
              :max-zoom="19"
              :control="mapControl"
              @map_inited="handleMapInited"
            >
              <tlbs-multi-marker
                v-if="markerGeometries.length"
                :styles="markerStyles"
                :geometries="markerGeometries"
                @click="handleMarkerClick"
              />
            </tlbs-map>
            <div v-else class="map-placeholder">
              <b>腾讯地图 Key 未配置</b>
              <span>请确认后端 `tencent-map.key` 已配置，并重新刷新页面。</span>
            </div>
            <div class="map-legend">
              <span><i class="legend-dot active"></i>上架 POI</span>
              <span><i class="legend-dot offline"></i>下架 POI</span>
              <span>数字表示收藏数</span>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :lg="8">
          <el-card class="poi-detail" shadow="never">
            <template v-if="selectedPoi">
              <div class="detail-title">
                <h3>{{ selectedPoi.name }}</h3>
                <el-tag :type="selectedPoi.status === 1 ? 'success' : 'info'">{{ selectedPoi.status === 1 ? '上架' : '下架' }}</el-tag>
              </div>
              <p>{{ selectedPoi.description || '暂无简介' }}</p>
              <div class="metric-grid">
                <div><b>{{ selectedPoi.favoriteCount || 0 }}</b><span>收藏</span></div>
                <div><b>{{ selectedPoi.commentCount || 0 }}</b><span>评论</span></div>
                <div><b>{{ selectedPoi.pendingCommentCount || 0 }}</b><span>待审评论</span></div>
                <div><b>{{ selectedPoi.mediaCount || 0 }}</b><span>影像</span></div>
                <div><b>{{ selectedPoi.ugcCount || 0 }}</b><span>UGC</span></div>
              </div>
              <dl>
                <dt>纬度</dt>
                <dd>{{ selectedPoi.latitude }}</dd>
                <dt>经度</dt>
                <dd>{{ selectedPoi.longitude }}</dd>
              </dl>
              <el-tabs class="poi-tabs">
                <el-tab-pane label="影像">
                  <div v-if="selectedPoi.mediaList && selectedPoi.mediaList.length" class="media-list">
                    <div v-for="media in selectedPoi.mediaList" :key="media.id" class="media-item">
                      <el-image :src="media.previewUrl" fit="cover" :preview-src-list="[media.previewUrl]" preview-teleported />
                      <div>
                        <b>{{ media.year || '-' }} · {{ media.type }}</b>
                        <span>{{ media.description || '暂无说明' }}</span>
                      </div>
                    </div>
                  </div>
                  <el-empty v-else description="暂无影像" />
                </el-tab-pane>
                <el-tab-pane label="收藏用户">
                  <div v-if="selectedPoi.favorites && selectedPoi.favorites.length" class="compact-list">
                    <div v-for="favorite in selectedPoi.favorites" :key="favorite.id">
                      <b>{{ favorite.nickname || ('用户 #' + favorite.userId) }}</b>
                      <span>{{ favorite.targetName || favorite.targetType + ' #' + favorite.targetId }}</span>
                    </div>
                  </div>
                  <el-empty v-else description="暂无收藏" />
                </el-tab-pane>
                <el-tab-pane label="评论">
                  <div v-if="selectedPoi.comments && selectedPoi.comments.length" class="compact-list">
                    <div v-for="comment in selectedPoi.comments" :key="comment.id">
                      <b>{{ comment.nickname || ('用户 #' + comment.userId) }} · {{ comment.reviewStatus }}</b>
                      <span>{{ comment.content }}</span>
                    </div>
                  </div>
                  <el-empty v-else description="暂无评论" />
                </el-tab-pane>
              </el-tabs>
            </template>
            <el-empty v-else description="选择一个 POI 查看详情" />
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="16" class="tables">
      <el-col :xs="24" :lg="12">
        <el-card class="panel" shadow="never">
          <template #header>最近收藏</template>
          <el-table :data="overview.recentFavorites" height="320">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="nickname" label="用户" width="120" show-overflow-tooltip />
            <el-table-column label="目标" min-width="150" show-overflow-tooltip>
              <template #default="{ row }">{{ row.targetName || row.targetType + ' #' + row.targetId }}</template>
            </el-table-column>
            <el-table-column prop="createTime" label="时间" width="170" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card class="panel" shadow="never">
          <template #header>最近评论</template>
          <el-table :data="overview.recentComments" height="320">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="nickname" label="用户" width="120" show-overflow-tooltip />
            <el-table-column prop="reviewStatus" label="状态" width="90" />
            <el-table-column prop="content" label="内容" min-width="180" show-overflow-tooltip />
            <el-table-column prop="createTime" label="时间" width="170" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { Refresh, Search } from '@element-plus/icons-vue'
import { getAdminMapConfig, getAdminMapOverview } from '@/api/adminMap'

const BUAA_CENTER = { lat: 39.97611493, lng: 116.34067928 } // Tencent GCJ02 center around BUAA.
const loading = ref(false)
const selectedPoi = ref(null)
const mapKey = ref('')
const mapCenter = ref({ ...BUAA_CENTER })
const mapZoom = ref(16)
const mapInstance = ref(null)
const mapControl = { zoom: true, scale: true, rotation: false }
const overview = reactive({
  pois: [],
  recentFavorites: [],
  recentComments: []
})
const query = reactive({
  keyword: '',
  status: undefined,
  commentStatus: undefined,
  limit: 50
})

onMounted(async () => {
  await loadMapConfig()
  await loadData()
})

const validPois = computed(() => overview.pois.filter((poi) => poi.latitude != null && poi.longitude != null))

const markerStyles = computed(() => {
  return Object.fromEntries(validPois.value.map((poi) => [markerStyleId(poi), makeMarkerStyle(poi)]))
})

const markerGeometries = computed(() => {
  return validPois.value.map((poi) => ({
    id: String(poi.id),
    styleId: markerStyleId(poi),
    position: poiPosition(poi),
    properties: { poiId: poi.id }
  }))
})

async function loadMapConfig() {
  const config = await getAdminMapConfig()
  mapKey.value = config.tencentMapKey || import.meta.env.VITE_TENCENT_MAP_KEY || ''
}

async function loadData() {
  loading.value = true
  try {
    const data = await getAdminMapOverview({
      keyword: query.keyword || undefined,
      status: query.status,
      commentStatus: query.commentStatus,
      limit: query.limit
    })
    overview.pois = data.pois || []
    overview.recentFavorites = data.recentFavorites || []
    overview.recentComments = data.recentComments || []
    syncSelection()
  } finally {
    loading.value = false
  }
}

function handleMapInited(map) {
  mapInstance.value = map
}

function handleMarkerClick(event) {
  const poiId = Number(event?.geometry?.properties?.poiId || event?.geometry?.id)
  const poi = validPois.value.find((item) => Number(item.id) === poiId)
  if (poi) {
    selectPoi(poi)
  }
}

function syncSelection() {
  if (!validPois.value.length) {
    selectedPoi.value = null
    mapCenter.value = { ...BUAA_CENTER }
    return
  }
  const current = validPois.value.find((poi) => poi.id === selectedPoi.value?.id)
  selectPoi(current || validPois.value[0], false)
}

function selectPoi(poi, zoomIn = true) {
  selectedPoi.value = poi
  mapCenter.value = poiPosition(poi)
  if (zoomIn) {
    mapZoom.value = Math.max(mapZoom.value, 17)
  }
}

function poiPosition(poi) {
  return { lat: Number(poi.latitude), lng: Number(poi.longitude) }
}

function markerStyleId(poi) {
  return `poi-${poi.id}-${poi.status === 1 ? 'active' : 'offline'}-${poi.favoriteCount || 0}`
}

function makeMarkerStyle(poi) {
  return {
    width: 38,
    height: 46,
    anchor: { x: 19, y: 42 },
    src: markerSvg(poi)
  }
}

function markerSvg(poi) {
  const active = poi.status === 1
  const fill = active ? '#2f5fcb' : '#7b8794'
  const stroke = active ? '#173f9a' : '#5d6672'
  const count = String(poi.favoriteCount || 0)
  const safeCount = count.length > 2 ? '99+' : count
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" width="38" height="46" viewBox="0 0 38 46">
      <path fill="${fill}" stroke="${stroke}" stroke-width="2" d="M19 44C14.2 36.6 5 28.2 5 18.5 5 10.5 11.3 4 19 4s14 6.5 14 14.5C33 28.2 23.8 36.6 19 44Z"/>
      <circle cx="19" cy="18.5" r="10.5" fill="#fff"/>
      <text x="19" y="23" text-anchor="middle" font-family="Arial, sans-serif" font-size="12" font-weight="700" fill="${fill}">${safeCount}</text>
    </svg>`
  return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`
}
</script>

<style scoped>
.map-shell {
  position: relative;
  min-height: 560px;
  overflow: hidden;
  border: 1px solid var(--tc-border);
  border-radius: 8px;
  background: #eef5f2;
}

.tencent-map,
.map-placeholder {
  width: 100%;
  height: 560px;
}

.map-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--tc-muted);
  background: #f3f7fb;
}

.map-placeholder b {
  color: var(--tc-text);
}

.map-legend {
  position: absolute;
  left: 12px;
  bottom: 12px;
  z-index: 500;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  max-width: calc(100% - 24px);
  padding: 8px 10px;
  border: 1px solid rgba(16, 35, 29, 0.12);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 8px 20px rgba(25, 58, 48, 0.12);
}

.map-legend span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--tc-muted);
  font-size: 12px;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--tc-primary);
}

.legend-dot.offline {
  background: #909399;
}

.poi-detail {
  min-height: 560px;
}

.detail-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.detail-title h3 {
  margin: 0;
  font-size: 20px;
}

.poi-detail p {
  color: var(--tc-muted);
  line-height: 1.7;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin: 18px 0;
}

.metric-grid div {
  padding: 12px;
  border: 1px solid var(--tc-border);
  border-radius: 8px;
  background: #f8fbfa;
}

.metric-grid b,
.metric-grid span {
  display: block;
}

.metric-grid b {
  color: var(--tc-primary);
  font-size: 22px;
}

.metric-grid span {
  margin-top: 4px;
  color: var(--tc-muted);
}

dl {
  display: grid;
  grid-template-columns: 60px 1fr;
  gap: 8px 12px;
}

dt {
  color: var(--tc-muted);
}

dd {
  margin: 0;
}

.tables {
  margin-top: 16px;
}

.poi-tabs {
  margin-top: 16px;
}

.media-list,
.compact-list {
  display: grid;
  gap: 10px;
  max-height: 260px;
  overflow: auto;
}

.media-item {
  display: grid;
  grid-template-columns: 82px 1fr;
  gap: 10px;
  align-items: center;
}

.media-item .el-image {
  width: 82px;
  height: 58px;
  border-radius: 6px;
  background: #f2f5fa;
}

.media-item b,
.media-item span,
.compact-list b,
.compact-list span {
  display: block;
}

.media-item span,
.compact-list span {
  margin-top: 3px;
  color: var(--tc-muted);
  line-height: 1.5;
}

.compact-list div {
  padding: 10px;
  border: 1px solid var(--tc-border);
  border-radius: 8px;
  background: #f8faff;
}
</style>
