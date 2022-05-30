package com.seckill.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.seckill.goods.dao.SeckillTimeMapper;
import com.seckill.goods.pojo.SeckillTime;
import com.seckill.goods.service.SeckillTimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/****
 * @Author:www.itheima.com
 * @Description:SeckillTime业务层接口实现类
 * @Date  0:16
 *****/
@Service
public class SeckillTimeServiceImpl implements SeckillTimeService {

    @Autowired
    private SeckillTimeMapper seckillTimeMapper;


    /**
     * SeckillTime条件+分页查询
     * @param seckillTime 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<SeckillTime> findPage(SeckillTime seckillTime, int page, int size){
        //分页
        PageHelper.startPage(page,size);
        //搜索条件构建
        QueryWrapper<SeckillTime> example = createExample(seckillTime);
        //执行搜索
        return new PageInfo<SeckillTime>(seckillTimeMapper.selectList(example));
    }

    /**
     * SeckillTime分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<SeckillTime> findPage(int page, int size){
        //静态分页
        PageHelper.startPage(page,size);
        //分页查询
        return new PageInfo<SeckillTime>(seckillTimeMapper.selectList(new QueryWrapper<>()));
    }

    /**
     * SeckillTime条件查询
     * @param seckillTime
     * @return
     */
    @Override
    public List<SeckillTime> findList(SeckillTime seckillTime){
        //构建查询条件
        QueryWrapper<SeckillTime> example = createExample(seckillTime);
        //根据构建的条件查询数据
        return seckillTimeMapper.selectList(example);
    }


    /**
     * SeckillTime构建查询对象
     * @param seckillTime
     * @return
     */
    public QueryWrapper<SeckillTime> createExample(SeckillTime seckillTime){
        QueryWrapper<SeckillTime> queryWrapper = new QueryWrapper<>();
        if(seckillTime!=null){
            // 
            if(!StringUtils.isEmpty(seckillTime.getId())){
                queryWrapper.eq("id",seckillTime.getId());
            }
            // 秒杀分类名字,双十一秒杀，每日时段秒杀等
            if(!StringUtils.isEmpty(seckillTime.getName())){
                queryWrapper.like("name","%"+seckillTime.getName()+"%");
            }
            // 开始时间
            if(!StringUtils.isEmpty(seckillTime.getStarttime())){
                queryWrapper.eq("starttime",seckillTime.getStarttime());
            }
            // 结束时间
            if(!StringUtils.isEmpty(seckillTime.getEndtime())){
                queryWrapper.eq("endtime",seckillTime.getEndtime());
            }
            // 秒杀时长,按小时计算
            if(!StringUtils.isEmpty(seckillTime.getTotalTime())){
                queryWrapper.eq("totalTime",seckillTime.getTotalTime());
            }
            // 状态，1：开启，2：停用
            if(!StringUtils.isEmpty(seckillTime.getStatus())){
                queryWrapper.eq("status",seckillTime.getStatus());
            }
        }
        return queryWrapper;
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(Integer id){
        seckillTimeMapper.deleteById(id);
    }

    /**
     * 修改SeckillTime
     * @param seckillTime
     */
    @Override
    public void update(SeckillTime seckillTime){
        seckillTimeMapper.updateById(seckillTime);
    }

    /**
     * 增加SeckillTime
     * @param seckillTime
     */
    @Override
    public void add(SeckillTime seckillTime){
        seckillTimeMapper.insert(seckillTime);
    }

    /**
     * 根据ID查询SeckillTime
     * @param id
     * @return
     */
    @Override
    public SeckillTime findById(Integer id){
        return  seckillTimeMapper.selectById(id);
    }

    /**
     * 查询SeckillTime全部数据
     * @return
     */
    @Override
    public List<SeckillTime> findAll(String name) {
        if(!StringUtils.isEmpty(name)){
            return seckillTimeMapper.selectList(new QueryWrapper<SeckillTime>().like("name","%"+name+"%"));
        }
        return seckillTimeMapper.selectList(new QueryWrapper<>());
    }

    /***
     * 审核
     * @param id
     * @param type
     */
    @Override
    public void audit(Integer id, Integer type) {
        SeckillTime seckillTime = new SeckillTime();
        seckillTime.setStatus(type);
        seckillTime.setId(id);
        seckillTimeMapper.updateById(seckillTime);
    }

    /***
     * 查询所有有效时间
     * @return
     */
    @Override
    public List<SeckillTime> findAllValidTimes() {
        return seckillTimeMapper.selectList(new QueryWrapper<SeckillTime>().eq("status",1));
    }

    /***
     * 删除所有时间段
     */
    @Override
    public void deleteAll() {
        seckillTimeMapper.delete(null);
    }

    /***
     * 批量增加时间段
     * @param seckillTimes
     */
    @Override
    public void addTimes(List<SeckillTime> seckillTimes) {
        for (SeckillTime seckillTime : seckillTimes) {
            seckillTimeMapper.insert(seckillTime);
        }
    }

}