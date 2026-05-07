<template>
  <div class="home-page">
    <section class="hero-section">
      <div class="hero-background">
        <div class="hero-gradient"></div>
        <div class="hero-pattern"></div>
      </div>

      <div class="hero-content">
        <h1 class="hero-title">校园二手交易平台</h1>
        <p class="hero-subtitle">闲置不浪费，校园淘好物</p>
        

      </div>
    </section>

    <section class="hot-section">
      <div class="section-header">
        <h2 class="section-title">
          <span class="title-icon">🔥</span>
          热门推荐
        </h2>
        <a-button type="text" class="view-more" @click="$router.push('/portal/buyer/items')">
          查看更多
          <icon-right />
        </a-button>
      </div>
      <div v-if="loadingHot" class="hot-grid">
        <a-skeleton :rows="3" animated v-for="i in 4" :key="'skeleton-' + i" />
      </div>
      <div v-else-if="hotItems.length > 0" class="hot-grid">
        <ItemCard
          v-for="item in hotItems"
          :key="item.id"
          :item="formatItem(item)"
          class="hot-item"
          @click="$router.push(`/portal/item/${item.id}`)"
        />
      </div>

      <div v-if="!loadingHot && hotItems.length === 0" class="empty-state">
        <a-empty description="暂无热门商品">
          <template #image>
            <icon-search size="48" />
          </template>
        </a-empty>
      </div>
    </section>

    <section class="quick-categories">
      <div class="category-header">
        <h3 class="category-title">分类浏览</h3>
      </div>
      <div class="category-grid">
        <div
          v-for="cat in quickCategories"
          :key="cat.value"
          class="category-card"
          @click="$router.push(`/portal/buyer/items?category=${cat.value}`)"
        >
          <span class="category-card__icon">{{ cat.icon }}</span>
          <span class="category-card__label">{{ cat.label }}</span>
        </div>
      </div>
    </section>

    <a-back-top :visible-height="300" />
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { IconRight } from "@arco-design/web-vue/es/icon";
import { getItems } from "../../../services/api";
import ItemCard from "../../../components/data/ItemCard/ItemCard.vue";

const router = useRouter();

const hotItems = ref([]);
const loadingHot = ref(false);

const quickCategories = [
  { value: 'ELECTRONICS', label: '电子产品', icon: '📱' },
  { value: 'BOOKS', label: '图书教材', icon: '📚' },
  { value: 'CLOTHING', label: '服饰鞋包', icon: '👔' },
  { value: 'DAILY', label: '生活用品', icon: '🏠' },
  { value: 'SPORTS', label: '运动器材', icon: '⚽' },
  { value: 'BEAUTY', label: '美妆护肤', icon: '💄' },
  { value: 'FOOD', label: '食品零食', icon: '🍪' },
  { value: 'OTHER', label: '其他物品', icon: '🎁' },
];

function formatItem(item) {
  return {
    id: item.id,
    title: item.title,
    price: item.price,
    imageUrls: item.imageUrls || (item.image ? [item.image] : []),
    sellerName: item.sellerName,
    category: item.category,
    conditionLevel: item.conditionLevel || item.condition,
    campus: item.campus,
  };
}

async function loadHotItems() {
  loadingHot.value = true;
  try {
    const res = await getItems({
      sort: 'latest',
      pageSize: 8,
      approvedOnly: true
    });
    const items = res?.data?.items || res?.items || res?.rows || [];
    hotItems.value = Array.isArray(items) ? [...items] : [];
  } catch (e) {
    console.error('[Home] 加载热门推荐失败:', e);
  } finally {
    loadingHot.value = false;
  }
}

onMounted(() => {
  loadHotItems();
});
</script>

<style lang="scss" scoped>
$primary-blue: #165DFF;
$primary-blue-light: #4080FF;
$bg-white: #FFFFFF;
$bg-gray: #F5F7FA;
$text-primary: #1D2129;
$text-secondary: #4E5969;

.home-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding-bottom: 40px;
  background: transparent;
}

.hero-section {
  position: relative;
  width: 100vw;
  min-height: 560px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  margin: -24px calc(-50vw + 50%) 0 calc(-50vw + 50%);
}

.hero-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

.hero-gradient {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, 
    $primary-blue 0%, 
    $primary-blue-light 35%,
    #4A90FF 65%, 
    #E8F3FF 100%
  );
}

.hero-pattern {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0.08;
  background-image: 
    radial-gradient(circle at 20% 50%, rgba(255,255,255,0.15) 0%, transparent 50%),
    radial-gradient(circle at 80% 30%, rgba(255,255,255,0.1) 0%, transparent 40%);
  background-size: 100% 100%;
}

.hero-content {
  position: relative;
  z-index: 1;
  text-align: center;
  max-width: 900px;
  padding: 60px 24px;
  animation: fadeInUp 0.8s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.hero-title {
  margin: 0 0 16px;
  font-size: 56px;
  font-weight: 800;
  color: $bg-white;
  letter-spacing: -0.02em;
  line-height: 1.2;
  text-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.hero-subtitle {
  margin: 0;
  font-size: 22px;
  color: rgba(255, 255, 255, 0.95);
  font-weight: 400;
  letter-spacing: 0.01em;
}

.hot-section {
  background: $bg-white;
  border-radius: 16px;
  padding: 28px 32px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.section-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: $text-primary;
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-icon {
  font-size: 24px;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-4px); }
}

.view-more {
  font-size: 14px;
  font-weight: 500;
  color: $primary-blue;
  display: flex;
  align-items: center;
  gap: 4px;
}

.hot-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.empty-state {
  padding: 40px 0;
  text-align: center;
}

.quick-categories {
  background: $bg-white;
  border-radius: 16px;
  padding: 28px 32px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
}

.category-header {
  margin-bottom: 20px;
}

.category-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: $text-primary;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 16px;
}

.category-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 20px 12px;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: $bg-gray;
  border: 2px solid transparent;
  
  &:hover {
    transform: translateY(-6px);
    background: #E8F3FF;
    box-shadow: 0 8px 24px rgba(22, 93, 255, 0.12);
    border-color: rgba(22, 93, 255, 0.2);
  }

  &__icon {
    font-size: 38px;
  }

  &__label {
    font-size: 14px;
    font-weight: 500;
    color: $text-primary;
    white-space: nowrap;
  }
}

@media (max-width: 1279px) {
  .hot-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  .category-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 1023px) {
  .hero-title {
    font-size: 44px;
  }
  
  .hero-subtitle {
    font-size: 19px;
  }
  
  .hot-section {
    padding: 20px 24px;
  }
  
  .hot-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
  }
}

@media (max-width: 767px) {
  .hero-section {
    min-height: 320px;
  }
  
  .hero-title {
    font-size: 34px;
  }
  
  .hero-subtitle {
    font-size: 17px;
  }
  
  .hero-content {
    padding: 40px 20px;
  }
  
  .hot-section {
    padding: 16px;
    border-radius: 12px;
  }
  
  .hot-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .section-title {
    font-size: 18px;
  }
  
  .quick-categories {
    padding: 20px 16px;
  }
  
  .category-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
  }
  
  .category-card {
    padding: 16px 8px;
    
    &__icon {
      font-size: 28px;
    }
    
    &__label {
      font-size: 12px;
    }
  }
}
</style>