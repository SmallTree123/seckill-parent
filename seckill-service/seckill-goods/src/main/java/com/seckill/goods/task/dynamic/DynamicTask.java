package com.seckill.goods.task.dynamic;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.seckill.goods.dao.SkuActMapper;
import com.seckill.goods.dao.SkuMapper;
import com.seckill.goods.pojo.Sku;
import com.seckill.goods.pojo.SkuAct;
import org.springframework.beans.factory.annotation.Autowired;

import javax.management.Query;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*****
 * @Author: http://www.itheima.com
 * @Description: com.seckill.goods.task.dynamic.DynamicTask
 ****/
public class DynamicTask implements SimpleJob {

    @Autowired
    private SkuActMapper skuActMapper;

    @Autowired
    private SkuMapper skuMapper;

    /****
     * 实现对应的业务
     * @param shardingContext
     */
    @Override
    public void execute(ShardingContext shardingContext) {
        //活动ID
        String id = shardingContext.getJobParameter();
        modify(id);
        System.out.println(shardingContext.getJobName()+"--------------->"+id);
    }

    /****
     * 1.根据活动ID查询活动ID下拥有的秒杀商品集合
     * 2.修改参与活动的秒杀商品状态，将状态改成非秒杀商品->MySQL->binlog->Canal->获取增量数据->Canal微服务订阅增量数据->调用【静态页微服务、搜索微服务】
     * @param id
     */
    public void modify(String id){
        //1.根据活动ID查询活动ID下拥有的秒杀商品集合F
        List<SkuAct> skuActs = skuActMapper.selectList(new QueryWrapper<SkuAct>().eq("activityId",id));
        List<String> ids = new ArrayList<>();
        for (SkuAct act : skuActs) {
            ids.add(act.getSkuId());
        }

        //2.修改参与活动的秒杀商品状态
        Sku sku = new Sku();
        sku.setStatus("1");

        QueryWrapper<Sku> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status","2");
        queryWrapper.in("id",ids);

        skuMapper.update(sku,queryWrapper);
    }
}
