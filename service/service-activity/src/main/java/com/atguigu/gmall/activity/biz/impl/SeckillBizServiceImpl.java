package com.atguigu.gmall.activity.biz.impl;

import com.atguigu.gmall.activity.biz.SeckillBizService;
import com.atguigu.gmall.activity.service.SeckillGoodsCacheOpsService;
import com.atguigu.gmall.common.auth.AuthUtils;
import com.atguigu.gmall.common.constant.SysRedisConst;
import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.constant.MqConst;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.to.mq.SeckillTempOrderMsg;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@Service
public class SeckillBizServiceImpl implements SeckillBizService {

    @Autowired
    SeckillGoodsCacheOpsService cacheOpsService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 生成秒杀码
     *
     * @param skuId
     * @return
     */
    @Override
    public String generateSkuSeckillCode(Long skuId) {
        //生成秒杀码时先进行校验

        //1.获取当前商品
        SeckillGoods goods = cacheOpsService.getSeckillGoodsDetail(skuId);

        if (goods == null) {
            //请求不合法  不是参与秒杀的商品
            throw new GmallException(ResultCodeEnum.SECKILL_ILLEGAL);
        }

        //2.判断商品是否参与秒杀
        Date date = new Date();
        if (!date.after(goods.getStartTime())) {
            //2.1如果当前时间不在  秒杀商品开始时间之后
            //秒杀活动还未开始
            throw new GmallException(ResultCodeEnum.SECKILL_NO_START);
        }
        if (!date.before(goods.getEndTime())) {
            //2.2 如果当前时间不在秒杀活动结束之前  说明活动已结束
            throw new GmallException(ResultCodeEnum.SECKILL_END);
        }

        //3.判断秒杀商品库存是否充足

        if (goods.getStockCount() <= 0L) {
            //3.1如果库存不充足
            throw new GmallException(ResultCodeEnum.SECKILL_FINISH);
        }

        Long userId = AuthUtils.getCurrentAuthInfo()
                .getUserId();
        String code_date = DateUtil.formatDate(new Date());

        //通过特定算法生成秒杀码
        String code = generateCode(userId, skuId, code_date);


        return code;
    }

    /**
     * 生成秒杀码算法
     *
     * @param userId
     * @param skuId
     * @param code_date
     * @return
     */
    private String generateCode(Long userId, Long skuId, String code_date) {
        //1.通过md5算法+传入参数生成秒杀码
        String code = MD5.encrypt(userId + "_" + skuId + "_" + code_date);
        //2.并将秒杀码存入redis中缓存
        redisTemplate.opsForValue()
                .setIfAbsent(SysRedisConst.SECKILL_CODE + code, "1", 1, TimeUnit.DAYS);
        return code;
    }

    /**
     * 校验秒杀码
     *
     * @param skuId
     * @param code
     * @return
     */
    @Override
    public boolean checkSkuSeckillCode(Long skuId, String code) {
        //秒杀码校验
        //重新生成秒杀码与redis中数据对比
        String sysCode = MD5.encrypt(AuthUtils.getCurrentAuthInfo()
                .getUserId() + "_" + skuId + "_" + DateUtil.formatDate(new Date()));
        //请求携带的秒杀码与系统生成秒杀码比较  redis中判断秒杀码是否存在
        if (sysCode.equals(code) && redisTemplate.hasKey(SysRedisConst.SECKILL_CODE + code)) {
            //说明秒杀码合法
            return true;

        }
        return false;
    }

