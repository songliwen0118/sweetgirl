package com.song.sweet.core;

import com.alibaba.fastjson.JSON;
import com.song.sweet.model.User;
import com.song.sweet.utils.CommonUtils;
import com.song.sweet.utils.JWTUtils;
import com.song.sweet.utils.RedisUtils;
import eu.bitwalker.useragentutils.UserAgent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Liwen
 * @Description // 项目过滤器
 * @Version 1.0.0
 * @create 2019-05-17 16:29
 */
@Component
@WebFilter(urlPatterns = "/*", filterName = "ApiFilter")
public class ApiFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(ApiFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String version = "1.0.0";
        String method = request.getMethod();
        UserAgent userAgent = CommonUtils.getUserAgent(request);
        if (userAgent.getBrowserVersion() != null) {
            version = userAgent.getBrowserVersion().getMajorVersion();
        }
        logger.info("Http Request Info : {" +
                "\"Method\":\"" + method +
                "\",\"URL\":\"" + CommonUtils.getRequestURI(request) +
                "\",\"IpAddress\":\"" + CommonUtils.getIpAddress(request) +
                "\",\"Content-Type\":\"" + CommonUtils.getContentType(request) +
                "\",\"User-System\":\"" + userAgent.getOperatingSystem() +
                "\",\"User-Browser\":\"" + userAgent.getBrowser() + version +
                "\"}");

        response.setContentType("application/json;charset=UTF-8");
        // 允许跨域
        response.setHeader("Access-Control-Allow-Origin", "*");
        // 允许自定义请求头token(允许head跨域)
        response.setHeader("Access-Control-Allow-Headers", "token, Accept, Origin, X-Requested-With, Content-Type, Last-Modified");
        // 允许前端拿到的header
        response.setHeader("Access-Control-Expose-Headers", "token, Accept, Origin, X-Requested-With, Content-Type, Last-Modified");

        String token = request.getHeader(JWTUtils.TOKEN_HEADER);
        try {
            ServletContext sc = request.getSession().getServletContext();
            WebApplicationContext cxt = WebApplicationContextUtils.getWebApplicationContext(sc);
            RedisUtils redisUtils = cxt.getBean(RedisUtils.class);
            Boolean invalid = redisUtils.hasKey("tokenBlackList:" + token);
            if (StringUtils.isNotBlank(token) && token.startsWith(JWTUtils.TOKEN_PREFIX) && JWTUtils.isExpiration(token) && !invalid) {
                token = token.substring(JWTUtils.TOKEN_PREFIX.length());
                if (JWTUtils.canRefresh(token)) {
                    redisUtils.setCache("tokenBlackList:" + token, 1, JWTUtils.getExpireTime(token) / 1000);
                    token = JWTUtils.refreshToken(token, false);
                }
                User user = JWTUtils.getUserInfo(token);
                if (user != null) {
                    request.setAttribute("currentUser", user);
                }
                response.setStatus(HttpStatus.ACCEPTED.value());
                request.setAttribute(JWTUtils.TOKEN_HEADER, token);

                filterChain.doFilter(request, response);
            } else {
                filterChain.doFilter(request, response);
            }
        } catch (Exception e){
            logger.debug(e.toString());
            response.getWriter().write(JSON.toJSONString(new ResponseEntity(e.getMessage(), HttpStatus.UNAUTHORIZED)));
        }
    }

    @Override
    public void destroy() {
    }

}
