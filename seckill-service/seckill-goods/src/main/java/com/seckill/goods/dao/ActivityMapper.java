package com.seckill.goods.dao;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.goods.pojo.Activity;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/****
 * @Author:www.itheima.com
 * @Description:Activity的Dao
 * @Date  0:12
 *****/
public interface ActivityMapper extends BaseMapper<Activity> {

    /***
     * 时间查询
     * @return
     */
    @Select("(SELECT * FROM tb_activity WHERE begintime<NOW() AND `status`=1 ORDER BY begintime DESC LIMIT 1) UNION (SELECT * FROM tb_activity WHERE begintime>=NOW() AND `status`=1 ORDER BY begintime ASC)  LIMIT 5")
    List<Activity> times();
}
