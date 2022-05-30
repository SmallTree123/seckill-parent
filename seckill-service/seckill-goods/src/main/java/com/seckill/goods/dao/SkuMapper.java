package com.seckill.goods.dao;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.goods.pojo.Sku;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;


import java.util.List;

/****
 * @Author:www.itheima.com
 * @Description:Sku的Dao
 * @Date  0:12
 *****/
public interface SkuMapper extends BaseMapper<Sku> {

    /**
     * 查询总数量
     * @return
     */
    @Select("SELECT count(*) FROM tb_spu sp INNER JOIN tb_sku sk ON sp.id=sk.spu_id WHERE sp.is_delete='0' AND sp.is_marketable='1' AND sp.`status`='1'")
    Integer count();

    /**
     * 锁定
     * @param id
     */
    @Update("update sku set lock=2 where id=#{id} and lock=1")
    void lock(String id);

    /**
     * 解锁
     * @param id
     */
    @Update("update sku set lock=1 where id=#{id} and lock=2")
    void unlock(String id);

    /***
     * 根据活动ID查询商品
     * @param id
     * @return
     */
    @Select("SELECT s.id,s.images,s.`name`,s.price,s.seckill_price seckillPrice,s.num,s.seckill_num seckillNum,s.alert_num alertNum,s.sale_num saleNum,s.image,s.count FROM tb_sku_act sa LEFT JOIN tb_sku s ON sa.sku_id=s.id WHERE sa.activity_id=#{id} ORDER BY sa.create_time DESC")
    List<Sku> findSkuByActivityId(String id);

    /***
     * 库存递减
     * 递减数量
     * 商品ID
     * ------->控制超卖
     */
    @Update("update tb_sku set seckill_num=seckill_num-#{count} where id=#{id} and seckill_num>=#{count} and islock=1")
    int dcount(@Param("id")String id,@Param("count")Integer count);

    /***
     * 修改
     */
    @Update("update tb_sku set islock=1 ,status=1")
    void modifySku();
}
