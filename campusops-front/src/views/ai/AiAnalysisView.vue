<template>
  <div class="page-stack">
    <el-card shadow="never">
      <template #header>
        <span>AI 分析记录</span>
      </template>
      <el-alert
        title="当前阶段先保留 AI 能力入口，后续接入工单分类、摘要、相似问题推荐。"
        type="info"
        show-icon
        :closable="false"
      />
      <el-table :data="[]" empty-text="暂无 AI 分析记录" class="mt-16">
        <el-table-column prop="businessType" label="业务类型" width="140" />
        <el-table-column prop="analysisType" label="分析类型" width="160" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
      </el-table>
    </el-card>
  </div>
</template>
