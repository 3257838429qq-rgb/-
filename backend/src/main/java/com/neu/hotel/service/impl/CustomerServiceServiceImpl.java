package com.neu.hotel.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neu.hotel.service.CustomerServiceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 智能客服服务实现类
 *
 * 【功能说明】
 * - 提供东北大学校招待所的智能客服功能
 * - 基于DeepSeek AI大模型进行问答
 * - 支持酒店业务咨询和闲聊
 *
 * 【工作流程】
 * 1. 优先调用DeepSeek API进行AI回答
 * 2. API调用失败时，使用本地关键词匹配
 * 3. 关键词也不匹配时，返回客服电话
 *
 * 【配置项】
 * - deepseek.api-key: API密钥
 * - deepseek.api-url: API地址
 *
 * 【关联关系】
 * - 被 CustomerServiceController 调用
 *
 * 【对应前端】
 * - API：@/api/customer.js
 * - Vue组件：@/views/layout/ChatWidget.vue
 */
@Service
public class CustomerServiceServiceImpl implements CustomerServiceService {

    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${deepseek.api-url}")
    private String apiUrl;

    /**
     * 系统提示词
     * 定义AI客服的身份、知识库和回答规范
     */
    private static final String SYSTEM_PROMPT =
        "你是东北大学校招待所的智能客服助手小东。请根据以下信息回答用户问题：\n\n" +
        "【招待所基本信息】\n" +
        "- 名称：东北大学校招待所\n" +
        "- 地址：辽宁省沈阳市和平区文化路3号巷11号，东北大学校内\n" +
        "- 电话：024-83680000\n" +
        "- 邮箱：hotel@neu.edu.cn\n" +
        "- 服务时间：24小时前台服务\n\n" +
        "【房型与价格】\n" +
        "- 单人间：¥128/晚\n" +
        "- 标准间：¥198/晚\n" +
        "- 商务间：¥328/晚\n" +
        "- 套房：¥588/晚\n" +
        "实际价格以预订时系统显示为准。\n\n" +
        "【客房设施】\n" +
        "免费Wi-Fi、独立卫浴（24小时热水）、空调暖气、液晶电视、书桌衣柜、一次性洗漱用品。\n\n" +
        "【预订流程】\n" +
        "1. 用户在「客房浏览」页面选择房间\n" +
        "2. 点击「立即预订」填写入住人姓名、手机号、身份证号、入住和退房日期\n" +
        "3. 提交后等待管理员审核\n" +
        "4. 审核通过后可在「我的预订」中进行付款\n" +
        "5. 付款后即可入住\n\n" +
        "【VIP会员】\n" +
        "- 铜牌会员：充值¥500，享95折\n" +
        "- 银牌会员：充值¥1000，享9折\n" +
        "- 金牌会员：充值¥3000，享85折\n" +
        "- 钻石会员：充值¥5000，享8折\n" +
        "会员折扣在预订时自动计算房费。会员有效期1年。\n\n" +
        "【支付方式】\n" +
        "支持余额支付（VIP账户余额）、微信支付、支付宝、银行卡。\n\n" +
        "【入住与退房时间】\n" +
        "- 入住时间：14:00后\n" +
        "- 退房时间：12:00前\n" +
        "如需提前入住或延迟退房，请在预订时备注说明。\n\n" +
        "【退房流程】\n" +
        "用户在「我的预订」中对已入住订单点击「申请退房」，管理员审核后完成退房。\n\n" +
        "【来访预约】\n" +
        "校外访客通过「来访预约」提交信息，审核通过后方可到访。来访时需携带有效身份证件。\n\n" +
        "【停车】\n" +
        "校内设有停车场，住客免费停车。来访车辆请预约时填写车牌号。\n\n" +
        "【发票】\n" +
        "退房时向前台说明即可开具发票。\n\n" +
        "你是东北大学校招待所的智能客服小东，请以热情、友好的态度回应用户。\n" +
        "- 当用户咨询招待所相关问题（房型、价格、预订、VIP、支付、退房、设施、地址等），结合以上信息准确回答。\n" +
        "- 当用户闲聊或咨询其他话题时，可以自由聊天、讲笑话、聊生活、聊技术等，不必局限于招待所。\n" +
        "- 回答简洁明了，适当使用emoji，让对话更亲切。\n" +
        "- 如果用户要投诉或反馈问题，耐心倾听并尽力帮忙。";

    /**
     * 本地关键词匹配规则（AI不可用时的降级方案）
     * 格式：关键词1|关键词2|关键词3 -> 回复内容
     */
    private static final LinkedHashMap<String, String> FALLBACK_RULES = new LinkedHashMap<>();
    static {
        FALLBACK_RULES.put("价格|费用|多少钱|房价", "我们的房型：单人间¥128/晚、标准间¥198/晚、商务间¥328/晚、套房¥588/晚。");
        FALLBACK_RULES.put("预订|订房|怎么订", "浏览「客房浏览」→选房间→填信息→提交→等审核→付款入住。");
        FALLBACK_RULES.put("VIP|会员|折扣|充值", "铜牌¥500享95折 | 银牌¥1000享9折 | 金牌¥3000享85折 | 钻石¥5000享8折。");
        FALLBACK_RULES.put("退房|退订|取消", "在「我的预订」中点击「申请退房」，管理员审核后完成退房。");
        FALLBACK_RULES.put("支付|付款", "支持余额支付、微信支付、支付宝、银行卡。");
        FALLBACK_RULES.put("位置|地址|在哪", "沈阳市和平区文化路3号巷11号，东北大学校内。");
        FALLBACK_RULES.put("电话|联系", "前台电话：024-83680000。");
        FALLBACK_RULES.put("入住时间|退房时间|几点", "入住14:00后，退房12:00前。");
        FALLBACK_RULES.put("设施|wifi|WiFi|空调|热水", "免费Wi-Fi、独立卫浴、空调暖气、液晶电视、洗漱用品。");
    }

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public CustomerServiceServiceImpl() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 智能问答
     * @param question 用户问题
     * @return AI回复内容
     */
    @Override
    public String answer(String question) {
        // 空问题处理
        if (question == null || question.isBlank()) {
            return "您好！有什么可以帮您的？😊";
        }

        // 尝试调用DeepSeek AI
        try {
            Map<String, Object> body = Map.of(
                "model", "deepseek-chat",
                "messages", List.of(
                    Map.of("role", "system", "content", SYSTEM_PROMPT),
                    Map.of("role", "user", "content", question)
                ),
                "temperature", 0.7,
                "max_tokens", 500
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            // 发送API请求
            ResponseEntity<String> resp = restTemplate.postForEntity(
                apiUrl, new HttpEntity<>(objectMapper.writeValueAsString(body), headers), String.class);

            // 解析返回内容
            JsonNode root = objectMapper.readTree(resp.getBody());
            String reply = root.path("choices").get(0).path("message").path("content").asText();

            if (reply != null && !reply.isBlank()) {
                return reply;
            }
        } catch (Exception e) {
            // API调用失败，使用本地规则
        }

        // 降级方案：关键词匹配
        for (Map.Entry<String, String> entry : FALLBACK_RULES.entrySet()) {
            for (String kw : entry.getKey().split("\\|")) {
                if (question.contains(kw)) {
                    return entry.getValue();
                }
            }
        }

        // 兜底：返回客服电话
        return "抱歉，智能客服暂时不可用。请拨打前台电话 024-83680000 咨询，感谢您的理解。";
    }
}
