package com.atguigu.gmall.item.aspect;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.atguigu.gmall.common.constant.SysRedisConst;
import com.atguigu.gmall.item.annotation.GmallCache;
import com.atguigu.gmall.item.cache.CacheOpsService;
import com.atguigu.gmall.model.to.SkuDetailTo;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

@Aspect//声明为切面
@Component
public class CacheAspect {
    /**
     * 目标方法： public SkuDetailTo getSkuDetailWithCache(Long skuId)
     * 连接点：所有目标方法的信息都在连接点
     * <p>
     * try{
     * //前置通知
     * 目标方法.invoke(args)
     * //返回通知
     * }catch(Exception e){
     * //异常通知
     * }finally{
     * //后置通知
     * }
     */
    @Autowired
    CacheOpsService cacheOpsService;

    @Around("@annotation(com.atguigu.gmall.item.annotation.GmallCache)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = null;
        Object arg = joinPoint.getArgs()[0];

        //TODO key 不同 方法不同
        //String cacheKey = SysRedisConst.SKU_INFO_PREFIX + arg;
        String cacheKey = determinCacheKey(joinPoint);
        //1.先查缓存
        //TODO 查缓存方法不同 返回数据不同
        Type returnType = getMethodGenericReturnType(joinPoint);
        Object cacheData = cacheOpsService.getCacheData(cacheKey, returnType);

        //2.缓存判断
        if (cacheData == null) {
            //缓存没有  回源
            //问布隆有无当前商品id
            String bloomName = determinBloomName(joinPoint);
            if(!StringUtils.isEmpty(bloomName)){
                //指定开启了布隆
                Object bVal = determinBloomValue(joinPoint);
                boolean contains = cacheOpsService.bloomContains(bloomName,bVal);
                if(!contains){
                    return null;
                }
            }

            boolean lock = false;
            String lockName = "";
            try {
                //布隆有  加锁  尝试
                //不同场景用自己的锁
                lockName = determinLockName(joinPoint);
                lock = cacheOpsService.tryLock(lockName); //49
                if (lock) {

                    //成功拿到锁
                    //调用目标方法
                    result = joinPoint.proceed(joinPoint.getArgs());
                    //调用成功  重新保存到缓存
                    cacheOpsService.saveData(cacheKey, result);
                    return result;
                } else {
                    //没拿到锁 睡眠1S查缓存
                    Thread.sleep(10000);
                    return cacheOpsService.getCacheData(cacheKey, SkuDetailTo.class);
                }
            } finally {
                //解锁操作  加上锁之后才执行解锁操作
                if(lock) cacheOpsService.unlock(lockName);

            }


        }
        return cacheData;
        ////public SkuDetailTo getSkuDetail(Long skuId)
        ////1.获取方法签名
        //MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //
        ////2.获取当时调用者调用目标方法时传递的所有参数
        //Object[] args = joinPoint.getArgs();
        //
        //System.out.println(joinPoint.getTarget());
        //System.out.println(joinPoint.getThis());
        //
        ////3.放行目标方法
        //Method method = signature.getMethod();
        //Object result=null;
        //try {
        //    //前置通知
        //    //目标方法执行
        //
        //    //方法对象可以通过joinPoint.getTarget()   joinPoint.getThis()  获取
        //    /*  实现类SkuDetailServiceImpl调用
        //        public SkuDetailTo getSkuDetail(Long skuId)方法，该方法被
        //     */
        //    result= method.invoke(joinPoint.getTarget(), args);//反射执行传入对象的方法  反射调用方法可以修改参数
        //    //返回通知
        //    //return null;
        //} catch (Exception e) {
        //    //异常通知
        //    throw  new RuntimeException(e);
        //
        //} finally {
        //    //后置通知
        //    return result;//可以自定义返回值
        //}
    }

    /**
     * 获取目标方法的精确返回值类型
     * @param joinPoint
     * @return
     */
    private Type getMethodGenericReturnType(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Method method = signature.getMethod();
        Type type = method.getGenericReturnType();
        return type;
    }

    /**
     * 根据当前连接点信息决定使用什么缓存使用什么key
     * @param joinPoint
     * @return
     */
    private String determinCacheKey(ProceedingJoinPoint joinPoint) {
        //1、拿到目标方法上的@GmallCache注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Method method = signature.getMethod();

        //2、拿到注解
        GmallCache cacheAnnotation = method.getDeclaredAnnotation(GmallCache.class);

        String expression = cacheAnnotation.cacheKey();

        //3、根据表达式计算缓存键
        String cacheKey = evaluationExpression(expression,joinPoint,String.class);

        return cacheKey;
    }

    //创建一个表达式解析器，这个是线程安全的
    ExpressionParser parser = new SpelExpressionParser();
    ParserContext context = new TemplateParserContext();
    private<T> T evaluationExpression(String expression,
                                      ProceedingJoinPoint joinPoint,
                                      Class<T> clz) {
        //1、得到表达式
        Expression exp = parser.parseExpression(expression, context);

        //2、sku:info:#{#params[0]}
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
        //3、取出所有参数，绑定到上下文
        Object[] args = joinPoint.getArgs();
        evaluationContext.setVariable("params",args);

        //4、得到表达式的值
        T expValue = exp.getValue(evaluationContext, clz);
        return expValue;
    }
    /**
     * 获取布隆过滤器的名字
     * @param joinPoint
     * @return
     */
    private String determinBloomName(ProceedingJoinPoint joinPoint) {
        //1、拿到目标方法上的@GmallCache注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Method method = signature.getMethod();

        //2、拿到注解
        GmallCache cacheAnnotation = method.getDeclaredAnnotation(GmallCache.class);

        String bloomName = cacheAnnotation.bloomName();

        return bloomName;
    }
    /**
     * 根据布隆过滤器值表达式计算出布隆需要判定的值
     * @param joinPoint
     * @return
     */
    private Object determinBloomValue(ProceedingJoinPoint joinPoint) {
        //1、拿到目标方法上的@GmallCache注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Method method = signature.getMethod();

        //2、拿到注解
        GmallCache cacheAnnotation = method.getDeclaredAnnotation(GmallCache.class);

        //3、拿到布隆值表达式
        String bloomValue = cacheAnnotation.bloomValue();

        Object expression = evaluationExpression(bloomValue, joinPoint, Object.class);

        return expression;
    }
    /**
     * 根据表达式计算出要用的锁的名字
     * @param joinPoint
     * @return
     */
    private String determinLockName(ProceedingJoinPoint joinPoint) {
        //1、拿到目标方法上的@GmallCache注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Method method = signature.getMethod();

        //2、拿到注解
        GmallCache cacheAnnotation = method.getDeclaredAnnotation(GmallCache.class);

        //3、拿到锁表达式
        String lockName = cacheAnnotation.lockName(); //lock-方法名
        if(StringUtils.isEmpty(lockName)){
            //没指定锁用方法级别的锁
            return SysRedisConst.LOCK_PREFIX+method.getName();
        }

        //4、计算锁值
        String lockNameVal = evaluationExpression(lockName, joinPoint, String.class);
        return lockNameVal;
    }

}
