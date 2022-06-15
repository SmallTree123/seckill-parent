package com.seckill.goods.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/****
 * @Author:www.itheima.com
 * @Description:Sku构建
 * @Date  19:13
 *****/
@Table(name="tb_sku")
@Data
public class Sku implements Serializable{

	@Id
    @Column(name = "id")
	private String id;//商品id

    @Column(name = "name")
	private String name;//SKU名称

    @Column(name = "price")
	private Integer price;//价格（分）

    @Column(name = "seckill_price")
	private Integer seckillPrice;//单位，分

    @Column(name = "num")
	private Integer num;//库存数量

    @Column(name = "alert_num")
	private Integer alertNum;//库存预警数量

    @Column(name = "image")
	private String image;//商品图片

    @Column(name = "images")
	private String images;//商品图片列表

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
//	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	@Column(name = "create_time")
	private Date createTime;//创建时间

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
//	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @Column(name = "update_time")
	private Date updateTime;//更新时间

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
//	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	@Column(name = "seckill_begin")
	private Date seckillBegin;//秒杀开始时间

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
//	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	@Column(name = "seckill_end")
	private Date seckillEnd;//秒杀结束时间

    @Column(name = "spu_id")
	private String spuId;//SPUID

    @Column(name = "category1_id")
	private Integer category1Id;//类目ID

    @Column(name = "category2_id")
	private Integer category2Id;//

    @Column(name = "category3_id")
	private Integer category3Id;//

    @Column(name = "category1_name")
	private String category1Name;//

    @Column(name = "category2_name")
	private String category2Name;//

    @Column(name = "category3_name")
	private String category3Name;//类目名称

    @Column(name = "brand_id")
	private Integer brandId;//

    @Column(name = "brand_name")
	private String brandName;//品牌名称

    @Column(name = "spec")
	private String spec;//规格

    @Column(name = "sale_num")
	private Integer saleNum;//销量

    @Column(name = "comment_num")
	private Integer commentNum;//评论数

    @Column(name = "status")
	private String status;//商品状态 1-正常，2-下架，3-删除

	@Column(name = "islock")
	private Integer islock;//商品状态 1正常，2锁定

	//秒杀开始日期
//	@Transient
//	private Date startDate;
//
//	//秒杀开始时间
//	@Transient
//	private String startTimestr;
//
//	//持续时长
//	@Transient
//	private Integer len;

	//秒杀数量
	@Column(name = "seckill_num")
	private Integer seckillNum;

	//商品审核状态   1 已审核
//	@Column(name = "audit")
//	private Integer audit;

	//限购数量
//	@Column(name = "count")
//	private Integer count;
//
//	//是否删除  1未删除，2已删除
//	@Column(name="isdel")
//	private Integer isdel;
//
//	@Transient
//	private Integer points;
////
//	@Transient
//	private String bgtime;

}
