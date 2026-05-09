<template>
  <div
    class="grid-card"
    :class="{ 'grid-card--show': show }"
    :style="{
      transitionDelay: `${indexNum * 0.1}s`
    }"
    @click="$emit('click')"
  >
    <div class="grid-card__content">
      <div class="grid-card__title">{{ title }}</div>
      <div class="grid-card__text">{{ text }}</div>
      <div class="grid-card__action">
        <icon-arrow-right class="grid-card__action__icon" />
      </div>
      <div class="grid-card__tag">{{ tag }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { IconArrowRight } from '@arco-design/web-vue/es/icon'

defineProps({
  title: {
    type: String,
    default: ''
  },
  text: {
    type: String,
    default: ''
  },
  indexNum: {
    type: Number,
    default: 0
  },
  tag: {
    type: String,
    default: ''
  }
})

defineEmits(['click'])

const show = ref(false)
setTimeout(() => {
  show.value = true
}, 300)
</script>

<style lang="scss" scoped>
.grid-card {
  min-height: 242px;
  box-sizing: border-box;
  transform: scale(0);
  transition: transform 0.3s cubic-bezier(1, -0.15, 0.3, 1.44);
  padding: 30px 35px;
  background: linear-gradient(180deg, #ffffff 0%, #ecf0f6 100%);
  border-radius: 4px 4px 4px 4px;
  border: 1px solid #ccd3ea;
  border-bottom: 5px solid #bbc7e7;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;

  &--show {
    transform: scale(1);
  }

  &:hover {
    cursor: pointer;
    .grid-card__action {
      opacity: 1;
      height: 53px;
    }
  }

  &__content {
    display: flex;
    flex-direction: column;
  }

  &__action {
    position: absolute;
    width: 82px;
    bottom: 0;
    right: 35px;
    padding: 9px;
    height: 0;
    overflow: hidden;
    background: linear-gradient(135deg, #165dff 0%, #4c6ef5 100%);
    border-radius: 50%;
    transition: all 0.3s ease-in-out;
    opacity: 0;
    display: flex;
    align-items: center;
    justify-content: center;

    &__icon {
      width: 36px;
      height: 36px;
      color: #fff;
      font-size: 20px;
    }
  }

  &__tag {
    background: rgba(22, 93, 255, 0.1);
    border-radius: 4px 4px 4px 4px;
    position: absolute;
    padding: 6px 7px;
    left: 36px;
    bottom: 32px;
    font-weight: 400;
    font-size: 12px;
    color: #165dff;
    line-height: 12px;
    text-align: center;
  }

  &__title {
    font-weight: 500;
    font-size: 24px;
    color: #252933;
    line-height: 30px;
    text-align: justify;
    line-clamp: 2;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    margin-bottom: 7px;
    overflow: hidden;
  }

  &__text {
    font-weight: 400;
    font-size: 16px;
    color: #5d5d5d;
    line-height: 24px;
    text-align: justify;
    line-clamp: 2;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    margin-bottom: 39px;
    overflow: hidden;
  }
}
</style>
