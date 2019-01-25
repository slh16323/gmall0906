package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.service.CartInfoService;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class CartServiceImpl implements CartInfoService {

    @Autowired
    private CartInfoMapper cartInfoMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public CartInfo exsistCart(CartInfo cartInfo) {
        CartInfo cart = cartInfoMapper.selectOne(cartInfo);
        return cart;
    }

    @Override
    public void savaCartInfo(CartInfo cartInfo) {

        cartInfoMapper.insertSelective(cartInfo);
    }

    @Override
    public void flushCartCacah(String userId, String skuId) {
        String cartkey = "cart:" + userId + ":info";

        List<CartInfo> cartInfos = getCartInfos(userId);

        Jedis jedis = redisUtil.getJedis();

        //从缓存中回获取数据
        List<String> hvals = jedis.hvals(cartkey);
        if (hvals.size() <= 0) {
            HashMap<String, String> cartMap = new HashMap<>();
            for (CartInfo cartInfo : cartInfos) {
                cartMap.put(cartInfo.getSkuId(), JSON.toJSONString(cartInfo));
            }
            //将数据加入到缓存中
            jedis.hmset(cartkey, cartMap);
        } else {
            //更新缓存
            for (CartInfo cartInfo : cartInfos) {
                if (cartInfo.getSkuId().equals(skuId)) {
                    jedis.hset(cartkey, skuId, JSON.toJSONString(cartInfo));
                }
            }
        }
        jedis.close();
    }

    private List<CartInfo> getCartInfos(String userId) {

        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        List<CartInfo> cartInfos = cartInfoMapper.select(cartInfo);

        return cartInfos;
    }

    @Override
    public void updateCartInfo(CartInfo exsistCart) {
        Example example = new Example(CartInfo.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId", exsistCart.getUserId()).andEqualTo("skuId", exsistCart.getSkuId());

        cartInfoMapper.updateByExampleSelective(exsistCart, example);

    }

    @Override
    public List<CartInfo> getCartListFromCacah(String userId) {
        String cartKey = "cart:" + userId + ":info";
        Jedis jedis = redisUtil.getJedis();
        List<CartInfo> cartInfos = new ArrayList<>();

        List<String> hvals = jedis.hvals(cartKey);
        if (hvals.size() > 0) {
            for (String hval : hvals) {
                CartInfo cartInfo = JSON.parseObject(hval, CartInfo.class);
                cartInfos.add(cartInfo);
            }
        } else {
            cartInfos = getCartInfos(userId);
        }
        return cartInfos;
    }

    @Override
    public void merge(String userId, String listCartCookie) {
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        List<CartInfo> cartInfosFromDB = cartInfoMapper.select(cartInfo);

        List<CartInfo> cartInfosFromCache = JSON.parseArray(listCartCookie, CartInfo.class);
        if (cartInfosFromDB.size() > 0) {
            for (CartInfo info : cartInfosFromCache) {
                boolean b = if_new_cart(cartInfosFromDB, info);
                if (b) {
                    info.setUserId(userId);
                    //数据库中的购物车没有cookie中购物车的商品，将cookie中的商品插入到数据库中购物车中
                    cartInfoMapper.insertSelective(info);
                } else {
                    //将数据库中的商品进行更新
                    for (CartInfo cartInfo1 : cartInfosFromDB) {
                        if (cartInfo1.getSkuId().equals(info.getSkuId())) {
                            cartInfo1.setSkuNum(cartInfo1.getSkuNum() + info.getSkuNum());
                            cartInfo1.setCartPrice(cartInfo1.getCartPrice().multiply(new BigDecimal(cartInfo1.getSkuNum())));
                            cartInfoMapper.updateByPrimaryKey(cartInfo1);
                        }
                    }
                }
            }
        } else {
            for (CartInfo info : cartInfosFromCache) {
                info.setUserId(userId);
                cartInfoMapper.insertSelective(info);
            }
        }
        //刷新缓存
        flushCart(userId);
    }

    @Override
    public void flushCart(String userId) {

        String cartkey = "cart:" + userId + ":info";

        List<CartInfo> cartInfos = getCartInfos(userId);

        Jedis jedis = redisUtil.getJedis();
        if (cartInfos.size()>0) {
            HashMap<String, String> cartMap = new HashMap<>();
            for (CartInfo cartInfo : cartInfos) {
                cartMap.put(cartInfo.getSkuId(), JSON.toJSONString(cartInfo));
            }
            //将数据加入到缓存中
            jedis.hmset(cartkey, cartMap);
        }
        jedis.close();

    }



    private boolean if_new_cart(List<CartInfo> cartInfos, CartInfo cartInfo) {
        boolean b = true;

        for (CartInfo info : cartInfos) {
            String skuId = info.getSkuId();
            if (skuId.equals(cartInfo.getSkuId())) {
                b = false;
            }
        }
        return b;
    }

}
