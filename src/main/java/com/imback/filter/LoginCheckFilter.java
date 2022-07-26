package com.imback.filter;

import com.alibaba.fastjson.JSON;
import com.imback.common.BaseContext;
import com.imback.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否已经登录 urlPatterns指拦截所有
 */
@Slf4j
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    //定义路径匹配器,支持通配符匹配
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        //先强转Servlet
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;
        String requestURI = request.getRequestURI();//uri是相对路径
        //1.定义一个需要放行的路径数组
        String[] urls = {
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/*",
                "/user/login", //移动端登录
                "/user/sendMsg" //移动端发送短信
        };
        //2.判断本次请求是否需要处理
        if(checkUrl(urls,requestURI)){
            //能匹配上，需要放行
            filterChain.doFilter(request,response);
            return;
        }
        //3-1.判断登录状态
        Long empId = (Long)request.getSession().getAttribute("employee");
        if( empId!= null){
            //已登陆，先把empId放到ThreadLocal中，再放行
            BaseContext.setCurrentId(empId);
            filterChain.doFilter(request,response);
            return;
        }
        //3-2.判断移动端用户登录状态
        Long userId = (Long)request.getSession().getAttribute("user");
        if( userId!= null){
            //已登陆，先把empId放到ThreadLocal中，再放行
            BaseContext.setCurrentId(userId);
            filterChain.doFilter(request,response);
            return;
        }
        //4.未登录,通过字符流响应
        response.getWriter().write(JSON.toJSONString(Result.error("NOTLOGIN")));
        return;
    }

    //路径匹配，是否需要放行
    public boolean checkUrl(String[] urls,String requestUrl){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestUrl);
            if(match){
                return true;
            }
        }
        return false;
    }
}
