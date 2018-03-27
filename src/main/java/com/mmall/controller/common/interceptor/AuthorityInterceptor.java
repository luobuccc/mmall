package com.mmall.controller.common.interceptor;

import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisSharedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by 53254 on 2018/3/14 10:04 /mmal
 */
@Slf4j
public class AuthorityInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        log.info("preHandle");
        HandlerMethod handlerMethod = (HandlerMethod) o;
        //解析类名 方法名
        String methodName = handlerMethod.getMethod().getName();
        String className = handlerMethod.getBean().getClass().getSimpleName();
        //解析参数
        StringBuilder requestParamBuffer = new StringBuilder();
        Map paramMap = httpServletRequest.getParameterMap();
        Iterator iterator = paramMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String mapKey = (String) entry.getKey();
            String mapValue = StringUtils.EMPTY;
            //value 返回string数组
            Object obj = entry.getValue();
            if (obj instanceof String[]) {
                String[] strs = (String[]) obj;
                mapValue = Arrays.toString(strs);
            }
            requestParamBuffer.append(mapKey).append("=").append(mapValue);
        }
        if (StringUtils.equals(className, "UserManageController") && StringUtils.equals(methodName, "login")) {
            log.info("拦截请求，ClassName:{},MethodName:{}", className, methodName);
            return true;
        }
        User user = null;
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isNotEmpty(loginToken)) {
            String userJsonStr = RedisSharedPoolUtil.get(loginToken);
            user = JsonUtil.string2Obj(userJsonStr, User.class);
        }
        log.info("拦截请求，ClassName:{},MethodName:{},param:{}", className, methodName, requestParamBuffer.toString());
        if (user == null || (user.getRole().intValue() != Const.Role.ROLE_ADMIN)) {
            //返回false
            httpServletResponse.reset();//重置response
            httpServletResponse.setCharacterEncoding("UTF-8");
            httpServletResponse.setContentType("application/json;charset=UTF-8");
            PrintWriter out = httpServletResponse.getWriter();
            if (user == null) {
                //特殊处理
                if (StringUtils.equals(className, "ProductManageController") && StringUtils.equals(methodName, "richTextImgUpload")) {
                    Map resultMap = Maps.newHashMap();
                    resultMap.put("success", false);
                    resultMap.put("msg", "用户未登录");
                    out.print(JsonUtil.obj2String(resultMap));
                } else {
                    out.print(JsonUtil.obj2String(ServerResponse.createByErrorMessage("拦截器拦截 用户未登录")));
                }
            } else {
                if (StringUtils.equals(className, "ProductManageController") && StringUtils.equals(methodName, "richTextImgUpload")) {
                    Map resultMap = Maps.newHashMap();
                    resultMap.put("success", false);
                    resultMap.put("msg", "无权限操作");
                    out.print(JsonUtil.obj2String(resultMap));
                } else {
                    out.print(JsonUtil.obj2String(ServerResponse.createByErrorMessage("拦截器拦截 用户无权限操作")));
                }
            }
            out.flush();
            out.close();
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        log.info("postHandle");
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        log.info("afterCompletion");
    }
}
