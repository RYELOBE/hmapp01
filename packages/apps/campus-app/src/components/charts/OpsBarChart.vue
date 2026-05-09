<template>
  <div class="ops-bar-chart" :style="{ height: height + 'px' }">
    <v-chart v-if="chartOptions" :option="chartOptions" autoresize class="echarts-container" />
    <div v-else class="empty-state">暂无数据</div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import VChart from 'vue-echarts';
import { use } from 'echarts/core';
import { BarChart } from 'echarts/charts';
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import * as echarts from 'echarts/core';

use([
  BarChart,
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  CanvasRenderer,
]);

const props = defineProps({
  data: {
    type: Array,
    default: () => [],
  },
  title: {
    type: String,
    default: '',
  },
  height: {
    type: Number,
    default: 280,
  },
  colors: {
    type: Array,
    default: () => ['#165DFF', '#00B42A', '#FF7D00', '#722ED1', '#F53F3F'],
  },
});

const chartOptions = computed(() => {
  if (!props.data || props.data.length === 0) return null;

  const categories = props.data.map(item => item.name);
  const values = props.data.map(item => item.count);
  const maxValue = Math.max(...values, 1);

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e5e6eb',
      borderWidth: 1,
      textStyle: { color: '#1d2129', fontSize: 13 },
      formatter: (params) => {
        const item = params[0];
        return `<strong>${item.name}</strong><br/>数量: <span style="color:#165DFF;font-weight:600">${item.value}</span>`;
      },
    },
    grid: {
      left: '3%',
      right: '8%',
      bottom: '3%',
      top: '10%',
      containLabel: true,
    },
    xAxis: {
      type: 'value',
      show: false,
    },
    yAxis: {
      type: 'category',
      data: categories.reverse(),
      axisTick: { show: false },
      axisLine: { show: false },
      axisLabel: {
        color: '#4e5969',
        fontSize: 13,
        fontWeight: 500,
      },
    },
    series: [
      {
        type: 'bar',
        data: values.reverse().map((value, index) => ({
          value,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: props.colors[index % props.colors.length] },
              { offset: 1, color: `${props.colors[index % props.colors.length]}80` },
            ]),
            borderRadius: [0, 4, 4, 0],
          },
          barWidth: '60%',
          label: {
            show: true,
            position: 'right',
            color: '#1d2129',
            fontWeight: 600,
            fontSize: 13,
          },
        })),
      },
    ],
  };
});
</script>

<style lang="scss" scoped>
.ops-bar-chart {
  width: 100%;
  
  .echarts-container {
    width: 100%;
    height: 100%;
  }
  
  .empty-state {
    display: flex;
    align-items: center;
    justify-content: center;
    color: #c9cdd4;
    font-size: 14px;
    height: 100%;
  }
}
</style>
