package com.leyou.auth.service;

import com.leyou.auth.clients.UserClient;
import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.user.pojo.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@EnableConfigurationProperties(JwtProperties.class)
public class AuthService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtProperties jwtProps;

    public String accredit(String username, String password) {

        try {
            //跨服务查询用户信息
            User user = this.userClient.queryUser(username, password);

            //根据这个user，生成token
            if (null!=user){

                UserInfo userInfo = new UserInfo();

                //准备userInfo对象
                BeanUtils.copyProperties(user,userInfo);

                //根据userUser生成了token
                String token = JwtUtils.generateToken(userInfo, jwtProps.getPrivateKey(), jwtProps.getExpire());

                return token;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
