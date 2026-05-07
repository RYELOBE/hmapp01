<template>
  <div class="about-us">
    <section class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">关于校园二手市场</h1>
        <p class="hero-subtitle">让闲置流动起来，让校园更美好</p>
      </div>
    </section>

    <section class="content-section">
      <div class="container">
        <div class="mission-block">
          <h2 class="section-title">我们的使命</h2>
          <p class="section-text">
            校园二手市场致力于为高校师生提供一个便捷、安全、可信赖的二手物品交易平台。我们相信每一件闲置物品都值得被重新利用，每一次交易都能创造价值。
          </p>
        </div>

        <div class="values-block">
          <h2 class="section-title">核心价值观</h2>
          <div class="values-grid">
            <div class="value-card">
              <div class="value-emoji">🤝</div>
              <h3 class="value-title">诚信交易</h3>
              <p class="value-desc">实名认证体系，保障交易安全</p>
            </div>
            <div class="value-card">
              <div class="value-emoji">♻️</div>
              <h3 class="value-title">绿色环保</h3>
              <p class="value-desc">减少浪费，倡导可持续生活方式</p>
            </div>
            <div class="value-card">
              <div class="value-emoji">💰</div>
              <h3 class="value-title">经济实惠</h3>
              <p class="value-desc">高性价比商品，减轻学生经济负担</p>
            </div>
            <div class="value-card">
              <div class="value-emoji">🎓</div>
              <h3 class="value-title">服务师生</h3>
              <p class="value-desc">专注高校市场，理解学生需求</p>
            </div>
          </div>
        </div>

        <div class="team-block">
          <h2 class="section-title">核心团队</h2>
          <div class="team-grid">
            <div class="team-member">
              <div class="avatar">👨‍💻</div>
              <h4 class="member-name">产品负责人</h4>
              <p class="member-role">负责产品规划和用户体验设计</p>
            </div>
            <div class="team-member">
              <div class="avatar">👩‍💻</div>
              <h4 class="member-name">技术负责人</h4>
              <p class="member-role">负责系统架构和技术实现</p>
            </div>
            <div class="team-member">
              <div class="avatar">👨‍🎨</div>
              <h4 class="member-name">设计师</h4>
              <p class="member-role">负责界面设计和视觉呈现</p>
            </div>
            <div class="team-member">
              <div class="avatar">👩‍💼</div>
              <h4 class="member-name">运营负责人</h4>
              <p class="member-role">负责平台运营和社区建设</p>
            </div>
          </div>
        </div>

        <div class="timeline-block">
          <h2 class="section-title">发展历程</h2>
          <a-timeline mode="left">
            <a-timeline-item>2025.Q1 - 项目启动，完成需求调研</a-timeline-item>
            <a-timeline-item>2025.Q2 - MVP版本上线，首批种子用户</a-timeline-item>
            <a-timeline-item>2025.Q3 - 功能完善，用户突破1000+</a-timeline-item>
            <a-timeline-item>2025.Q4 - 全面升级，打造全新体验</a-timeline-item>
          </a-timeline>
        </div>

        <div class="contact-block">
          <h2 class="section-title">联系我们</h2>
          <div class="contact-grid">
            <div class="contact-item">
              <icon-email />
              <span>邮箱：support@campus-market.com</span>
            </div>
            <div class="contact-item">
              <icon-phone />
              <span>电话：400-123-4567</span>
            </div>
            <div class="contact-item">
              <icon-location />
              <span>地址：XX省XX市XX大学科技园</span>
            </div>
            <div class="contact-item">
              <icon-wechat />
              <span>微信公众号：校园二手市场</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="cta-section">
      <div class="cta-content">
        <h2 class="cta-title">加入我们</h2>
        <p class="cta-text">立即注册，开启你的校园二手交易之旅</p>
        <a-button type="primary" size="large" @click="$router.push('/portal/home')">
          立即注册
        </a-button>
      </div>
    </section>
  </div>
</template>

<script setup>
import {
  IconEmail,
  IconPhone,
  IconLocation,
  IconWechat
} from '@arco-design/web-vue/es/icon'
</script>

<style lang="scss" scoped>
.about-us {
  min-height: 100vh;
  background: #ffffff;
}

