package com.seckill.goods.dao;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.goods.pojo.SkuAct;
import org.apache.ibatis.annotations.Select;


import java.util.List;

/****
 * @Author:www.itheima.com
 * @Description:SkuAct的Dao
 * @Date  0:12
 *****/
public interface SkuActMapper extends BaseMapper<SkuAct> {

    @Select("SELECT sku_id FROM tb_sku_act WHERE activity_id=#{id} ORDER BY create_time desc limit 4")
    List<String> findSkuById(String id);
}
