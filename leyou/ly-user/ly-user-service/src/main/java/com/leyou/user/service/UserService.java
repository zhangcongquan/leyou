package com.leyou.user.service;

import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {


    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    Logger logger = LoggerFactory.getLogger(UserService.class);


    static final String KEY_PREFIX = "user:code:phone:";

    public Boolean checkData(String data, Integer type) {

        //如果type为1表示校验用户名，2，表示手机号
        User record = new User();

        switch (type) {
            case 1:
                record.setUsername(data);
                break;
            case 2:
                record.setPhone(data);
        }
        //根据上面的条件进行查询，如果查到了值则也只会有一个值，所以1!=1的结果为false
        //查询到了返回false，没查询到返回true
        return this.userMapper.selectCount(record)!=1;
    }

    /**
     *
     * 提出发送验证码的需求，把需求发送给消息中间件
     * @param phone
     * @return
     */
    public Boolean sendVerifyCode(String phone) {

        //指定长度生成一个随机值
        String code = NumberUtils.generateCode(5);

        try {
            // 发送短信
            Map<String, String> msg = new HashMap<>();
            msg.put("phone", phone);
            msg.put("code",code );
            //发消息
            this.amqpTemplate.convertAndSend("ly.sms.exchange", "sms.verify.code", msg);

            //存入到redis中，并指定有效周期为5分钟
            redisTemplate.opsForValue().set(KEY_PREFIX+phone,code,5, TimeUnit.MINUTES);

            return true;
        } catch (Exception e) {
            logger.error("发送短信失败。phone：{}， code：{}", phone, code);
            return false;
        }
    }

    /**
     *
     * 注册业务，进来后首先要验证验证码
     *
     * @param user
     * @param code
     * @return
     */
    public Boolean register(User user, String code) {

        //根据用户传的手机号获取对应的验证码
        String storeCode = this.redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());

        //获取的验证码不为空
        if (StringUtils.isNotBlank(storeCode)){
            //验证码校验通过
            if (storeCode.equals(code)){

                //保存用户数据
                user.setCreated(new Date());

                //生成随机的盐值
                String salt = CodecUtils.generateSalt();
                user.setSalt(salt);
                //根据原始的password和盐值，生成新的加密密码
                user.setPassword(CodecUtils.md5Hex(user.getPassword(),salt));

                this.userMapper.insertSelective(user);

                //验证一切通过后，应该把redis中对应的验证码删除

                this.redisTemplate.delete(KEY_PREFIX+user.getPhone());

                return true;

            }

            return null;
        }

        return null;
    }

    public User queryUser(String username, String password) {

        User record = new User();
        record.setUsername(username);
        //根据用户名查询用户信息
        User user = this.userMapper.selectOne(record);

        //根据用户名查询用户有可能不存在
        //TODO  各位今后生产中，只要是获取到的对象，在使用之前必须判空
        if (null==user){
            return null;
        }

        //根据用户输入的密码和存储的盐值重新生成密码
        String newPassword = CodecUtils.md5Hex(password, user.getSalt());

        //进行密码的比对
        if (newPassword.equals(user.getPassword())){
            return user;
        }

        return null;
    }
}
