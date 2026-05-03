<template>
  <section>
    <div class="page-head">
      <div>
        <h2>POI 管理</h2>
        <p>维护小程序地图首页展示的校园地点与基础介绍。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增 POI</el-button>
    </div>

    <el-card class="panel" shadow="never">
      <div class="toolbar">
        <el-input v-model="query.keyword" clearable placeholder="地点名称" style="width: 220px" @keyup.enter="loadData" />
        <el-select v-model="query.status" clearable placeholder="状态" style="width: 140px">
          <el-option label="上架" :value="1" />
          <el-option label="下架" :value="0" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </div>

      <el-table v-loading="loading" :data="rows" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="地点名称" min-width="150" />
        <el-table-column prop="latitude" label="纬度" width="130" />
        <el-table-column prop="longitude" label="经度" width="130" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="简介" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" @click="openDialog(row)">编辑</el-button>
            <el-button link class="danger" :icon="Delete" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogOpen" :title="form.id ? '编辑 POI' : '新增 POI'" width="620px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="地点名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="纬度" prop="latitude">
              <el-input-number v-model="form.latitude" :precision="8" :min="-90" :max="90" :controls="false" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经度" prop="longitude">
              <el-input-number v-model="form.longitude" :precision="8" :min="-180" :max="180" :controls="false" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="冷知识">
          <el-input v-model="form.funFact" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogOpen = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { Delete, Edit, Plus, Refresh, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createPoi, deletePoi, listPois, updatePoi } from '@/api/poi'

const loading = ref(false)
const saving = ref(false)
const dialogOpen = ref(false)
const rows = ref([])
const formRef = ref()
const query = reactive({ keyword: '', status: undefined })
const form = reactive({})

const rules = {
  name: [{ required: true, message: '请输入地点名称', trigger: 'blur' }],
  latitude: [{ required: true, message: '请输入纬度', trigger: 'blur' }],
  longitude: [{ required: true, message: '请输入经度', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    rows.value = await listPois({
      keyword: query.keyword || undefined,
      status: query.status
    })
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.keyword = ''
  query.status = undefined
  loadData()
}

function openDialog(row) {
  Object.keys(form).forEach((key) => delete form[key])
  Object.assign(form, row ? { ...row } : { status: 1 })
  dialogOpen.value = true
  nextTick(() => formRef.value?.clearValidate())
}

async function submit() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.id) {
      await updatePoi(form.id, form)
    } else {
      await createPoi(form)
    }
    ElMessage.success('保存成功')
    dialogOpen.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除 POI「${row.name}」吗？`, '删除确认', { type: 'warning' })
  await deletePoi(row.id)
  ElMessage.success('删除成功')
  loadData()
}
</script>
