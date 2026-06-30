package com.web.interceptor;

import cn.hutool.core.util.StrUtil;
import com.web.annotation.IgnoreAuth;
import com.web.exception.BusinessException;
import com.web.service.TokenService;
import com.web.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LoginInterceptor implements HandlerInterceptor {

    public static final String LOGIN_TOKEN_KEY = "Token";
    private static final Logger LOG = LoggerFactory.getLogger(LoginInterceptor.class);

    @Resource
    private TokenService tokenService;

    @Resource
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        LOG.info("------------- LoginInterceptor 开始-------------");

        long startTime = System.currentTimeMillis();
        request.setAttribute("requestStartTime", startTime);

        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Headers",
                "x-requested-with,request-source,Token, Origin,imgType, Content-Type, cache-control,postman-token,Cookie, Accept,authorization");
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));

        if (request.getMethod().toUpperCase().equals("OPTIONS")) {
            return true;
        }

        IgnoreAuth annotation;
        if (handler instanceof HandlerMethod) {
            annotation = ((HandlerMethod) handler).getMethodAnnotation(IgnoreAuth.class);
        } else {
            return true;
        }

        String path = request.getRequestURL().toString();
        LOG.info("接口登录拦截:path:{}", path);

        String token = request.getHeader(LOGIN_TOKEN_KEY);
        LOG.info("登录校验开始，token：{}", token);

        if (annotation != null) {
            return true;
        }

        if (StrUtil.isEmpty(token)) {
            LOG.info("token 为空，请求被拦截");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            throw new BusinessException("token 为空，请求被拦截", HttpStatus.UNAUTHORIZED.value());
        }

        // 使用 JWT 验证 token
        if (!jwtUtil.validateToken(token)) {
            LOG.info("token 无效或已过期，请求被拦截");
            throw new BusinessException("token 无效或已过期，请求被拦截", HttpStatus.UNAUTHORIZED.value());
        }

        // 检查黑名单
        if (tokenService.isInBlacklist(token)) {
            LOG.info("token 已失效（在黑名单中），请求被拦截");
            throw new BusinessException("token 已失效，请重新登录", HttpStatus.UNAUTHORIZED.value());
        }

        // 从 JWT 中解析用户信息
        Claims claims = jwtUtil.parseToken(token);
        if (claims == null) {
            LOG.info("token 解析失败，请求被拦截");
            throw new BusinessException("token 解析失败，请求被拦截", HttpStatus.UNAUTHORIZED.value());
        }

        // 将用户信息存入 session
        request.getSession().setAttribute("userId", claims.get("userId", Integer.class));
        request.getSession().setAttribute("role", claims.get("role", String.class));
        request.getSession().setAttribute("tableName", claims.get("tableName", String.class));
        request.getSession().setAttribute("userName", claims.get("userName", String.class));
        
        LOG.info("登录校验通过，用户：{}", claims.get("userName"));
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        long startTime = (Long) request.getAttribute("requestStartTime");
        LOG.info("------------- LoginInterceptor 结束 耗时：{} ms -------------", System.currentTimeMillis() - startTime);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {

    }
}
