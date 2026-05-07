<template>
  <footer class="campus-footer">
    <div class="footer-container">
      <!-- 主要内容区域 -->
      <div class="footer-main">
        <!-- 关于我们 -->
        <div class="footer-column">
          <h3 class="column-title">关于我们</h3>
          <p class="column-desc">校园二手市场是一个专注于校园内的二手交易平台，为大学生提供安全、便捷的二手物品交易服务。</p>
          <div class="social-links">
            <a href="#" class="social-link" aria-label="微信">
              <icon-wechat />
            </a>
            <a href="#" class="social-link" aria-label="QQ">
              <icon-qq />
            </a>
            <a href="#" class="social-link" aria-label="微博">
              <icon-weibo />
            </a>
          </div>
        </div>

        <!-- 快速链接 -->
        <div class="footer-column">
          <h3 class="column-title">快速链接</h3>
          <ul class="link-list">
            <li><router-link to="/portal/home">首页</router-link></li>
            <li><router-link to="/portal/buyer/items">全部商品</router-link></li>
            <li><router-link to="/portal/seller/publish">发布商品</router-link></li>
            <li><router-link to="/portal/orders">我的订单</router-link></li>
            <li><router-link to="/portal/favorites">我的收藏</router-link></li>
            <li><router-link to="/about">关于我们</router-link></li>
          </ul>
        </div>

        <!-- 帮助支持 -->
        <div class="footer-column">
          <h3 class="column-title">帮助支持</h3>
          <ul class="link-list">
            <li><a href="#">新手指南</a></li>
            <li><a href="#">交易规则</a></li>
            <li><a href="#">常见问题</a></li>
            <li><a href="#">安全保障</a></li>
            <li><a href="#">联系客服</a></li>
          </ul>
        </div>

        <!-- 联系方式 -->
        <div class="footer-column">
          <h3 class="column-title">联系方式</h3>
          <ul class="contact-list">
            <li class="contact-item">
              <icon-phone />
              <span>400-123-4567</span>
            </li>
            <li class="contact-item">
              <icon-email />
              <span>support@campus-market.com</span>
            </li>
            <li class="contact-item">
              <icon-location />
              <span>某某大学校园服务中心</span>
            </li>
            <li class="contact-item">
              <icon-clock-circle />
              <span>周一至周日 9:00-21:00</span>
            </li>
          </ul>
        </div>
      </div>

      <!-- 底部版权信息 -->
      <div class="footer-bottom">
        <p class="copyright">© 2025 校园二手市场. All Rights Reserved.</p>
        <div class="legal-links">
          <a href="#">用户协议</a>
          <span class="divider">|</span>
          <a href="#">隐私政策</a>
          <span class="divider">|</span>
          <a href="#">网站地图</a>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
import {
  IconWechat,
  IconQq,
  IconWeibo,
  IconPhone,
  IconEmail,
  IconLocation,
  IconClockCircle
} from '@arco-design/web-vue/es/icon'
</script>

<style lang="scss" scoped>
.campus-footer {
  background-color: var(--gray-50, #F7F8FA);
  border-top: 1px solid var(--gray-200, #E5E6EB);
  margin-top: auto;
}

.footer-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 48px 24px 24px;
}

.footer-main {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 40px;
  margin-bottom: 40px;
}

.footer-column {
  .column-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--gray-600, #1D2129);
    margin-bottom: 20px;
  }

  .column-desc {
    font-size: 14px;
    line-height: 1.7;
    color: var(--gray-400, #86909C);
    margin-bottom: 20px;
  }

  .social-links {
    display: flex;
    gap: 12px;

    .social-link {
      width: 36px;
      height: 36px;
      border-radius: 8px;
      background-color: var(--bg-card, #FFFFFF);
      display: flex;
      align-items: center;
      justify-content: center;
      color: var(--gray-400, #86909C);
      transition: all 0.2s ease;
      border: 1px solid var(--gray-100, #F2F3F5);

      &:hover {
        color: var(--primary-500, #165DFF);
        border-color: var(--primary-500, #165DFF);
        transform: translateY(-2px);
      }
    }
  }

  .link-list,
  .contact-list {
    list-style: none;
    padding: 0;
    margin: 0;

    li {
      margin-bottom: 12px;

      &:last-child {
        margin-bottom: 0;
      }
    }

    a {
      font-size: 14px;
      color: var(--gray-500, #4E5969);
      text-decoration: none;
      transition: color 0.2s ease;

      &:hover {
        color: var(--primary-500, #165DFF);
      }
    }
  }

  .contact-list {
    .contact-item {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 14px;
      color: var(--gray-500, #4E5969);

      :deep(.arco-icon) {
        font-size: 16px;
        color: var(--primary-500, #165DFF);
        flex-shrink: 0;
      }
    }
  }
}

.footer-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 24px;
  border-top: 1px solid var(--gray-100, #F2F3F5);

  .copyright {
    font-size: 13px;
    color: var(--gray-400, #86909C);
  }

  .legal-links {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 13px;

    a {
      color: var(--gray-400, #86909C);
      text-decoration: none;
      transition: color 0.2s ease;

      &:hover {
        color: var(--primary-500, #165DFF);
      }
    }

    .divider {
      color: var(--gray-300, #C9CDD4);
    }
  }
}

@media (max-width: 1023px) {
  .footer-main {
    grid-template-columns: repeat(2, 1fr);
    gap: 32px;
  }
}

@media (max-width: 767px) {
  .footer-container {
    padding: 32px 16px 20px;
  }

  .footer-main {
    grid-template-columns: 1fr;
    gap: 28px;
    margin-bottom: 24px;
  }

  .footer-bottom {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
}
</style>
