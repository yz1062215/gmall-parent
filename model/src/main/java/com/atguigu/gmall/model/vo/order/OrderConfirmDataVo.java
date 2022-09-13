package com.atguigu.gmall.model.vo.order;


import com.atguigu.gmall.model.user.UserAddress;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单确定vo
 */
@Data
public class OrderConfirmDataVo {
    private List<CartInfoVoNew> detailArrList;

    private Integer totalNum;

    private BigDecimal totalAmount;

    private List<UserAddress> userAddressesList;

    private String tradeNo;
}
