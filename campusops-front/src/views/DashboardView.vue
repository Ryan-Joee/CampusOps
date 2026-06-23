<template>
  <div class="page-stack">
    <el-row :gutter="16">
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-label">待处理工单</div>
          <div class="stat-value">18</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-label">处理中</div>
          <div class="stat-value">32</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-label">今日关闭</div>
          <div class="stat-value">9</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-label">AI 建议采纳</div>
          <div class="stat-value">76%</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <template #header>
        <span>近期工单</span>
      </template>
      <el-table :data="[]" empty-text="暂无数据，等待后端接口接入" />
    </el-card>
  </div>
</template>
