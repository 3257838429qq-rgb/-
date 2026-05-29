<!--
  智能客服聊天组件

  【模块说明】
  - 右下角悬浮的智能客服聊天窗口
  - 基于DeepSeek AI大模型进行智能问答
  - 支持关键词匹配降级方案

  【功能流程】
  1. 点击悬浮按钮打开/关闭聊天窗口
  2. 输入问题 -> 发送 -> 显示用户消息 -> AI回复
  3. 支持打字机动画效果

  【API调用】
  - askQuestion: 发送问题获取AI回答

  【后端对应】
  - Controller: CustomerServiceController
  - 路径前缀: /cs

  【显示位置】
  - 固定在页面右下角
  - 位于UserLayout中引入
-->
<template>
  <div class="chat-widget">
    <transition name="fade">
      <div v-if="visible" class="chat-panel">
        <div class="chat-header">
          <div class="chat-header-left">
            <el-icon><ChatDotRound /></el-icon>
            <span>智能客服小东</span>
            <span class="chat-badge">DeepSeek V4 Pro</span>
          </div>
          <el-icon class="chat-close" @click="visible = false"><Close /></el-icon>
        </div>
        <div class="chat-body" ref="chatBodyRef">
          <div v-for="(msg, i) in messages" :key="i" class="chat-msg" :class="msg.role">
            <div class="msg-avatar" v-if="msg.role === 'assistant'">
              <el-icon><Service /></el-icon>
            </div>
            <div class="msg-bubble" v-html="msg.content"></div>
          </div>
          <div v-if="typing" class="chat-msg assistant">
            <div class="msg-avatar"><el-icon><Service /></el-icon></div>
            <div class="msg-bubble typing"><span></span><span></span><span></span></div>
          </div>
        </div>
        <div class="chat-footer">
          <el-input
            v-model="input"
            placeholder="输入您的问题..."
            @keyup.enter="sendMsg"
            :disabled="typing"
          >
            <template #append>
              <el-button :icon="Promotion" @click="sendMsg" :disabled="typing" />
            </template>
          </el-input>
        </div>
      </div>
    </transition>
    <div class="chat-fab" @click="toggle" :class="{ active: visible }">
      <el-icon :size="24">
        <ChatDotRound v-if="!visible" />
        <Close v-else />
      </el-icon>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import { ChatDotRound, Close, Service, Promotion } from '@element-plus/icons-vue'
import { askQuestion } from '@/api/customer'

const visible = ref(false)
const input = ref('')
const typing = ref(false)
const messages = ref([])
const chatBodyRef = ref()

function toggle() {
  visible.value = !visible.value
  if (visible.value && messages.value.length === 0) {
    messages.value.push({ role: 'assistant', content: '您好！我是招待所的智能客服小东，有什么可以帮助您的？<br>您可以问我关于<strong>预订、价格、VIP、支付、退房、设施</strong>等方面的问题。' })
  }
}

async function sendMsg() {
  const q = input.value.trim()
  if (!q || typing.value) return
  input.value = ''
  messages.value.push({ role: 'user', content: q })
  typing.value = true
  await scrollBottom()
  try {
    const res = await askQuestion(q)
    if (res.code === 200) {
      messages.value.push({ role: 'assistant', content: (res.data || '').replace(/\n/g, '<br>') })
    } else {
      messages.value.push({ role: 'assistant', content: '抱歉，系统繁忙请稍后再试。' })
    }
  } catch {
    messages.value.push({ role: 'assistant', content: '网络异常，请稍后再试。' })
  } finally {
    typing.value = false
    await scrollBottom()
  }
}

async function scrollBottom() {
  await nextTick()
  const el = chatBodyRef.value
  if (el) el.scrollTop = el.scrollHeight
}
</script>

<style lang="scss" scoped>
.chat-widget {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 999;
}

.chat-fab {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff, #337ecc);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.4);
  transition: all 0.3s;

  &:hover {
    transform: scale(1.08);
    box-shadow: 0 6px 20px rgba(64, 158, 255, 0.5);
  }

  &.active {
    background: #909399;
    box-shadow: 0 4px 12px rgba(144, 147, 153, 0.4);
  }
}

.chat-panel {
  position: absolute;
  right: 0;
  bottom: 68px;
  width: 380px;
  height: 500px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  background: linear-gradient(135deg, #409eff, #337ecc);
  color: #fff;

  .chat-header-left {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
  }

  .chat-badge {
    font-size: 11px;
    font-weight: 500;
    background: rgba(255,255,255,0.25);
    padding: 2px 8px;
    border-radius: 10px;
    letter-spacing: 0.5px;
  }

  .chat-close {
    cursor: pointer;
    font-size: 18px;
    transition: opacity 0.2s;

    &:hover { opacity: 0.7; }
  }
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f5f7fa;
}

.chat-msg {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;

  &.user {
    flex-direction: row-reverse;

    .msg-bubble {
      background: #409eff;
      color: #fff;
      border-radius: 14px 4px 14px 14px;
    }
  }

  &.assistant {
    .msg-bubble {
      background: #fff;
      color: #303133;
      border-radius: 4px 14px 14px 14px;
      box-shadow: 0 1px 4px rgba(0,0,0,0.06);
    }
  }
}

.msg-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #909399;
  font-size: 14px;
}

.msg-bubble {
  padding: 10px 14px;
  font-size: 14px;
  line-height: 1.7;
  max-width: 270px;
  word-break: break-word;

  &.typing {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 14px 18px;

    span {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #c0c4cc;
      animation: typing 1.4s infinite both;

      &:nth-child(2) { animation-delay: 0.2s; }
      &:nth-child(3) { animation-delay: 0.4s; }
    }
  }
}

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-6px); opacity: 1; }
}

.chat-footer {
  padding: 12px;
  border-top: 1px solid #e4e7ed;
}

.fade-enter-active, .fade-leave-active {
  transition: all 0.25s ease;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
  transform: translateY(16px) scale(0.96);
}
</style>
