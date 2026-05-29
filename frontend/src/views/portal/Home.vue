<!--
  用户端首页组件

  【模块说明】
  - 用户端首页，展示招待所概览和快速入口
  - 包含Banner、特点介绍、房型展示、住客评价

  【功能模块】
  1. Hero Banner：招待所宣传图
  2. 特点介绍：舒适客房、便捷预订、贴心服务
  3. 精选房型：展示房型列表
  4. 住客评价：最新评价展示

  【API调用】
  - getRoomTypeList: 获取房型列表
  - getRecentReviews: 获取最新评价

  【后端对应】
  - Controller: DormRoomTypeController
  - 路径: /dorm/room-type/list
  - Controller: ReviewController
  - 路径: /review/recent

  【路由对应】
  - /portal/home
-->
<template>
  <div class="portal-home">
    <!-- Hero banner -->
    <section class="hero">
      <div class="hero-content">
        <h1>欢迎来到东北大学校招待所</h1>
        <p>为来访学者、家长及宾客提供舒适便捷的住宿体验</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" round @click="$router.push('/portal/rooms')">
            立即预订 <el-icon><ArrowRight /></el-icon>
          </el-button>
          <el-button size="large" round @click="scrollToRooms">查看房型</el-button>
        </div>
      </div>
    </section>

    <!-- Features -->
    <section class="features">
      <el-row :gutter="24">
        <el-col :md="8" :sm="12" :span="24">
          <div class="feature-card">
            <div class="feature-icon"><el-icon :size="32"><House /></el-icon></div>
            <h3>舒适客房</h3>
            <p>多种房型可选，干净整洁、设施齐全</p>
          </div>
        </el-col>
        <el-col :md="8" :sm="12" :span="24">
          <div class="feature-card">
            <div class="feature-icon"><el-icon :size="32"><Checked /></el-icon></div>
            <h3>便捷预订</h3>
            <p>在线浏览房型，一键提交入住申请</p>
          </div>
        </el-col>
        <el-col :md="8" :sm="12" :span="24">
          <div class="feature-card">
            <div class="feature-icon"><el-icon :size="32"><Service /></el-icon></div>
            <h3>贴心服务</h3>
            <p>24小时前台服务，为您的住宿保驾护航</p>
          </div>
        </el-col>
      </el-row>
    </section>

    <!-- Room types section -->
    <section id="room-section" class="room-section">
      <div class="section-header">
        <h2>精选房型</h2>
        <p>多种房型满足不同需求</p>
      </div>
      <el-row :gutter="20" v-loading="loading">
        <el-col v-for="room in roomTypes" :key="room.id" :md="6" :sm="12" :span="24">
          <div class="room-card" @click="$router.push('/portal/rooms')">
            <div class="room-card-img">
              <img :src="getRoomImage(room.name)" alt="room" />
              <div class="room-price">
                <span class="price-num">¥{{ room.basePrice }}</span>
                <span class="price-unit">/晚</span>
              </div>
            </div>
            <div class="room-card-body">
              <h4>{{ room.name }}</h4>
              <p>{{ room.description }}</p>
              <div class="room-tags">
                <el-tag size="small" v-for="f in parseFacility(room.facility)" :key="f" class="room-tag">
                  {{ f }}
                </el-tag>
              </div>
              <div class="room-meta">
                <span><el-icon><User /></el-icon> {{ room.bedCount }}床</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
      <div class="section-action" v-if="roomTypes.length > 0">
        <el-button type="primary" size="large" @click="$router.push('/portal/rooms')">
          查看全部房型 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
    </section>

    <!-- Reviews section -->
    <section v-if="reviews.length > 0" class="review-section">
      <div class="section-header">
        <h2>住客评价</h2>
        <p>来自真实住客的入住体验分享</p>
      </div>
      <el-row :gutter="20" v-loading="reviewLoading">
        <el-col v-for="r in reviews" :key="r.id" :md="8" :sm="12" :span="24">
          <div class="review-card">
            <div class="review-top">
              <el-rate v-model="r.rating" disabled size="small" />
              <span class="review-room">{{ r.roomNo }}</span>
            </div>
            <p class="review-body">{{ r.content }}</p>
            <div class="review-footer">
              <span class="review-user">{{ r.userName }}</span>
              <span class="review-time">{{ formatReviewTime(r.createTime) }}</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ArrowRight, House, Checked, Service, User } from '@element-plus/icons-vue'
import { getRoomTypeList } from '@/api/user'
import { getRecentReviews } from '@/api/review'
import dayjs from 'dayjs'

const roomTypes = ref([])
const loading = ref(false)
const reviews = ref([])
const reviewLoading = ref(false)

function parseFacility(f) {
  if (!f) return []
  try { return JSON.parse(f) } catch { return f.split(',').map(s => s.trim()) }
}

