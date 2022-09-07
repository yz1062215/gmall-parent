package com.atguigu.gmall.gateway.filter;

import com.atguigu.gmall.common.constant.SysRedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.gateway.properties.AuthUrlProperties;
import com.atguigu.gmall.model.user.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关的全局过滤器
 */
@Slf4j
@Component
public class GatewayGlobalFilter implements GlobalFilter, Ordered {
    //路径匹配器
    AntPathMatcher matcher = new AntPathMatcher();
    @Autowired
    AuthUrlProperties authUrlProperties;
    @Autowired
    StringRedisTemplate redisTemplate;

    /**
     * 鉴权过滤器
     *
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        //1.前置拦截
        String path = exchange.getRequest().getURI().getPath();//获取请求路径
        String uri = exchange.getRequest().getURI().toString();//获取uri
        log.info("{} 请求开始================", path);


        //2.对于静态资源  直接放行
        for (String url : authUrlProperties.getNoAuthUrl()) {
            if (matcher.match(url, path)){
                return chain.filter(exchange);
            }
        }

        //3、只要是 /api/inner/的全部拒绝
        for (String url : authUrlProperties.getDenyUrl()) {
            boolean match = matcher.match(url, path);
            if(match){
                //直接响应json数据即可
                Result<String> result = Result.build("",
                        ResultCodeEnum.PERMISSION);
                return responseResult(result,exchange);
            }
        }

        //4.需要登录的请求  进行鉴权
        for (String url : authUrlProperties.getLoginAuthUrl()) {
            if (matcher.match(url, path)){
                //如果是需要登录的请求 进行登录校验
                //3.1获取token信息   不关返回值是否为空都返回
                String tokenValue=getTokenValue(exchange);
                //3.2 校验token
                UserInfo info = getTokenInfo(tokenValue);
                //3.3 判断用户信息是否正确
                if (info!=null) {//redis存在此用户

                    //用户id 透传  提供给其他微服务使用
                    ServerWebExchange mutateNewReq = userIdTransport(info, exchange);//透传的新请求
                    return chain.filter(mutateNewReq);

                }else {
                    //redis无此用户  假令牌  token不存在  未登录
                    //重定向登录页
                    return redirectToCustomPage(authUrlProperties.getLoginPage()+"?originUrl="+uri,exchange);
                }
            }
        }


        //5.普通请求
        //能走到这儿，既不是静态资源直接放行，也不是必须登录才能访问的，就一普通请求
        //普通请求只要带了 token，说明可能登录了。只要登录了，就透传用户id。
        String tokenValue = getTokenValue(exchange);
        UserInfo info = getTokenInfo(tokenValue);

        if (info != null) {
            exchange = userIdTransport(info, exchange);
        }else {
            //如果前端带了token  依然没有用户信息 说明这是假令牌
            if(!StringUtils.isEmpty(tokenValue)) {
                return redirectToCustomPage(authUrlProperties.getLoginPage() + "?originUrl=" + uri, exchange);
            }
            //没有token  清除cookie
            //HttpCookie cookie = exchange.getRequest().getCookies().getFirst("userInfo");
            //cookie.getValue();
            //System.out.println(cookie.getValue());
            //h.getCookies().remove("userInfo");
        }

        return chain.filter(exchange);
    }

    /**
     * 用户id透传
     * @param info
     * @param exchange
     */
    private ServerWebExchange userIdTransport(UserInfo info, ServerWebExchange exchange) {
        //本质 更改请求头
        if (info!=null){
            //请求一旦发来，所有的请求数据是固定的，不能进行任何修改，只能读取
            ServerHttpRequest oldRequest = exchange.getRequest();

            //根据原来的请求，封装一个新的请求
            ServerHttpRequest newRequest = exchange
                    .getRequest()
                    .mutate()
                    .header(SysRedisConst.USERID_HEADER, info.getId().toString())
                    .build();
            //放行的时候传改掉的exchange
            //根据老请求变异新请求  mutate
            ServerWebExchange mutateNewReq = exchange.mutate().request(newRequest)//传入自定义请求
                    .response(exchange.getResponse())//传入老响应
                    .build();
            return mutateNewReq;
        }

        //没有用户
        return exchange;

    }

    /**
     * 内部请求响应一个结果即可
     * @param result
     * @param exchange
     * @return
     */
    private Mono<Void> responseResult(Result<String> result, ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.OK);
        String jsonStr = Jsons.toStr(result);

        DataBuffer dataBuffer = response
                .bufferFactory()//buffer工厂
                .wrap(jsonStr.getBytes());//包装

        response.getHeaders()
                .setContentType(MediaType.APPLICATION_JSON);
        return response.writeWith(Mono.just(dataBuffer));
    }

    /**
     * 重定向到指定位置
     * @param location
     * @param exchange
     * @return
     */
    private Mono<Void> redirectToCustomPage(String location,
                                            ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();

        //1、重定向【302状态码 + 响应头中 Location: 新位置
        response.setStatusCode(HttpStatus.FOUND);
        response.getHeaders().add(HttpHeaders.LOCATION,location);

        //2、清除旧的错误的Cookie[token]（同名cookie并max-age=0）解决无限重定向问题
        ResponseCookie tokenCookie=ResponseCookie
                .from("token","666")
                .maxAge(0)
                .path("/")
                .domain(".gmall.com")
                .build();
        response.getCookies().set("token", tokenCookie);
        //3、响应结束
        return response.setComplete();
    }

    /**
     * 根据token值去redis查询用户真正信息
     * @param tokenValue
     * @return
     */
    private UserInfo getTokenInfo(String tokenValue) {
        String jsonStr = redisTemplate.opsForValue().get(SysRedisConst.LOGIN_USER + tokenValue);
        if (!StringUtils.isEmpty(jsonStr)){
            UserInfo info = Jsons.toObj(jsonStr, UserInfo.class);
            return info;
        }
        return null;

    }

    /**
     * 从cookie或者请求头中取得token对应的值
     * 获取token信息  前端乱传 token可能在cooke 也可能在header
     * @param exchange
     * @return
     */
    private String getTokenValue(ServerWebExchange exchange) {

        String tokenValue="";
        HttpCookie token = exchange.getRequest().getCookies().getFirst("token");
        if (token!=null){
            //如果cook中存在
            tokenValue=token.getValue();
            return tokenValue;
        }
        //cook中没有
        tokenValue = exchange
                .getRequest()
                .getHeaders()
                .getFirst("token");
        return tokenValue;
    }

    /**
     * 设置优先级
     *
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
