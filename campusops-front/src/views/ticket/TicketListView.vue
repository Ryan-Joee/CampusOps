<template>
  <div class="page-stack">
    <el-card shadow="never">
      <el-form inline>
        <el-form-item label="关键词">
          <el-input placeholder="标题、编号、提交人" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select placeholder="全部状态" clearable style="width: 160px">
            <el-option label="待分派" value="pending" />
            <el-option label="处理中" value="processing" />
            <el-option label="已解决" value="resolved" />
            <el-option label="已关闭" value="closed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary">查询</el-button>
          <el-button>新建工单</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="[]" empty-text="暂无工单数据">
        <el-table-column prop="code" label="工单编号" width="140" />
        <el-table-column prop="title" label="标题" min-width="220" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="priority" label="优先级" width="120" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
      </el-table>
    </el-card>
  </div>
</template>
