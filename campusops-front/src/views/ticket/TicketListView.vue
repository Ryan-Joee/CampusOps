<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getTicketPage } from '../../api/ticket'

const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const filters = reactive({
  keyword: '',
  status: '',
  priority: '',
  page: 1,
  pageSize: 20,
})

const statusOptions = [
  { label: '待分派', value: 'pending_assignment' },
  { label: '待处理', value: 'pending_process' },
  { label: '处理中', value: 'processing' },
  { label: '待用户确认', value: 'pending_confirm' },
  { label: '已关闭', value: 'closed' },
  { label: '已驳回', value: 'rejected' },
  { label: '已取消', value: 'canceled' },
]

const priorityOptions = [
  { label: '低', value: 'low' },
  { label: '中', value: 'medium' },
  { label: '高', value: 'high' },
  { label: '紧急', value: 'urgent' },
]

const STATUS_TAG = {
  pending_assignment: '',
  pending_process: 'warning',
  processing: 'primary',
  pending_confirm: 'info',
  closed: 'success',
  rejected: 'danger',
  canceled: 'info',
}

const STATUS_LABEL = {
  pending_assignment: '待分派',
  pending_process: '待处理',
  processing: '处理中',
  pending_confirm: '待用户确认',
  closed: '已关闭',
  rejected: '已驳回',
  canceled: '已取消',
}

const PRIORITY_TAG = {
  low: 'info',
  medium: '',
  high: 'warning',
  urgent: 'danger',
}

const PRIORITY_LABEL = {
  low: '低',
  medium: '中',
  high: '高',
  urgent: '紧急',
}

async function fetchTickets() {
  loading.value = true
  try {
    const params = {}
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.status) params.status = filters.status
    if (filters.priority) params.priority = filters.priority
    params.page = filters.page
    params.pageSize = filters.pageSize

    const res = await getTicketPage(params)
    if (res.code === 'SUCCESS') {
      const page = res.data
      tableData.value = page.items || []
      total.value = page.total || 0
    } else {
      tableData.value = []
      total.value = 0
    }
  } catch (e) {
    ElMessage.error(e.message || '获取工单列表失败')
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  filters.page = 1
  fetchTickets()
}

function handleReset() {
  filters.keyword = ''
  filters.status = ''
  filters.priority = ''
  filters.page = 1
  fetchTickets()
}

function handleSizeChange(size) {
  filters.pageSize = size
  filters.page = 1
  fetchTickets()
}

function handleCurrentChange(page) {
  filters.page = page
  fetchTickets()
}

function formatDate(val) {
  if (!val) return '-'
  return val.replace('T', ' ').slice(0, 16)
}

onMounted(fetchTickets)
</script>

<template>
  <div class="page-stack">
    <el-card shadow="never">
      <el-form inline @submit.prevent="handleSearch">
        <el-form-item label="关键词">
          <el-input
            v-model="filters.keyword"
            placeholder="标题、编号、提交人"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="filters.status"
            placeholder="全部状态"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select
            v-model="filters.priority"
            placeholder="全部优先级"
            clearable
            style="width: 140px"
          >
            <el-option
              v-for="item in priorityOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="router.push('/tickets/create')">新建工单</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table
        :data="tableData"
        v-loading="loading"
        empty-text="暂无工单数据"
      >
        <el-table-column prop="ticketNo" label="工单编号" width="160" />
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="STATUS_TAG[row.status]" size="small">
              {{ STATUS_LABEL[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="PRIORITY_TAG[row.priority]" size="small" effect="plain">
              {{ PRIORITY_LABEL[row.priority] || row.priority }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitterName" label="提交人" width="120" />
        <el-table-column prop="assigneeName" label="处理人" width="120">
          <template #default="{ row }">
            {{ row.assigneeName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="filters.page"
          v-model:page-size="filters.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