    /**
     * 秒杀预排队
     *
     * @param skuId
     * @param skuIdStr
     * @return
     */
    @Override
    public ResultCodeEnum seckillOrder(Long skuId, String skuIdStr) {
        //1.检查秒杀码是否合法
        boolean b = checkSkuSeckillCode(skuId, skuIdStr);
        if (!b) {
            //请求不合法
            return ResultCodeEnum.SECKILL_ILLEGAL;
        }

        //2.获取当前商品  检查是否为参与秒杀的商品
        SeckillGoods detail = cacheOpsService.getSeckillGoodsDetail(skuId);
        if (detail == null) {
            return ResultCodeEnum.SECKILL_FINISH;
        }

        //3.检查秒杀时间是否合法
        Date c_date = new Date();
        if (!c_date.after(detail.getStartTime())) {
            //说明当前时间在秒杀活动时间之前  活动还未开始
            return ResultCodeEnum.SECKILL_NO_START;
        }
        if (!c_date.before(detail.getEndTime())) {
            //说明活动已经结束
            return ResultCodeEnum.SECKILL_END;
        }

        //4.检查本地库存
        if (detail.getStockCount() <= 0L) {
            //本地库存不存在
            return ResultCodeEnum.SECKILL_FINISH;
        }

        //1----TODO  本地内存状态-1
        detail.setStockCount(detail.getStockCount() - 1);
        //5.判断秒杀请求是否发送过
        Long increment = redisTemplate.opsForValue()
                .increment(SysRedisConst.SECKILL_CODE + skuId);
        if (increment > 2) {
            //说明已经发送过一次请求
            return ResultCodeEnum.SUCCESS;
        }

        //6.开始秒杀业务.....
        //6.1 redis先扣除库存 seckill:goods:stock:46
        Long decrement = redisTemplate.opsForValue()
                .decrement(SysRedisConst.CACHE_SECKILL_GOODS_STOCK + skuId);
        if (decrement >= 0) {
            //6.2说明库存足够  扣减库存成功  发送消息执行创建订单业务
            //redis中创建一个临时订单表
            OrderInfo orderInfo = prepareTempSeckillOrder(skuId);
            //6.3 redis缓存临时秒杀订单表
            redisTemplate.opsForValue()
                    .set(SysRedisConst.SECKILL_ORDER + skuIdStr, Jsons.toStr(orderInfo));
            //2 TODO----发送消息开始创建订单
            SeckillTempOrderMsg tempOrderMsg = new SeckillTempOrderMsg(orderInfo.getUserId(), skuId, skuIdStr);
            String msg = Jsons.toStr(tempOrderMsg);
            rabbitTemplate.convertAndSend(MqConst.EXCHANGE_SECKILL_EVENT, MqConst.RK_SECKILL_ORDERWAIT, msg);
            return ResultCodeEnum.SUCCESS;
        } else {
            return ResultCodeEnum.SECKILL_FINISH;
        }

    }

    /**
     * 检查秒杀单的状态
     *
     * @param skuId
     * @return
     */
    @Override
    public ResultCodeEnum checkSeckillOrderStatus(Long skuId) {
        //1.根据算法自己生成秒杀码
        Long userId = AuthUtils.getCurrentAuthInfo().getUserId();
        String code = MD5.encrypt(userId + "_" + skuId + "_" + DateUtil.formatDate(new Date()));
        //2.根据秒杀码查询排队时通过秒杀码生成的临时订单来判断订单状态  seckill:goods:order:4df1cc1d6ae3e72d8f9ac254b1252c69
        String json = redisTemplate.opsForValue().get(SysRedisConst.SECKILL_ORDER + code);
        if (json == null) {
            //2.1如果订单为空 说明可能正在排队 前端每隔3S执行一次 
            return ResultCodeEnum.SECKILL_RUN;
        }
        if ("x".equals(json)) {
            //2.2 扣库存失败。redis临时单改成 x 错误标志。   -> 说明商品已售罄
            return ResultCodeEnum.SECKILL_FINISH;
        }
        /*
           下单状态判断在前   抢单判断在后
           如果下单了 订单id 和 OperateTime 都不为空
           抢单只是 OperateTime 不为空
           粒度由细 到 粗
         */
        //3.判断是否已经下过订单
        OrderInfo orderInfo = Jsons.toObj(json, OrderInfo.class);

        if (orderInfo.getId() != null && orderInfo.getId() > 0) {
            //3.1通过订单id是否存在判断是否生成过订单  雪花算法生成的id大于0
            return ResultCodeEnum.SECKILL_ORDER_SUCCESS;
        }

        //4.判断是否抢单成功   
        //    设置订单操作时间表示库存扣减操作过了->   是否抢单成功
        orderInfo.setOperateTime(new Date());
        if (orderInfo.getOperateTime() != null) {
            return ResultCodeEnum.SECKILL_SUCCESS;
        }
        //只要是成功状态就会继续查询最终状态
        return ResultCodeEnum.SUCCESS;
    }

    /**
     * 创建redis临时表的准备工作
     *
     * @param skuId
     * @return
     */
    private OrderInfo prepareTempSeckillOrder(Long skuId) {
        SeckillGoods detail = cacheOpsService.getSeckillGoodsDetail(skuId);
        //传入用户id
        Long userId = AuthUtils.getCurrentAuthInfo()
                .getUserId();
        OrderInfo orderInfo = new OrderInfo();

        orderInfo.setTotalAmount(detail.getCostPrice());
        orderInfo.setTradeBody(detail.getSkuName());
        orderInfo.setUserId(userId);
        orderInfo.setImgUrl(detail.getSkuDefaultImg());

        OrderDetail item = new OrderDetail();
        item.setSkuId(skuId);
        item.setUserId(userId);
        item.setSkuName(detail.getSkuName());
        item.setImgUrl(detail.getSkuDefaultImg());
        item.setOrderPrice(detail.getPrice());
        item.setSkuNum(1);
        item.setHasStock("1");
        item.setSplitTotalAmount(detail.getCostPrice());
        item.setSplitCouponAmount(detail.getPrice()
                .subtract(detail.getCostPrice())); //秒杀订单的优惠额度

        orderInfo.setOrderDetailList(Arrays.asList(item));

        return orderInfo;

    }
}
