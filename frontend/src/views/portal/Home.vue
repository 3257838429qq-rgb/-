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
              <img src="/418.webp" alt="room" />
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ArrowRight, House, Checked, Service, User } from '@element-plus/icons-vue'
import { getRoomTypeList } from '@/api/user'

const roomTypes = ref([])
const loading = ref(false)

function parseFacility(f) {
  if (!f) return []
  try { return JSON.parse(f) } catch { return f.split(',').map(s => s.trim()) }
}

function scrollToRooms() {
  document.getElementById('room-section')?.scrollIntoView({ behavior: 'smooth' })
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getRoomTypeList()
    if (res.code === 200) roomTypes.value = res.data || []
  } catch (e) { console.error(e) }
  finally { loading.value = false }
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

// Hero
.hero {
  background: linear-gradient(135deg, #1a3a5c 0%, #2d6a9f 50%, #4a9fd4 100%);
  border-radius: 16px;
  padding: 80px 60px;
  text-align: center;
  color: #fff;
  margin-bottom: 48px;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: url('/418.webp') center / cover no-repeat;
    opacity: 0.05;
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
    opacity: 0.7;
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
</style>
