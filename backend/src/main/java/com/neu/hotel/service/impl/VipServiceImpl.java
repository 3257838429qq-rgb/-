package com.neu.hotel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.hotel.entity.VipMember;
import com.neu.hotel.entity.VipPackage;
import com.neu.hotel.entity.VipRecharge;
import com.neu.hotel.mapper.VipMemberMapper;
import com.neu.hotel.mapper.VipPackageMapper;
import com.neu.hotel.mapper.VipRechargeMapper;
import com.neu.hotel.service.VipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * VIP会员服务实现类
 *
 * 【功能说明】
 * - 管理VIP会员信息
 * - 处理会员充值
 * - 计算会员折扣
 *
 * 【VIP等级体系】
 * | 等级   | 充值金额 | 折扣率 |
 * |--------|----------|--------|
 * | 铜牌   | 500元    | 95%    |
 * | 银牌   | 1000元   | 90%    |
 * | 金牌   | 3000元   | 85%    |
 * | 钻石   | 5000元   | 80%    |
 *
 * 【充值规则】
 * - 选择套餐 -> 计算赠送金额 -> 更新余额和等级 -> 记录充值流水
 * - 会员有效期1年
 * - 续费时顺延有效期
 *
 * 【关联关系】
 * - 依赖 VipMemberMapper、VipPackageMapper、VipRechargeMapper
 * - 被 VipController 调用
 * - 被 CheckInServiceImpl 调用计算折扣
 *
 * 【对应前端】
 * - API：@/api/vip.js
 * - Vue组件：@/views/portal/Vip.vue
 */
@Service
public class VipServiceImpl implements VipService {

    @Autowired
    private VipMemberMapper vipMemberMapper;

    @Autowired
    private VipRechargeMapper vipRechargeMapper;

    @Autowired
    private VipPackageMapper vipPackageMapper;

    /**
     * 获取会员信息
     * 如果用户不是会员，返回空会员对象（等级0、无余额、无折扣）
     * @param userId 用户ID
     * @return 会员信息
     */
    @Override
    public VipMember getMemberInfo(Long userId) {
        LambdaQueryWrapper<VipMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VipMember::getUserId, userId);
        VipMember member = vipMemberMapper.selectOne(wrapper);
        if (member == null) {
            // 非会员，返回默认空会员
            member = new VipMember();
            member.setUserId(userId);
            member.setVipLevel(0);
            member.setBalance(BigDecimal.ZERO);
            member.setTotalRecharge(BigDecimal.ZERO);
            member.setDiscountRate(BigDecimal.ONE);  // 无折扣
            member.setStatus(1);
        }
        return member;
    }

    /**
     * 获取可用的VIP套餐列表
     * @return 套餐列表
     */
    @Override
    public List<VipPackage> getAvailablePackages() {
        LambdaQueryWrapper<VipPackage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VipPackage::getStatus, 1);  // 启用状态
        wrapper.orderByAsc(VipPackage::getLevel);  // 按等级升序
        return vipPackageMapper.selectList(wrapper);
    }

    /**
     * 会员充值
     * - 验证套餐有效性
     * - 计算赠送金额（按套餐赠送率）
     * - 更新会员余额和等级
     * - 记录充值流水
     * @param userId 用户ID
     * @param packageId 套餐ID
     * @param amount 充值金额
     * @param paymentMethod 支付方式
     * @return 更新后的会员信息
     */
    @Override
    @Transactional
    public VipMember recharge(Long userId, Long packageId, BigDecimal amount, String paymentMethod) {
        VipPackage pkg = vipPackageMapper.selectById(packageId);
        if (pkg == null || pkg.getStatus() != 1) {
            throw new RuntimeException("充值套餐不存在或已下架");
        }

        VipMember member = getMemberInfo(userId);

        // 计算赠送金额
        BigDecimal giftAmount = amount.multiply(pkg.getGiftRate()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = amount.add(giftAmount);

        // 如果是新会员，先创建会员记录
        if (member.getId() == null) {
            member.setBalance(BigDecimal.ZERO);
            member.setTotalRecharge(BigDecimal.ZERO);
            member.setCreateTime(LocalDateTime.now());
            member.setStatus(1);
            member.setVipLevel(0);
            member.setDiscountRate(BigDecimal.ONE);
            vipMemberMapper.insert(member);
        }

        // 更新余额和累计充值
        member.setBalance(member.getBalance().add(totalAmount));
        member.setTotalRecharge(member.getTotalRecharge().add(amount));

        // 升级VIP等级（如果充值金额达到更高等级）
        if (member.getVipLevel() == null || member.getVipLevel() < pkg.getLevel()) {
            member.setVipLevel(pkg.getLevel());
            member.setDiscountRate(pkg.getDiscountRate());
            member.setExpireDate(LocalDateTime.now().plusYears(1));  // 设置1年有效期
            member.computeActive();
        } else {
            // 续费，顺延有效期
            if (member.getExpireDate() != null && member.getExpireDate().isBefore(LocalDateTime.now())) {
                member.setExpireDate(LocalDateTime.now().plusYears(1));
                member.computeActive();
            } else if (member.getExpireDate() != null) {
                member.setExpireDate(member.getExpireDate().plusYears(1));
                member.computeActive();
            }
        }

        vipMemberMapper.updateById(member);
        member.computeActive();

        // 记录充值流水
        VipRecharge recharge = new VipRecharge();
        recharge.setUserId(userId);
        recharge.setMemberId(member.getId());
        recharge.setRechargeNo(generateRechargeNo());  // 生成充值单号
        recharge.setAmount(amount);
        recharge.setGiftAmount(giftAmount);
        recharge.setPaymentMethod(paymentMethod);
        recharge.setStatus(1);
        recharge.setCreateTime(LocalDateTime.now());
        vipRechargeMapper.insert(recharge);

        return member;
    }

    /**
     * 获取充值历史记录
     * @param userId 用户ID
     * @return 充值记录列表
     */
    @Override
    public List<VipRecharge> getRechargeHistory(Long userId) {
        LambdaQueryWrapper<VipRecharge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VipRecharge::getUserId, userId);
        wrapper.eq(VipRecharge::getStatus, 1);  // 有效充值
        wrapper.orderByDesc(VipRecharge::getCreateTime);
        return vipRechargeMapper.selectList(wrapper);
    }

    /**
     * 计算折扣后的价格
     * @param userId 用户ID
     * @param originalPrice 原价
     * @return 折后价
     */
    @Override
    public BigDecimal calculateDiscount(Long userId, BigDecimal originalPrice) {
        VipMember member = getMemberInfo(userId);
        if (!member.isActive()) {
            return originalPrice;  // 非VIP或已过期，无折扣
        }
        return originalPrice.multiply(member.getDiscountRate()).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 获取用户折扣率
     * @param userId 用户ID
     * @return 折扣率（1表示无折扣）
     */
    @Override
    public BigDecimal getDiscountRate(Long userId) {
        VipMember member = getMemberInfo(userId);
        if (!member.isActive()) {
            return BigDecimal.ONE;  // 无折扣
        }
        return member.getDiscountRate();
    }

    /**
     * 判断用户是否为VIP会员
     * @param userId 用户ID
     * @return 是否VIP
     */
    @Override
    public boolean isVip(Long userId) {
        VipMember member = getMemberInfo(userId);
        return member.isActive();
    }

    /**
     * 生成充值单号
     * 格式：RC + 年月日时分秒 + 4位随机数
     * @return 充值单号
     */
    private String generateRechargeNo() {
        return "RC" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
               + String.format("%04d", (int)(Math.random() * 10000));
    }
}