function getRoomImage(name) {
  if (!name) return '/418.webp'
  const n = name.toLowerCase()
  if (n.includes('单')) return '/images/单人间.png'
  if (n.includes('标准')) return '/images/标准间.png'
  if (n.includes('商务')) return '/images/商务房.png'
  if (n.includes('套')) return '/images/套房.png'
  return '/418.webp'
}

function formatReviewTime(d) { return d ? dayjs(d).format('YYYY-MM-DD') : '' }

function scrollToRooms() {
  document.getElementById('room-section')?.scrollIntoView({ behavior: 'smooth' })
}

async function fetchReviews() {
  reviewLoading.value = true
  try {
    const res = await getRecentReviews(6)
    if (res.code === 200) reviews.value = res.data || []
  } catch (e) { console.error(e) }
  finally { reviewLoading.value = false }
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getRoomTypeList()
    if (res.code === 200) roomTypes.value = res.data || []
  } catch (e) { console.error(e) }
  finally { loading.value = false }
  fetchReviews()
})
</script>

<style lang="scss" scoped>
.portal-home {
  animation: fadeIn 0.4s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes heroSlide {
  0% { opacity: 0; }
  5% { opacity: 1; }
  30% { opacity: 1; }
  35% { opacity: 0; }
  100% { opacity: 0; }
}

// Hero
.hero {
  background: linear-gradient(135deg, rgba(26, 58, 92, 0.85) 0%, rgba(45, 106, 159, 0.75) 50%, rgba(74, 159, 212, 0.7) 100%),
              url('/images/dongda1.png') center/cover no-repeat;
  border-radius: 16px;
  padding: 80px 60px;
  text-align: center;
  color: #fff;
  margin-bottom: 48px;
  position: relative;
  overflow: hidden;
  min-height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: url('/images/dongda2.jpg') center/cover no-repeat;
    opacity: 0;
    animation: heroSlide 18s infinite;
  }

  .hero-content {
    position: relative;
    z-index: 1;

    h1 {
      font-size: 36px;
      font-weight: 700;
      margin-bottom: 12px;
      letter-spacing: 1px;
    }

    p {
      font-size: 16px;
      opacity: 0.85;
      margin-bottom: 32px;
    }
  }

  .hero-actions {
    display: flex;
    justify-content: center;
    gap: 16px;
    flex-wrap: wrap;
  }
}

// Features
.features {
  margin-bottom: 48px;

  .feature-card {
    text-align: center;
    padding: 32px 20px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
    transition: all 0.3s;
    margin-bottom: 16px;

    &:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08); }

    .feature-icon {
      width: 64px;
      height: 64px;
      border-radius: 16px;
      background: #ecf5ff;
      color: #409eff;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 16px;
    }

    h3 { font-size: 18px; font-weight: 600; color: #303133; margin-bottom: 8px; }
    p { font-size: 14px; color: #909399; }
  }
}

// Room section
.room-section {
  margin-bottom: 32px;

  .section-header {
    text-align: center;
    margin-bottom: 32px;

    h2 { font-size: 28px; font-weight: 700; color: #303133; margin-bottom: 8px; }
    p { font-size: 15px; color: #909399; }
  }

  .section-action {
    text-align: center;
    margin-top: 28px;
  }
}

.room-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 20px;

  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 12px 28px rgba(0, 0, 0, 0.1);
  }
}

.room-card-img {
  height: 180px;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #e4e7ed, #dcdfe6);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .room-price {
    position: absolute;
    bottom: 12px;
    right: 12px;
    background: rgba(0, 0, 0, 0.65);
    color: #fff;
    padding: 6px 14px;
    border-radius: 20px;

    .price-num { font-size: 20px; font-weight: 700; }
    .price-unit { font-size: 12px; opacity: 0.8; }
  }
}

.room-card-body {
  padding: 16px;

  h4 { font-size: 16px; font-weight: 600; color: #303133; margin-bottom: 6px; }
  p { font-size: 13px; color: #909399; margin-bottom: 10px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

  .room-tags {
    display: flex; flex-wrap: wrap; gap: 4px; margin-bottom: 10px;
  }

  .room-meta {
    font-size: 13px; color: #606266;
    display: flex; align-items: center; gap: 16px;
  }
}

// Review section
.review-section {
  margin-bottom: 32px;

  .section-header {
    text-align: center;
    margin-bottom: 32px;

    h2 { font-size: 28px; font-weight: 700; color: #303133; margin-bottom: 8px; }
    p { font-size: 15px; color: #909399; }
  }
}

.review-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  margin-bottom: 20px;
  transition: all 0.3s;

  &:hover { box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08); }

  .review-top {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;

    .review-room {
      font-size: 12px;
      color: #409eff;
      background: #ecf5ff;
      padding: 2px 10px;
      border-radius: 10px;
      font-weight: 500;
    }
  }

  .review-body {
    font-size: 14px;
    color: #606266;
    line-height: 1.6;
    margin-bottom: 14px;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .review-footer {
    display: flex;
    justify-content: space-between;
    font-size: 12px;
    color: #c0c4cc;
  }
}
</style>
