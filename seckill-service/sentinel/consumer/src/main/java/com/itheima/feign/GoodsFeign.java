package com.itheima.feign;

import com.itheima.feign.fallback.GoodsFeignFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/*****
 * @Author: itheima
 * @Project: seckill
 * @Description: com.itheima.feign.GoodsFeign
 ****/
@FeignClient(value = "goods",fallbackFactory = GoodsFeignFallback.class)
//@FeignClient(value = "goods")
public interface GoodsFeign {

    /***
     * 获取一件商品
     */
    @GetMapping(value = "/goods/one/{max}")
    String one(@PathVariable(value = "max")Integer max);
}
