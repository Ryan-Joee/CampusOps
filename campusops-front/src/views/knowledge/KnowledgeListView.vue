<template>
  <div class="page-stack">
    <el-card shadow="never">
      <template #header>
        <span>知识库文章</span>
      </template>
      <el-table :data="[]" empty-text="暂无知识库文章">
        <el-table-column prop="title" label="标题" min-width="240" />
        <el-table-column prop="category" label="分类" width="160" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="updatedAt" label="更新时间" width="180" />
      </el-table>
    </el-card>
  </div>
</template>
