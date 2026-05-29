package com.neu.hotel.controller;

import com.neu.hotel.common.result.Result;
import com.neu.hotel.entity.VipMember;
import com.neu.hotel.entity.VipPackage;
import com.neu.hotel.entity.VipRecharge;
import com.neu.hotel.service.VipService;
import com.neu.hotel.service.impl.CheckInServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * VIP会员控制器
 *
 * 【功能说明】
 * - 管理VIP会员的注册、充值、折扣计算
 * - 提供VIP套餐查询
 * - 支持余额支付功能
 *
 * 【API前缀】/vip
 *
 * 【主要接口】
 * 1. 获取会员信息 - GET /vip/member
 * 2. 获取VIP套餐列表 - GET /vip/packages
 * 3. 会员充值 - POST /vip/recharge
 * 4. 充值历史 - GET /vip/history
 * 5. 获取折扣率 - GET /vip/discount
 * 6. 计算折扣价格 - GET /vip/calculate
 *
 * 【VIP等级体系】
 * - 铜牌会员：充值500元，享95折
 * - 银牌会员：充值1000元，享9折
 * - 金牌会员：充值3000元，享85折
 * - 钻石会员：充值5000元，享8折
 *
 * 【关联关系】
 * - 依赖 VipService 处理会员业务逻辑
 * - 依赖 CheckInServiceImpl 用于折扣价格计算
 * - 关联 VipMember（会员信息）、VipPackage（套餐）、VipRecharge（充值记录）实体
 *
 * 【对应前端】
 * - 路由：/portal/vip
 * - API：@/api/vip.js
 * - Vue组件：@/views/portal/Vip.vue
 */
@RestController
@RequestMapping("/vip")
public class VipController {

    @Autowired
    private VipService vipService;

    @Autowired
    private CheckInServiceImpl checkInService;

    /**
     * 获取当前用户的VIP会员信息
     * GET /vip/member
     * 返回：会员等级、余额、总充值金额、折扣率、状态等
     */
    @GetMapping("/member")
    public Result getMemberInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        VipMember member = vipService.getMemberInfo(userId);
        return Result.success(member);
    }

    /**
     * 获取所有可用的VIP套餐列表
     * GET /vip/packages
     * 返回：各等级套餐的充值金额、赠送金额、折扣率等
     */
    @GetMapping("/packages")
    public Result getPackages() {
        List<VipPackage> packages = vipService.getAvailablePackages();
        return Result.success(packages);
    }

    /**
     * 会员充值
     * POST /vip/recharge
     * body: { packageId, amount, paymentMethod }
     * 逻辑：选择套餐 -> 计算赠送金额 -> 更新余额和等级 -> 记录充值流水
     */
    @PostMapping("/recharge")
    public Result recharge(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long packageId = Long.valueOf(params.get("packageId").toString());
        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        String paymentMethod = params.getOrDefault("paymentMethod", "余额支付").toString();

        VipMember member = vipService.recharge(userId, packageId, amount, paymentMethod);
        return Result.success(member);
    }

    /**
     * 获取会员充值历史记录
     * GET /vip/history
     * 返回：历次充值的订单号、金额、赠送金额、支付方式等
     */
    @GetMapping("/history")
    public Result getRechargeHistory(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<VipRecharge> history = vipService.getRechargeHistory(userId);
        return Result.success(history);
    }

    /**
     * 获取当前用户的折扣率信息
     * GET /vip/discount
     * 返回：折扣率和是否VIP状态
     */
    @GetMapping("/discount")
    public Result getDiscountRate(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        BigDecimal rate = vipService.getDiscountRate(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("discountRate", rate);
        result.put("isVip", vipService.isVip(userId));
        return Result.success(result);
    }

    /**
     * 计算折扣后的价格
     * GET /vip/calculate?price=198
     * 返回：原价、折后价、折扣率、节省金额、是否VIP
     */
    @GetMapping("/calculate")
    public Result calculateDiscount(@RequestParam BigDecimal price, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        BigDecimal discountedPrice = vipService.calculateDiscount(userId, price);
        BigDecimal discountRate = vipService.getDiscountRate(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("originalPrice", price);
        result.put("discountedPrice", discountedPrice);
        result.put("discountRate", discountRate);
        result.put("savedAmount", price.subtract(discountedPrice));
        result.put("isVip", vipService.isVip(userId));
        return Result.success(result);
    }
}
