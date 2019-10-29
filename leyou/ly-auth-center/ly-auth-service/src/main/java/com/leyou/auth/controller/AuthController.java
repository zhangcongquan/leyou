package com.leyou.auth.controller;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.sun.org.apache.regexp.internal.RE;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {


    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties jwtProps;

    /**
     * 今后登录直接找这里，登录的参数为用户名和密码
     * 没有返回结果，登录成功后会以用户的信息为基础
     * 生成token，token经过cookie来返回
     *
     * @param username
     * @param password
     * @return
     */
    @PostMapping("accredit")
    public ResponseEntity<Void> accredit(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletRequest request,
            HttpServletResponse response) {

        //登录并生成token
        String token = this.authService.accredit(username, password);

        // 登录校验
        if (StringUtils.isBlank(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);//401
        }


        //httpOnly,  由于js可以操作cookie，所以为了安全，拒绝被js操作，
        CookieUtils.setCookie(request, response, jwtProps.getCookieName(), token, jwtProps.getCookieMaxAge(), null, true);

        //如果生成了token则，使用cookie保存token
        return ResponseEntity.ok().build();
    }

    /**
     * 使用注解直接获取对应的cookie的值
     *
     * 由于每次用户在前端操作都会执行verify方法，所以，我们可以在verify中重新生成token和cookie
     *
     * @return
     */
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verify(@CookieValue("LY_TOKEN") String token,
                                           HttpServletRequest request,HttpServletResponse response) {

        try {
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProps.getPublicKey());
            //生成新的token
            String newToken = JwtUtils.generateToken(userInfo, jwtProps.getPrivateKey(), jwtProps.getExpire());

            //把token再一次保存到cookie中
            CookieUtils.setCookie(request,response,jwtProps.getCookieName(),newToken,jwtProps.getCookieMaxAge(),null,true);


            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 抛出异常，证明token无效，直接返回401
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    }
}
