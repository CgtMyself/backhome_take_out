package com.imback.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.imback.common.Result;
import com.imback.entity.User;
import com.imback.service.UserService;
import com.imback.utils.SMSUtils;
import com.imback.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 移动端发送收集验证码
     * @param request
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public Result<String> sendMsg(HttpServletRequest request, @RequestBody User user){
        //获取手机号
        String phone = user.getPhone();
        if(StringUtils.isNotEmpty(phone)){
            //获取随机验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("验证码 = {}",code);

            //调用阿里云提供的短信服务API完成短信发送
            //SMSUtils.sendMessage("测试","",phone,code);//需要注册阿里云，并开通提供的短信服务API

            //将生成的验证码保存到session,把手机号做为key
            request.getSession().setAttribute(phone , code);

            return Result.success("验证码发送成功");
        }

        return Result.error("短信发送失败");
    }

    /**
     * 移动端用户登录
     * @param session
     * @param map
     * @return
     */
    @PostMapping("/login")
    public Result<User> login(HttpSession session, @RequestBody Map map){
        log.info("map = {}",map);
        String phone = map.get("phone").toString();
        String code = map.get("code").toString();
        //先比对验证码是否一致
        Object codeInSession = session.getAttribute(phone);
        if(codeInSession != null && codeInSession.equals(code)){
            //一致，可以登录
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone , phone);
            User user = userService.getOne(queryWrapper);
            //判断该用户是否是新用户
            if(user == null){
                //新用户
                user = new User();
                user.setPhone(phone);
                userService.save(user);
            }
            //把用户Id放到session
            session.setAttribute("user",user.getId());
            return Result.success(user);
        }

        return Result.error("登录失败");
    }

    /**
     * 移动端用户退出登录
     * @param session
     * @return
     */
    @PostMapping("/loginout")
    public Result<String> loginout(HttpSession session){
        //清除缓存
        session.removeAttribute("user");
        return Result.success("退出成功");
    }
}
