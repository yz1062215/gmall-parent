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
            if (matcher.match(url, path)) {
                return chain.filter(exchange);
            }
        }

        //3、只要是 /api/inner/的全部拒绝
        for (String url : authUrlProperties.getDenyUrl()) {
            boolean match = matcher.match(url, path);
            if (match) {
                //直接响应json数据即可
                Result<String> result = Result.build("", ResultCodeEnum.PERMISSION);
                return responseResult(result, exchange);
            }
        }

        //4.需要登录的请求  进行鉴权
        for (String url : authUrlProperties.getLoginAuthUrl()) {
            if (matcher.match(url, path)) {
                //如果是需要登录的请求 进行登录校验
                //3.1获取token信息   不关返回值是否为空都返回
                String tokenValue = getTokenValue(exchange);
                //3.2 校验token
                UserInfo info = getTokenInfo(tokenValue);
                //3.3 判断用户信息是否正确
                if (info != null) {//redis存在此用户
                    //用户id 透传  提供给其他微服务使用
                    ServerWebExchange mutateNewReq = userIdOrTempIdTransport(info, exchange);//透传的新请求
                    return chain.filter(mutateNewReq);

                } else {
                    //redis无此用户  假令牌  token不存在  未登录
                    //重定向登录页
                    return redirectToCustomPage(authUrlProperties.getLoginPage() + "?originUrl=" + uri, exchange);
                }
            }
        }


        //5.普通请求
        //能走到这儿，既不是静态资源直接放行，也不是必须登录才能访问的，就一普通请求
        //普通请求只要带了 token，说明可能登录了。只要登录了，就透传用户id。

        String tokenValue = getTokenValue(exchange);//获取登录令牌
        UserInfo info = getTokenInfo(tokenValue);//查redis
        if (!StringUtils.isEmpty(tokenValue) && info==null){//假token  前端携带token  但是 redis不存在
            //重定向到登录页面 并且让cookie中的token和userInfo过期
            return redirectToCustomPage(authUrlProperties.getLoginPage() + "?originUrl=" + uri, exchange);
        }
        //userInfo不为为空并且token为空   解决用户token被删除 登录状态异常问题
        if (exchange.getRequest().getCookies().getFirst("userInfo")!=null&&StringUtils.isEmpty(tokenValue)){

          return   redirectToCustomPageUI(exchange.getRequest().getPath().toString(), exchange);

        }

        //普通请求直接透穿
        exchange = userIdOrTempIdTransport(info, exchange);
        //
        return chain.filter(exchange);
    }

    /**
     * 用户id透传  临时id透传
     *
     * @param info
     * @param exchange
     */
    private ServerWebExchange userIdOrTempIdTransport(UserInfo info, ServerWebExchange exchange) {
        //本质 更改请求头
        //ServerHttpRequest oldRequest = exchange.getRequest();
        ServerHttpRequest.Builder newRequestBuild = exchange.getRequest().mutate();

        //用户登录
        if (info != null) {
            newRequestBuild.header(SysRedisConst.USERID_HEADER, info.getId().toString());
        }
        //未登录 穿透临时id
        String userTempId = getUserTempId(exchange);
        newRequestBuild.header(SysRedisConst.USERTEMPID_HEADER, userTempId);
        ServerWebExchange mutateNewReq = exchange.mutate()
                .request(newRequestBuild.build())//传入自定义请求
                .response(exchange.getResponse())//传入老响应
                .build();
        return mutateNewReq;


    }

    /**
     * 获取前端传来的临时用户ID
     *
     * @param exchange
     * @return
     */
    private String getUserTempId(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        //1.尝试获取前端请求头中的临时ID
        String tempUserId = request.getHeaders().getFirst("userTempId");
        //2.头中没有 尝试获取cookie
        if (StringUtils.isEmpty(tempUserId)) {
            HttpCookie cookie = request.getCookies().getFirst("userTempId");
            if (cookie!=null){
                tempUserId = cookie.getValue();
            }
        }
        return tempUserId;

    }

    /**
     * 内部请求响应一个结果即可
     *
     * @param result
     * @param exchange
     * @return
     */
    private Mono<Void> responseResult(Result<String> result, ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.OK);
        String jsonStr = Jsons.toStr(result);

        DataBuffer dataBuffer = response.bufferFactory()//buffer工厂
                .wrap(jsonStr.getBytes());//包装

        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return response.writeWith(Mono.just(dataBuffer));
    }

    /**
     * 重定向到指定位置
     *
     * @param location
     * @param exchange
     * @return
     */
    private Mono<Void> redirectToCustomPage(String location, ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();

        //1、重定向【302状态码 + 响应头中 Location: 新位置
        response.setStatusCode(HttpStatus.FOUND);
        response.getHeaders().add(HttpHeaders.LOCATION, location);

        //2、清除旧的错误的Cookie[token]（同名cookie并max-age=0）解决无限重定向问题
        ResponseCookie tokenCookie = ResponseCookie.from("token", "666").maxAge(0).path("/").domain(".gmall.com").build();
        ResponseCookie userInfoCookie = ResponseCookie.from("userInfo", "666").maxAge(0).path("/").domain(".gmall.com").build();
        response.getCookies().set("token", tokenCookie);
        response.getCookies().set("userInfo", userInfoCookie);
        //3、响应结束
        return response.setComplete();
    }

    /**
     * 根据token值去redis查询用户真正信息
     *
     * @param tokenValue
     * @return
     */
    private UserInfo getTokenInfo(String tokenValue) {
        String jsonStr = redisTemplate.opsForValue().get(SysRedisConst.LOGIN_USER + tokenValue);
        if (!StringUtils.isEmpty(jsonStr)) {
            UserInfo info = Jsons.toObj(jsonStr, UserInfo.class);
            return info;
        }
        return null;

    }

    /**
     * 从cookie或者请求头中取得token对应的值
     * 获取token信息  前端乱传 token可能在cooke 也可能在header
     *
     * @param exchange
     * @return
     */
    private String getTokenValue(ServerWebExchange exchange) {

        String tokenValue = "";
        HttpCookie token = exchange.getRequest().getCookies().getFirst("token");
        HttpCookie userInfo = exchange.getRequest().getCookies().getFirst("userInfo");
        if (token != null) {
            //如果cook中存在
            tokenValue = token.getValue();
            return tokenValue;
        }
        //cook中没有
        tokenValue = exchange.getRequest().getHeaders().getFirst("token");
        return tokenValue;
    }


    private Mono<Void> redirectToCustomPageUI(String location, ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();

        //1、重定向【302状态码 + 响应头中 Location: 新位置
        response.setStatusCode(HttpStatus.OK);


        response.setStatusCode(HttpStatus.FOUND);
        response.getHeaders().add(HttpHeaders.LOCATION, location);
        ResponseCookie userInfoCookie = ResponseCookie.
                from("userInfo", "666")
                .maxAge(0).path("/")
                .domain(".gmall.com").build();

        String jsonStr = Jsons.toStr(userInfoCookie);
        DataBuffer dataBuffer = response.bufferFactory()//buffer工厂
                .wrap(jsonStr.getBytes());

        response.getCookies().set("userInfo", userInfoCookie);
        //3、响应结束
        return response.writeWith(Mono.just(dataBuffer));
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
