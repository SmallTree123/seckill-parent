package com.itheima.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*****
 * @Author: itheima
 * @Project: seckill
 * @Description: com.itheima.controller.GoodsController
 ****/
@RestController
@RequestMapping(value = "/goods")
public class GoodsController {

    /***
     * 获取一件商品
     */
    @GetMapping(value = "/one/{max}")
    public String one(@PathVariable(value = "max")Integer max){
        System.out.println("最大个数是："+max);
        if(max>10){
            throw new RuntimeException("不允许超过10个");
        }
        return "华为 P40 Pro";
    }
}
