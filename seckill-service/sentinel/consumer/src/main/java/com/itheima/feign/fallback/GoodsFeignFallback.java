package com.itheima.feign.fallback;

import com.itheima.feign.GoodsFeign;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

/*****
 * @Author: itheima
 * @Project: seckill
 * @Description: com.itheima.feign.fallback.GoodsFeignFallback
 ****/
@Component
public class GoodsFeignFallback implements FallbackFactory<GoodsFeign> {


    @Override
    public GoodsFeign create(Throwable throwable) {
        return new GoodsFeign() {
            @Override
            public String one(Integer max) {
                return "服务降级";
            }
        };
    }
}
