<template>
  <div class="ops-donut-chart" :style="{ height: height + 'px' }">
    <div class="chart-wrapper">
      <v-chart v-if="chartOptions" :option="chartOptions" autoresize class="echarts-container" />
      <div v-else class="empty-state">暂无数据</div>
    </div>
    <div v-if="showLegend && data.length > 0" class="legend-list">
      <div 
        v-for="(item, index) in data" 
        :key="index"
        class="legend-item"
      >
        <span class="legend-dot" :style="{ background: colors[index % colors.length] }"></span>
        <span class="legend-name">{{ item.name }}</span>
        <span class="legend-count">{{ item.count }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import VChart from 'vue-echarts';
import { use } from 'echarts/core';
import { PieChart } from 'echarts/charts';
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

use([
  PieChart,
  TitleComponent,
  TooltipComponent,
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
    default: 240,
  },
  showLegend: {
    type: Boolean,
    default: true,
  },
  centerText: {
    type: String,
    default: '',
  },
  colors: {
    type: Array,
    default: () => ['#165DFF', '#00B42A', '#FF7D00', '#86909C', '#F53F3F'],
  },
});

const total = computed(() => {
  return props.data.reduce((sum, item) => sum + (item.count || item.value || 0), 0);
});

const chartOptions = computed(() => {
  if (!props.data || props.data.length === 0) return null;

  return {
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e5e6eb',
      borderWidth: 1,
      textStyle: { color: '#1d2129', fontSize: 13 },
      formatter: (params) => {
        const percent = ((params.value / total.value) * 100).toFixed(1);
        return `<strong>${params.name}</strong><br/>数量: <span style="color:${params.color};font-weight:600">${params.value}</span> (${percent}%)`;
      },
    },
    legend: {
      show: false,
    },
    series: [
      {
        name: props.title || '状态分布',
        type: 'pie',
        radius: ['55%', '75%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 3,
        },
        label: {
          show: !!props.centerText,
          position: 'center',
          formatter: () => {
            if (props.centerText) {
              return `{total|${props.centerText}}\n{value|${total.value}}`;
            }
            return '';
          },
          rich: {
            total: {
              fontSize: 12,
              color: '#86909c',
              lineHeight: 20,
            },
            value: {
              fontSize: 24,
              fontWeight: 700,
              color: '#1d2129',
              lineHeight: 28,
            },
          },
        },
        emphasis: {
          label: { show: false },
          scale: true,
          scaleSize: 8,
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.2)',
          },
        },
        data: props.data.map((item, index) => ({
          value: item.count || item.value || 0,
          name: item.name,
          itemStyle: {
            color: props.colors[index % props.colors.length],
          },
        })),
        animationType: 'scale',
        animationEasing: 'elasticOut',
        animationDelay: (idx) => idx * 100,
      },
    ],
  };
});
</script>

<style lang="scss" scoped>
.ops-donut-chart {
  width: 100%;
  display: flex;
  flex-direction: column;
  
  .chart-wrapper {
    flex: 1;
    min-height: 200px;
    
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
  
  .legend-list {
    margin-top: 16px;
    padding-top: 12px;
    border-top: 1px solid #f2f3f5;
    
    .legend-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 5px 0;
      
      &:not(:last-child) {
        border-bottom: 1px solid #fafafa;
      }
      
      .legend-dot {
        width: 10px;
        height: 10px;
        border-radius: 50%;
        flex-shrink: 0;
      }
      
      .legend-name {
        flex: 1;
        font-size: 13px;
        color: #4e5969;
      }
      
      .legend-count {
        font-size: 13px;
        font-weight: 600;
        color: #1d2129;
      }
    }
  }
}
</style>
