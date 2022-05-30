package com.seckill.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.seckill.goods.dao.ActivityMapper;
import com.seckill.goods.dao.SkuActMapper;
import com.seckill.goods.dao.SkuMapper;
import com.seckill.goods.pojo.Activity;
import com.seckill.goods.pojo.Sku;
import com.seckill.goods.pojo.SkuAct;
import com.seckill.goods.service.SkuActService;
import com.seckill.page.feign.SkuPageFeign;
import com.seckill.search.feign.SkuInfoFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

/****
 * @Author:www.itheima.com
 * @Description:SkuAct业务层接口实现类
 * @Date  0:16
 *****/
@Service
public class SkuActServiceImpl implements SkuActService {

    @Autowired
    private SkuActMapper skuActMapper;

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private SkuInfoFeign skuInfoFeign;

    @Autowired
    private SkuPageFeign skuPageFeign;

    /**
     * SkuAct条件+分页查询
     * @param skuAct 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<SkuAct> findPage(SkuAct skuAct, int page, int size){
        //分页
        PageHelper.startPage(page,size);
        //搜索条件构建
        QueryWrapper<SkuAct> example = createExample(skuAct);
        //执行搜索
        return new PageInfo<SkuAct>(skuActMapper.selectList(example));
    }

    /**
     * SkuAct分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<SkuAct> findPage(int page, int size){
        //静态分页
        PageHelper.startPage(page,size);
        //分页查询
        return new PageInfo<SkuAct>(skuActMapper.selectList(new QueryWrapper<>()));
    }

    /**
     * SkuAct条件查询
     * @param skuAct
     * @return
     */
    @Override
    public List<SkuAct> findList(SkuAct skuAct){
        //构建查询条件
        QueryWrapper<SkuAct> example = createExample(skuAct);
        //根据构建的条件查询数据
        return skuActMapper.selectList(example);
    }


    /**
     * SkuAct构建查询对象
     * @param skuAct
     * @return
     */
    public QueryWrapper<SkuAct> createExample(SkuAct skuAct){
        QueryWrapper<SkuAct> queryWrapper = new QueryWrapper<>();
        if(skuAct!=null){
            // 
            if(!StringUtils.isEmpty(skuAct.getSkuId())){
                queryWrapper.eq("skuId",skuAct.getSkuId());
            }
            // 
            if(!StringUtils.isEmpty(skuAct.getActivityId())){
                queryWrapper.eq("activityId",skuAct.getActivityId());
            }
        }
        return queryWrapper;
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(String id){
        skuActMapper.deleteById(id);
    }

    /**
     * 修改SkuAct
     * @param skuAct
     */
    @Override
    public void update(SkuAct skuAct){
        skuActMapper.updateById(skuAct);
    }

    /**
     * 增加SkuAct
     * @param skuAct
     */
    @Override
    public void add(SkuAct skuAct){
        //查询活动
        Activity activity = activityMapper.selectById(skuAct.getActivityId());

        //修改每个Sku的信息
        for (Sku sku : skuAct.getSkus()) {
            sku.setSeckillBegin(activity.getBegintime());
            sku.setSeckillEnd(activity.getEndtime());
            sku.setStatus("2"); //1普通商品，2秒杀商品
            sku.setIslock(1);   //未锁定
            sku.setSaleNum(sku.getSeckillNum());
            sku.setAlertNum(sku.getAlertNum());
            if(sku.getSeckillBegin()!=null){
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHH");
//                sku.setBgtime(simpleDateFormat.format(sku.getSeckillBegin()));
            }
            skuMapper.updateById(sku);

            //增加关联信息
            SkuAct newSkuAct = new SkuAct();
            newSkuAct.setActivityId(skuAct.getActivityId());
            newSkuAct.setSkuId(sku.getId());
            skuActMapper.insert(newSkuAct);
            try {
                //添加数据到索引库
                skuInfoFeign.modifySku(2,sku);
                //创建静态页
                skuPageFeign.html(sku.getId());
            } catch (Exception e) {
            }
        }
    }

    /**
     * 根据ID查询SkuAct
     * @param id
     * @return
     */
    @Override
    public SkuAct findById(String id){
        return  skuActMapper.selectById(id);
    }

    /**
     * 查询SkuAct全部数据
     * @return
     */
    @Override
    public List<SkuAct> findAll() {
        return skuActMapper.selectList(new QueryWrapper<>());
    }

    @Override
    public void deleteAll() {
        skuActMapper.delete(null);
    }

    /***
     * 增加活动下的商品
     * @param activity
     * @param skuList
     */
    @Override
    public void addList(Activity activity, List<Sku> skuList) throws Exception {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(simpleDateFormat.format(activity.getBegintime()));
        for (Sku sku : skuList) {
            SkuAct skuAct = new SkuAct();
            skuAct.setSkuId(sku.getId());
            skuAct.setActivityId(activity.getId());
            skuAct.setIsDel(1);
            skuActMapper.insert(skuAct);

            //更新状态
            sku.setSeckillBegin(activity.getBegintime());
            sku.setSeckillEnd(activity.getEndtime());
            sku.setSeckillPrice(sku.getPrice()-10);
            sku.setStatus("2");
            skuMapper.updateById(sku);
            //添加数据到索引库
            skuInfoFeign.modifySku(2,sku);
            //创建静态页
            //skuPageFeign.html(sku.getId());
        }
    }

    @Override
    public void deleteAllIndex() {
        skuInfoFeign.deleteAll();
    }
}
