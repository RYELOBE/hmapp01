<template>
  <a-row :gutter="16">
    <a-col :span="8">
      <a-card>
        <a-statistic title="待审核商品" :value="9" />
      </a-card>
    </a-col>
    <a-col :span="8">
      <a-card>
        <a-statistic title="今日通过" :value="18" />
      </a-card>
    </a-col>
    <a-col :span="8">
      <a-card>
        <a-statistic title="今日驳回" :value="3" />
      </a-card>
    </a-col>
  </a-row>
</template>
