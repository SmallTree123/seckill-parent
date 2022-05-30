package com.seckill.search.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

/*****
 * @Author: http://www.itheima.com
 * @Project: seckillproject
 * @Description: com.seckill.search.pojo.SkuInfo
 * 和索引库对应的JavaBean
 ****/
@Document(indexName = "goodsindex",type = "skuinfo")
@Data
public class SkuInfo {

    //Sku相关的数据
    //商品id，同时也是商品编号
    @Id //唯一标识符,ES中对应的_id
    private String id;

    /***
     * SKU名称
     * type =FieldType.Text:指定当前name属性所对应的域的类型为Text类型，该类型支持分词支持创建索引
     *       FiledType.Keyword:不分词
     * searchAnalyzer="ik_smart":搜索所使用的分词器
     * analyzer = "ik_smart":添加索引所使用的分词器
     */
    @Field(type =FieldType.Text ,searchAnalyzer = "ik_smart",analyzer = "ik_smart",store =false)
    private String name;

    //商品价格，单位为：元
    private Long price;

    //秒杀价
    private Long seckillPrice;

    //商品图片
    private String image;

    //更新时间
    private Date updateTime;

    //类目ID
    private String category1Id;

    //类目ID
    private String category2Id;

    //类目ID
    private String category3Id;

    //类目名称
    @Field(type = FieldType.Keyword)
    private String category1Name;

    //类目名称
    @Field(type = FieldType.Keyword)
    private String category2Name;

    //类目名称
    @Field(type = FieldType.Keyword)
    private String category3Name;

    //品牌名称
    @Field(type = FieldType.Keyword)
    private String brandName;

    //开始时间，用于做搜索
    @Field(type = FieldType.Keyword)
    private String bgtime;

    //品牌ID
    private String brandId;

    private Date seckillBegin;//秒杀开始时间

    private Date seckillEnd;//秒杀结束时间

    private Integer status; //秒杀状态，1普通商品，2秒杀

    //规格
    private String spec;

    //比例
    private Integer points;

}
