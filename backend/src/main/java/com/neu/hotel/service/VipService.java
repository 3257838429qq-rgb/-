package com.neu.hotel.service;

import com.neu.hotel.entity.VipMember;
import com.neu.hotel.entity.VipPackage;
import com.neu.hotel.entity.VipRecharge;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface VipService {

    VipMember getMemberInfo(Long userId);

    List<VipPackage> getAvailablePackages();

    VipMember recharge(Long userId, Long packageId, BigDecimal amount, String paymentMethod);

    List<VipRecharge> getRechargeHistory(Long userId);

    BigDecimal calculateDiscount(Long userId, BigDecimal originalPrice);

    BigDecimal getDiscountRate(Long userId);

    boolean isVip(Long userId);
}
