<template>
  <div class="ops-line-chart" :style="{ height: height + 'px' }">
    <v-chart v-if="chartOptions" :option="chartOptions" autoresize class="echarts-container" />
    <div v-else class="empty-state">暂无数据</div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import VChart from 'vue-echarts';
import { use } from 'echarts/core';
import { BarChart, LineChart } from 'echarts/charts';
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
  LineChart,
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
    default: 300,
  },
  type: {
    type: String,
    default: 'bar', // 'bar' | 'line' | 'mixed'
  },
  color: {
    type: String,
    default: '#165DFF',
  },
});

const chartOptions = computed(() => {
  if (!props.data || props.data.length === 0) return null;

  const categories = props.data.map(item => item.label || item.name);
  const values = props.data.map(item => item.value || item.count);

  const baseOption = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e5e6eb',
      borderWidth: 1,
      textStyle: { color: '#1d2129', fontSize: 13 },
      formatter: (params) => {
        const item = params[0];
        return `<strong>${item.axisValue}</strong><br/>${item.seriesName}: <span style="color:${props.color};font-weight:600">${item.value}</span>`;
      },
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '8%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      data: categories,
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#e5e6eb' } },
      axisLabel: { color: '#86909c', fontSize: 12 },
    },
    yAxis: {
      type: 'value',
      axisTick: { show: false },
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f2f3f5', type: 'dashed' } },
      axisLabel: { color: '#86909c', fontSize: 12 },
    },
  };

  if (props.type === 'bar') {
    return {
      ...baseOption,
      series: [{
        name: props.title || '数量',
        type: 'bar',
        data: values,
        barWidth: '50%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: props.color },
            { offset: 1, color: `${props.color}40` },
          ]),
          borderRadius: [4, 4, 0, 0],
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: props.color },
              { offset: 1, color: `${props.color}80` },
            ]),
          },
        },
      }],
    };
  }

  if (props.type === 'line') {
    return {
      ...baseOption,
      series: [{
        name: props.title || '趋势',
        type: 'line',
        data: values,
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: { width: 3, color: props.color },
        itemStyle: { color: props.color, borderColor: '#fff', borderWidth: 2 },
        areaStyle: {
          color: new (require('echarts').graphic.LinearGradient)(0, 0, 0, 1, [
            { offset: 0, color: `${props.color}30` },
            { offset: 1, color: `${props.color}05` },
          ]),
        },
      }],
    };
  }

  // mixed: 柱状 + 折线混合
  return {
    ...baseOption,
    legend: { data: ['数量', '趋势'], bottom: 0 },
    grid: { ...baseOption.grid, bottom: '12%' },
    series: [
      {
        name: '数量',
        type: 'bar',
        data: values,
        barWidth: '35%',
        itemStyle: {
          color: new (require('echarts').graphic.LinearGradient)(0, 0, 0, 1, [
            { offset: 0, color: props.color },
            { offset: 1, color: `${props.color}40` },
          ]),
          borderRadius: [4, 4, 0, 0],
        },
      },
      {
        name: '趋势',
        type: 'line',
        data: values,
        smooth: true,
        symbol: 'none',
        lineStyle: { width: 2, color: '#FF7D00', type: 'dashed' },
      },
    ],
  };
});
</script>

<style lang="scss" scoped>
.ops-line-chart {
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
