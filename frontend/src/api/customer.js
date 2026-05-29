/**
 * 智能客服相关API接口
 *
 * 【模块说明】
 * - 提供东北大学校招待所的智能客服功能
 * - 基于DeepSeek AI进行智能问答
 *
 * 【接口列表】
 * 1. askQuestion: 发送问题获取AI回答
 *
 * 【对应后端】
 * - Controller: CustomerServiceController
 * - 路径前缀: /cs
 *
 * 【对应前端组件】
 * - 聊天组件: @/views/layout/ChatWidget.vue
 */

import request from '@/utils/request'

/**
 * 发送问题，获取AI回答
 * @param {string} question - 用户问题
 * @returns {string} AI回复内容
 */
export function askQuestion(question) {
  return request({ url: '/cs/ask', method: 'post', data: { question } })
}
