<template>
  <a-card title="卖家中心">
    <a-space direction="vertical" fill>
      <a-statistic title="我的商品" :value="12" />
      <a-statistic title="待审核商品" :value="2" />
      <a-alert type="warning">运营通过审核后，商品才会出现在买家商品列表。</a-alert>
    </a-space>
  </a-card>
</template>
