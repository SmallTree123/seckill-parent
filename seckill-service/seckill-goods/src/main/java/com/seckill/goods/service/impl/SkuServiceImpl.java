package com.seckill.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.seckill.goods.dao.ActivityMapper;
import com.seckill.goods.dao.SkuActMapper;
import com.seckill.goods.dao.SkuMapper;
import com.seckill.goods.pojo.Activity;
import com.seckill.goods.pojo.Sku;
import com.seckill.goods.pojo.SkuAct;
import com.seckill.goods.service.SkuService;
import com.seckill.util.StatusCode;
import com.sun.jmx.snmp.Timestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

/****
 * @Author:www.itheima.com
 * @Description:Sku业务层接口实现类
 * @Date  0:16
 *****/
@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private SkuActMapper skuActMapper;

    @Autowired
    private ActivityMapper activityMapper;

    /***
     * 库存递减
     * @param id
     * @param count
     * @return
     */
    @Override
    public int dcount(String id, Integer count) {
        //1.调用Dao实现递减
        int dcount = skuMapper.dcount(id, count);
        //2.递减失败
        if(dcount==0){
            //查询
            Sku sku = skuMapper.selectById(id);
            //2.1递减失败原因->库存不足->405
            if(sku.getSeckillNum()<count){
                return StatusCode.DECOUNT_NUM;
            }else if (sku.getIslock()==2){
                //2.2递减失败原因->变成热点->205
                return StatusCode.DECOUNT_HOT;
            }
            return 0;
        }
        return StatusCode.DECOUNT_OK;
    }


    @Override
    public int commonDcount(String id, Integer count) {
        return skuMapper.commonDcount(id, count);
    }

    /****
     * 热点商品隔离
     * @param id
     */
    @Override
    public void hotIsolation(String id) {
        //1.数据库该商品进行锁定操作
        Sku sku = new Sku();
        sku.setIslock(2);   //锁定
        UpdateWrapper<Sku> wrapper = new UpdateWrapper<>();
        wrapper.eq("islock",1);
        wrapper.eq("id",id);
        //执行锁定
        int count = skuMapper.update(sku, wrapper);

        //2.锁定成功了，则把商品存入到Redis缓存进行隔离
        if(count>0){
            String key = "SKU_"+id;
            Sku currentSku = skuMapper.selectById(id);
            //存储商品数量
            //redisTemplate.boundHashOps(key).increment("num",currentSku.getSeckillNum());
            //存储商品信息
            //Map<String,Object> info = new HashMap<String,Object>();
            //info.put("id",id);
            //info.put("price",currentSku.getSeckillPrice());
            //info.put("name",currentSku.getName());
            //redisTemplate.boundHashOps(key).put("info",info);

            //2.数据合并
            Map<String,Object> dataMap = new HashMap<String,Object>();
            dataMap.put("num",currentSku.getSeckillNum());
            //存储商品信息
            Map<String,Object> info = new HashMap<String,Object>();
            info.put("id",id);
            info.put("price",currentSku.getSeckillPrice());
            info.put("name",currentSku.getName());
            dataMap.put("info",info);
            //1.2次操作合并成1次
            redisTemplate.opsForValue().set(key+":id",id);
            redisTemplate.opsForValue().set(key+":price",currentSku.getSeckillPrice());
            redisTemplate.opsForValue().set(key+":name",currentSku.getName());
            redisTemplate.opsForValue().set(key+":num",currentSku.getSeckillNum());
        }
    }

    /***
     * 查询总数
     * @return
     */
    @Override
    public Integer count() {
        QueryWrapper<Sku> queryWrapper = new QueryWrapper<>();

        //1.商品状态  stauts=2
        queryWrapper.eq("status","2");
        //2.商品秒杀数量 seckillNum>0
        queryWrapper.gt("seckillNum",0);
        //3.seckillEnd>now()
        queryWrapper.gt("seckillEnd",new Date());
        return skuMapper.selectCount(queryWrapper);
    }

    /****
     * 分页列表查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public List<Sku> list(Integer page, Integer size) {
        //1.分页-PageHelper
        PageHelper.startPage(page,size);

        //2.分页集合查询
        QueryWrapper<Sku> queryWrapper = new QueryWrapper<>();
        //1.商品状态  stauts=2
        //criteria.eq()("status","2");
        //2.商品秒杀数量 seckillNum>0
        //criteria.andGreaterThan("seckillNum",0);
        //3.seckillEnd>now()
        //criteria.andGreaterThan("seckillEnd",new Date());
        return skuMapper.selectList(queryWrapper);
    }

    /**
     * Sku条件+分页查询
     * @param sku 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<Sku> findPage(Sku sku, int page, int size){
        //分页
        PageHelper.startPage(page,size);
        //搜索条件构建
        QueryWrapper<Sku> queryWrapper = new QueryWrapper<>();
        //执行搜索
        return new PageInfo<Sku>(skuMapper.selectList(queryWrapper));
    }

    /**
     * Sku分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<Sku> findPage(int page, int size){
        //静态分页
        PageHelper.startPage(page,size);
        //分页查询
        return new PageInfo<Sku>(skuMapper.selectList(new QueryWrapper<>()));
    }

    /**
     * Sku条件查询
     * @param sku
     * @return
     */
    @Override
    public List<Sku> findList(Sku sku){
        //构建查询条件
        QueryWrapper<Sku> queryWrapper = createExample(sku);
        //根据构建的条件查询数据
        return skuMapper.selectList(queryWrapper);
    }


    /**
     * Sku构建查询对象
     * @param
     * @return
     */
    public QueryWrapper createExample(Sku sku){
        QueryWrapper<Sku> queryWrapper = new QueryWrapper<>();
        if(sku!=null){
            // 商品id
            if(!StringUtils.isEmpty(sku.getId())){
                queryWrapper.eq("id",sku.getId());
            }
            // SKU名称
            if(!StringUtils.isEmpty(sku.getName())){
                    queryWrapper.le("name","%"+sku.getName()+"%");
            }
            // 价格（分）
            if(!StringUtils.isEmpty(sku.getPrice())){
                    queryWrapper.eq("price",sku.getPrice());
            }
            // 单位，分
            if(!StringUtils.isEmpty(sku.getSeckillPrice())){
                    queryWrapper.eq("seckillPrice",sku.getSeckillPrice());
            }
            // 库存数量
            if(!StringUtils.isEmpty(sku.getNum())){
                    queryWrapper.eq("num",sku.getNum());
            }
            // 库存预警数量
            if(!StringUtils.isEmpty(sku.getAlertNum())){
                queryWrapper.eq("alertNum",sku.getAlertNum());
            }
            // 商品图片
            if(!StringUtils.isEmpty(sku.getImage())){
                queryWrapper.eq("image",sku.getImage());
            }
            // 商品图片列表
            if(!StringUtils.isEmpty(sku.getImages())){
                queryWrapper.eq("images",sku.getImages());
            }
            // 创建时间
            if(!StringUtils.isEmpty(sku.getCreateTime())){
                queryWrapper.eq("createTime",sku.getCreateTime());
            }
            // 更新时间
            if(!StringUtils.isEmpty(sku.getUpdateTime())){
                queryWrapper.eq("updateTime",sku.getUpdateTime());
            }
            // SPUID
            if(!StringUtils.isEmpty(sku.getSpuId())){
                queryWrapper.eq("spuId",sku.getSpuId());
            }
            // 类目ID
            if(!StringUtils.isEmpty(sku.getCategory1Id())){
                queryWrapper.eq("category1Id",sku.getCategory1Id());
            }
            // 
            if(!StringUtils.isEmpty(sku.getCategory2Id())){
                queryWrapper.eq("category2Id",sku.getCategory2Id());
            }
            // 
            if(!StringUtils.isEmpty(sku.getCategory3Id())){
                queryWrapper.eq("category3Id",sku.getCategory3Id());
            }
            // 
            if(!StringUtils.isEmpty(sku.getCategory1Name())){
                queryWrapper.eq("category1Name",sku.getCategory1Name());
            }
            // 
            if(!StringUtils.isEmpty(sku.getCategory2Name())){
                queryWrapper.eq("category2Name",sku.getCategory2Name());
            }
            // 类目名称
            if(!StringUtils.isEmpty(sku.getCategory3Name())){
                queryWrapper.eq("category3Name",sku.getCategory3Name());
            }
            // 
            if(!StringUtils.isEmpty(sku.getBrandId())){
                queryWrapper.eq("brandId",sku.getBrandId());
            }
            // 品牌名称
            if(!StringUtils.isEmpty(sku.getBrandName())){
                queryWrapper.eq("brandName",sku.getBrandName());
            }
            // 规格
            if(!StringUtils.isEmpty(sku.getSpec())){
                queryWrapper.eq("spec",sku.getSpec());
            }
            // 销量
            if(!StringUtils.isEmpty(sku.getSaleNum())){
                queryWrapper.eq("saleNum",sku.getSaleNum());
            }
            // 评论数
            if(!StringUtils.isEmpty(sku.getCommentNum())){
                queryWrapper.eq("commentNum",sku.getCommentNum());
            }
            // 商品状态 1-正常，2-下架，3-删除
            if(!StringUtils.isEmpty(sku.getStatus())){
                queryWrapper.eq("status",sku.getStatus());
            }

            // 秒杀开始时间
            if(!StringUtils.isEmpty(sku.getSeckillBegin())){
                queryWrapper.le("seckillBegin",sku.getSeckillBegin());
            }
            // 秒杀结束时间
            if(!StringUtils.isEmpty(sku.getSeckillEnd())){
                queryWrapper.ge("seckillEnd",sku.getSeckillEnd());
            }
        }
        return queryWrapper;
    }


    /**
     * Sku构建查询对象
     * @param sku
     * @return
     */
    public QueryWrapper<Sku> createExampleSp(Sku sku){
        QueryWrapper<Sku> queryWrapper = new QueryWrapper<>();
        if(sku!=null){
            // 商品id
            if(!StringUtils.isEmpty(sku.getId())){
                queryWrapper.eq("id",sku.getId());
            }
            // SKU名称
            if(!StringUtils.isEmpty(sku.getName())){
                queryWrapper.like("name","%"+sku.getName()+"%");
            }
            // 价格（分）
            if(!StringUtils.isEmpty(sku.getPrice())){
                queryWrapper.eq("price",sku.getPrice());
            }
            // 单位，分
            if(!StringUtils.isEmpty(sku.getSeckillPrice())){
                queryWrapper.eq("seckillPrice",sku.getSeckillPrice());
            }
            // 库存数量
            if(!StringUtils.isEmpty(sku.getNum())){
                queryWrapper.eq("num",sku.getNum());
            }
            // 库存预警数量
            if(!StringUtils.isEmpty(sku.getAlertNum())){
                queryWrapper.eq("alertNum",sku.getAlertNum());
            }
            // 商品图片
            if(!StringUtils.isEmpty(sku.getImage())){
                queryWrapper.eq("image",sku.getImage());
            }
            // 商品图片列表
            if(!StringUtils.isEmpty(sku.getImages())){
                queryWrapper.eq("images",sku.getImages());
            }
            // 创建时间
            if(!StringUtils.isEmpty(sku.getCreateTime())){
                queryWrapper.eq("createTime",sku.getCreateTime());
            }
            // 更新时间
            if(!StringUtils.isEmpty(sku.getUpdateTime())){
                queryWrapper.eq("updateTime",sku.getUpdateTime());
            }
            // SPUID
            if(!StringUtils.isEmpty(sku.getSpuId())){
                queryWrapper.eq("spuId",sku.getSpuId());
            }
            // 类目ID
            if(!StringUtils.isEmpty(sku.getCategory1Id())){
                queryWrapper.eq("brandId",sku.getCategory1Id());
            }
            //
            if(!StringUtils.isEmpty(sku.getCategory2Id())){
                queryWrapper.eq("category2Id",sku.getCategory2Id());
            }
            //
            if(!StringUtils.isEmpty(sku.getCategory3Id())){
                queryWrapper.eq("category3Id",sku.getCategory3Id());
            }
            //
            if(!StringUtils.isEmpty(sku.getCategory1Name())){
                queryWrapper.eq("category1Name",sku.getCategory1Name());
            }
            //
            if(!StringUtils.isEmpty(sku.getCategory2Name())){
                queryWrapper.eq("category2Name",sku.getCategory2Name());
            }
            // 类目名称
            if(!StringUtils.isEmpty(sku.getCategory3Name())){
                queryWrapper.eq("category3Name",sku.getCategory3Name());
            }
            //
            if(!StringUtils.isEmpty(sku.getBrandId())){
                queryWrapper.eq("category1Id",sku.getBrandId());
            }
            // 品牌名称
            if(!StringUtils.isEmpty(sku.getBrandName())){
                queryWrapper.eq("brandName",sku.getBrandName());
            }
            // 规格
            if(!StringUtils.isEmpty(sku.getSpec())){
                queryWrapper.eq("spec",sku.getSpec());
            }
            // 销量
            if(!StringUtils.isEmpty(sku.getSaleNum())){
                queryWrapper.eq("saleNum",sku.getSaleNum());
            }
            // 评论数
            if(!StringUtils.isEmpty(sku.getCommentNum())){
                queryWrapper.eq("commentNum",sku.getCommentNum());
            }
            // 商品状态 1-正常，2-下架，3-删除
            if(!StringUtils.isEmpty(sku.getStatus())){
                queryWrapper.eq("status",sku.getStatus());
            }

            // 秒杀开始时间
            if(!StringUtils.isEmpty(sku.getSeckillBegin())){
                queryWrapper.gt("seckillBegin",sku.getSeckillBegin());
            }
            // 秒杀结束时间
            if(!StringUtils.isEmpty(sku.getSeckillEnd())){
                queryWrapper.le("seckillEnd",sku.getSeckillEnd());
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
        //查询商品信息
        Sku currentsku = skuMapper.selectById(id);

        if(currentsku!=null){
            //查询活动信息
            Activity currentActivity = activityMapper.selectOne(new QueryWrapper<Activity>().eq("beginTime",currentsku.getSeckillBegin()));
            //skuMapper.deleteByPrimaryKey(id);
            Sku sku = new Sku();
            sku.setId(id);
            sku.setStatus("1"); //非秒杀商品
            sku.setIslock(1);   //未锁定
            int count = skuMapper.updateById(sku);
            if(count>0){
                //移除关联
                SkuAct skuAct = new SkuAct();
                skuAct.setActivityId(currentActivity.getId());
                skuAct.setSkuId(currentsku.getId());
                skuActMapper.deleteById(skuAct);
            }
        }
    }

    /**
     * 修改Sku
     * @param sku
     */
    @Override
    public void update(Sku sku){
        skuMapper.updateById(sku);
    }

    /**
     * 增加Sku
     * @param sku
     */
    @Override
    public void add(Sku sku){
        skuMapper.insert(sku);
    }

    /**
     * 根据ID查询Sku
     * @param id
     * @return
     */
    @Override
    public Sku findById(String id){
        return  skuMapper.selectById(id);
    }

    /**
     * 查询Sku全部数据
     * @return
     */
    @Override
    public List<Sku> findAll() {
        return skuMapper.selectList(new QueryWrapper<>());
    }

    /***
     * 锁定商品
     * @param id
     */
    @Override
    public void lock(String id) {
        skuMapper.lock(id);
    }

    /***
     * 解锁商品
     * @param id
     */
    @Override
    public void unlock(String id) {
        skuMapper.unlock(id);
    }

    @Autowired
    private RedisTemplate redisTemplate;


    /***
     * 根据活动ID查询
     * @param id
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<Sku> findSkuByActivityId(String id, Integer page, Integer size) {
        //分页
        PageHelper.startPage(page,size);

        //查询
        List<Sku> skuList = skuMapper.findSkuByActivityId(id);
        return new PageInfo<Sku>(skuList);
    }



    /***
     * 提取所有ID
     * @param skuList
     * @return
     */
    public List<String> getIds(List<Sku> skuList){
        List<String> ids = new ArrayList<String>();

        for (Sku sku : skuList) {
            ids.add(sku.getId());
        }
        return ids;
    }

    /***
     * 归零设置
     * @param id
     */
    @Override
    public void zero(String id) {
        Sku sku = new Sku();
        sku.setId(id);
        sku.setSeckillNum(0);
        skuMapper.updateById(sku);
    }

    /***
     * 商品更新成非秒杀商品，未锁定商品
     */
    @Override
    public void modifySku() {
        //修改状态
        skuMapper.modifySku();
    }

    @Override
    public List<Sku> findTop(String id) {
        //查询对应的商品信息
        List<String> ids = skuActMapper.findSkuById(id);
        return skuMapper.selectList(new QueryWrapper<Sku>().in("id",ids));
    }
}
