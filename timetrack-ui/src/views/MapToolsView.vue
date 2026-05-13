<template>
  <section>
    <div class="page-head">
      <div>
        <h2>地图工具</h2>
        <p>调用后端封装的腾讯地图 WebService，用于逆地理编码和地点搜索辅助。</p>
      </div>
    </div>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="12">
        <el-card class="panel" shadow="never">
          <template #header>逆地理编码</template>
          <el-form :model="reverseForm" label-width="80px">
            <el-form-item label="纬度">
              <el-input-number v-model="reverseForm.lat" :precision="8" :controls="false" />
            </el-form-item>
            <el-form-item label="经度">
              <el-input-number v-model="reverseForm.lng" :precision="8" :controls="false" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="reverseLoading" @click="runReverse">查询</el-button>
            </el-form-item>
          </el-form>
          <pre v-if="reverseResult" class="json-block">{{ reverseResult }}</pre>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card class="panel" shadow="never">
          <template #header>地点搜索辅助</template>
          <el-form :model="searchForm" label-width="80px">
            <el-form-item label="关键字">
              <el-input v-model="searchForm.keyword" placeholder="例如：主楼" />
            </el-form-item>
            <el-form-item label="城市">
              <el-input v-model="searchForm.region" placeholder="例如：北京" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="searchLoading" @click="runSearch">搜索</el-button>
            </el-form-item>
          </el-form>
          <pre v-if="searchResult" class="json-block">{{ searchResult }}</pre>
        </el-card>
      </el-col>
    </el-row>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { poiSearch, reverseGeocode } from '@/api/map'

const reverseLoading = ref(false)
const searchLoading = ref(false)
const reverseResult = ref('')
const searchResult = ref('')
const reverseForm = reactive({ lat: 39.97611493, lng: 116.34067928 })
const searchForm = reactive({ keyword: '', region: '北京' })

async function runReverse() {
  reverseLoading.value = true
  try {
    const res = await reverseGeocode(reverseForm)
    reverseResult.value = JSON.stringify(res, null, 2)
  } finally {
    reverseLoading.value = false
  }
}

async function runSearch() {
  if (!searchForm.keyword) {
    ElMessage.warning('请输入关键字')
    return
  }
  searchLoading.value = true
  try {
    const res = await poiSearch(searchForm)
    searchResult.value = JSON.stringify(res, null, 2)
  } finally {
    searchLoading.value = false
  }
}
</script>
