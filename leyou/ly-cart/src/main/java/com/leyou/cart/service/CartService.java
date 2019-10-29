package com.leyou.cart.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.auth.entity.UserInfo;
import com.leyou.cart.interceptors.LoginInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {


    //redis 用的是map结构，Map<userId,skuId,sku>
    @Autowired
    private StringRedisTemplate redisTemplate;


    static final String KEY_PREFIX = "ly:cart:uid:";
    /**
     *
     * 判断是否已经加入过，根据cookie中的token获取用户的信息，进而获取到用户的id
     * @param cart
     */
    public void addCart(Cart cart) {

        try {
            UserInfo loginUser = LoginInterceptor.getLoginUser();

            //根据用户id获取当前用户在redis中存储的所有的值，key,value全部必须为string
            BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(KEY_PREFIX + loginUser.getId());


            //根据skuId获取在redis中保存的sku信息
            Object skuObj = ops.get(cart.getSkuId().toString());

            if (null!=skuObj){//存在改数量
                Cart storeCart = JsonUtils.nativeRead(skuObj.toString(), new TypeReference<Cart>() {
                });
                storeCart.setNum(storeCart.getNum()+cart.getNum());
                ops.put(cart.getSkuId().toString(),JsonUtils.serialize(storeCart));
            }else{
                //JsonUtils.serialize 只是把cart对象转为json
                ops.put(cart.getSkuId().toString(), JsonUtils.serialize(cart));
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //查询参数是隐式传输
    public List<Cart> queryCarts() {


        try {
            UserInfo loginUser = LoginInterceptor.getLoginUser();


            //获取到用户的对应的redis操作对象Map<userId,skuId,sku>
            BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(KEY_PREFIX + loginUser.getId());

            List<Object> skusObj = ops.values();

            List<Cart> carts = new ArrayList<>();
            //如果获取到用户的购物车信息不为空，则需要取出每个具体的值从string转成具体的对象
            if (null!=skusObj){
                skusObj.forEach(skuObj->carts.add(JsonUtils.nativeRead(skuObj.toString(), new TypeReference<Cart>() {
                })));
            }

            return carts;


        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateIncrementCart(Cart cart) {
        UserInfo loginUser = LoginInterceptor.getLoginUser();

        //获取到用户的对应的redis操作对象Map<userId,skuId,sku>
        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(KEY_PREFIX + loginUser.getId());

        String skuJson = ops.get(cart.getSkuId().toString()).toString();

        Cart storeCart = JsonUtils.nativeRead(skuJson, new TypeReference<Cart>() {
        });

        storeCart.setNum(storeCart.getNum()+1);

        //改完之后要存起来
        ops.put(storeCart.getSkuId().toString(),JsonUtils.serialize(storeCart));
    }

    public void deleteCart(Long skuId) {
        UserInfo loginUser = LoginInterceptor.getLoginUser();

        //获取到用户的对应的redis操作对象Map<userId,skuId,sku>
        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(KEY_PREFIX + loginUser.getId());

        //指定id从map中删除
        ops.delete(skuId.toString());
    }
}
