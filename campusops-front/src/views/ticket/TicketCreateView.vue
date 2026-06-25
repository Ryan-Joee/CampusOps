<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createTicket, getTicketCategories } from '../../api/ticket'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const categories = ref([])

const form = reactive({
  title: '',
  description: '',
  categoryId: null,
  priority: 'medium',
})

const rules = {
  title: [
    { required: true, message: '请输入工单标题', trigger: 'blur' },
    { max: 200, message: '标题不超过 200 字', trigger: 'blur' },
  ],
  description: [
    { required: true, message: '请输入问题描述', trigger: 'blur' },
  ],
  categoryId: [
    { required: true, message: '请选择工单分类', trigger: 'change' },
  ],
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' },
  ],
}

const priorityOptions = [
  { label: '低', value: 'low' },
  { label: '中', value: 'medium' },
  { label: '高', value: 'high' },
  { label: '紧急', value: 'urgent' },
]

async function fetchCategories() {
  try {
    const res = await getTicketCategories()
    if (res.code === 'SUCCESS') {
      categories.value = res.data || []
    }
  } catch {
    // categories remain empty, user can still submit without it
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await createTicket({
      title: form.title,
      description: form.description,
      categoryId: form.categoryId,
      priority: form.priority,
    })
    if (res.code === 'SUCCESS') {
      ElMessage.success('工单创建成功')
      router.push('/tickets')
    } else {
      ElMessage.error(res.message || '创建失败')
    }
  } catch (e) {
    ElMessage.error(e.message || '创建失败，请重试')
  } finally {
    loading.value = false
  }
}

function handleCancel() {
  router.back()
}

onMounted(fetchCategories)
</script>

<template>
  <div class="page-stack">
    <el-card shadow="never">
      <template #header>
        <div class="create-header">
          <span>新建工单</span>
          <el-button @click="handleCancel" text>返回列表</el-button>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        style="max-width: 640px"
      >
        <el-form-item label="标题" prop="title">
          <el-input
            v-model="form.title"
            placeholder="请简要描述您的问题"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="分类" prop="categoryId">
          <el-select
            v-model="form.categoryId"
            placeholder="请选择问题分类"
            style="width: 100%"
          >
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.categoryName"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="优先级" prop="priority">
          <el-radio-group v-model="form.priority">
            <el-radio-button
              v-for="item in priorityOptions"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="问题描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="5"
            placeholder="请详细描述您遇到的问题，包括操作步骤、错误现象等"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">
            提交工单
          </el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.create-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
