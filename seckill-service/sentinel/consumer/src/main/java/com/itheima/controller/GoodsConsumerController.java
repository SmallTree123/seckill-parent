package com.itheima.controller;

import com.itheima.feign.GoodsFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*****
 * @Author: itheima
 * @Project: seckill
 * @Description: com.itheima.controller.GoodsConsumerController
 ****/
@RestController
@RequestMapping(value = "/consumer")
public class GoodsConsumerController {

    @Autowired
    private GoodsFeign goodsFeign;

    /***
     * feign调用
     * @return
     */
    @GetMapping(value = "/one/{max}")
    public String getOne(@PathVariable(value = "max")Integer max){
        return goodsFeign.one(max);
    }


    @GetMapping(value = "/uone/{max}")
    public String one(){
        return "No"+Math.random()*1000;
    }
}