.hero-section {
  height: 400px;
  background: linear-gradient(135deg, #165DFF 0%, #092DC2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: #FFFFFF;
}

.hero-content {
  max-width: 800px;
  padding: 0 24px;
}

.hero-title {
  font-size: 42px;
  font-weight: 800;
  margin: 0 0 16px 0;
  line-height: 1.2;
}

.hero-subtitle {
  font-size: 20px;
  font-weight: 400;
  margin: 0;
  opacity: 0.9;
}

.content-section {
  padding: 80px 24px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

.section-title {
  font-size: 32px;
  font-weight: 700;
  color: #1d2129;
  text-align: center;
  margin-bottom: 40px;
  position: relative;

  &::after {
    content: '';
    position: absolute;
    bottom: -12px;
    left: 50%;
    transform: translateX(-50%);
    width: 60px;
    height: 4px;
    background: #165dff;
    border-radius: 2px;
  }
}

.section-text {
  font-size: 16px;
  line-height: 1.8;
  color: #4e5969;
  text-align: center;
  max-width: 800px;
  margin: 0 auto 60px;
}

.mission-block,
.values-block,
.team-block,
.timeline-block,
.contact-block {
  margin-bottom: 80px;

  &:last-child {
    margin-bottom: 0;
  }
}

.values-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.value-card {
  background: #f7f8fa;
  border-radius: 12px;
  padding: 32px 24px;
  text-align: center;
  transition: all 0.25s ease;
  border: 1px solid transparent;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(22, 93, 255, 0.12);
    border-color: #165dff40;
    background: #ffffff;
  }

  .value-emoji {
    font-size: 48px;
    margin-bottom: 16px;
  }

  .value-title {
    font-size: 18px;
    font-weight: 600;
    color: #1d2129;
    margin: 0 0 8px 0;
  }

  .value-desc {
    font-size: 14px;
    color: #86909c;
    margin: 0;
    line-height: 1.6;
  }
}

.team-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.team-member {
  text-align: center;
  padding: 24px;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #f2f3f5;
  transition: all 0.25s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  }

  .avatar {
    width: 80px;
    height: 80px;
    margin: 0 auto 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 48px;
    background: #f7f8fa;
    border-radius: 50%;
  }

  .member-name {
    font-size: 16px;
    font-weight: 600;
    color: #1d2129;
    margin: 0 0 8px 0;
  }

  .member-role {
    font-size: 13px;
    color: #86909c;
    margin: 0;
    line-height: 1.5;
  }
}

.timeline-block {
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;

  :deep(.arco-timeline) {
    padding-left: 20px;
  }

  :deep(.arco-timeline-item-content-wrapper) {
    font-size: 15px;
    color: #4e5969;
    font-weight: 500;
  }
}

.contact-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 24px;
  background: #f7f8fa;
  border-radius: 10px;
  font-size: 15px;
  color: #4e5969;
  transition: all 0.2s ease;

  &:hover {
    background: #E6F1FF;
    color: #165DFF;
  }

  :deep(.arco-icon) {
    font-size: 24px;
    color: #165DFF;
    flex-shrink: 0;
  }
}

.cta-section {
  background: linear-gradient(135deg, #F7F8FA 0%, #EDF1F7 100%);
  padding: 80px 24px;
  text-align: center;
}

.cta-content {
  max-width: 600px;
  margin: 0 auto;
}

.cta-title {
  font-size: 36px;
  font-weight: 700;
  color: #1d2129;
  margin: 0 0 16px 0;
}

.cta-text {
  font-size: 18px;
  color: #4e5969;
  margin: 0 0 32px 0;
}

@media screen and (max-width: 1023px) {
  .values-grid,
  .team-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .hero-title {
    font-size: 36px;
  }

  .hero-subtitle {
    font-size: 18px;
  }

  .section-title {
    font-size: 28px;
  }
}

@media screen and (max-width: 767px) {
  .hero-section {
    height: 320px;
  }

  .hero-title {
    font-size: 28px;
  }

  .hero-subtitle {
    font-size: 16px;
  }

  .content-section {
    padding: 60px 16px;
  }

  .mission-block,
  .values-block,
  .team-block,
  .timeline-block,
  .contact-block {
    margin-bottom: 60px;
  }

  .section-title {
    font-size: 24px;
    margin-bottom: 32px;
  }

  .values-grid,
  .team-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .contact-grid {
    grid-template-columns: 1fr;
  }

  .cta-section {
    padding: 60px 16px;
  }

  .cta-title {
    font-size: 28px;
  }

  .cta-text {
    font-size: 16px;
  }
}
</style>
