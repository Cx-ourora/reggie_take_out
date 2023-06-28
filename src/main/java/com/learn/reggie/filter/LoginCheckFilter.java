package com.learn.reggie.filter;


import com.alibaba.fastjson.JSON;
import com.learn.reggie.common.BaseContext;
import com.learn.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {

    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        log.info("路径{}", request.getRequestURL());
        //获取本次请求的路径
        String requestURI = request.getRequestURI();

        //不需要拦截的路径
        String[] url = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/commmon/**",
                "/user/sendMsg",
                "/user/login"

        };

        //4判断请求是否需要拦截
        //4-1不需要处理或者已登录直接放行
        if (checks(url, requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }
        if (request.getSession().getAttribute("employee") != null) {
            Long employee = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(employee);

            filterChain.doFilter(request, response);
            return;
        }
        if (request.getSession().getAttribute("user") != null) {
            Long user = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(user);

            filterChain.doFilter(request, response);
            return;
        }
        //如果未登录，通过数据流响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }


    /**
     * 路径匹配检查
     *
     * @param urls
     * @param requestURL
     * @return
     */
    public boolean checks(String[] urls, String requestURL) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURL);
            if (match)
                return true;
        }
        return false;
    }
}
