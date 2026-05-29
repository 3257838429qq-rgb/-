package com.neu.hotel.controller;

import com.neu.hotel.common.result.Result;
import com.neu.hotel.service.CustomerServiceService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 智能客服控制器
 *
 * 【功能说明】
 * - 提供东北大学校招待所的智能客服功能
 * - 基于DeepSeek AI大模型进行智能问答
 * - 支持酒店相关业务咨询（房型、价格、预订、VIP等）
 * - 无API时启用本地关键词匹配作为降级方案
 *
 * 【API前缀】/cs
 *
 * 【主要接口】
 * 1. 智能问答 - POST /cs/ask
 *
 * 【工作流程】
 * 1. 用户发送问题 -> DeepSeek AI回答
 * 2. AI调用失败时 -> 本地关键词匹配回复
 * 3. 关键词也不匹配 -> 返回客服电话
 *
 * 【关联关系】
 * - 依赖 CustomerServiceService 处理智能问答
 * - 使用 DeepSeek API 进行AI对话
 *
 * 【对应前端】
 * - API：@/api/customer.js
 * - Vue组件：@/views/layout/ChatWidget.vue（右下角聊天组件）
 */
@RestController
@RequestMapping("/cs")
public class CustomerServiceController {

    private final CustomerServiceService customerServiceService;

    public CustomerServiceController(CustomerServiceService customerServiceService) {
        this.customerServiceService = customerServiceService;
    }

    /**
     * 智能问答接口
     * POST /cs/ask
     * body: { "question": "我想预订房间" }
     * 返回AI生成的回复内容
     */
    @PostMapping("/ask")
    public Result ask(@RequestBody Map<String, String> body) {
        String question = body.get("question");
        String answer = customerServiceService.answer(question);
        return Result.success(answer);
    }
}
