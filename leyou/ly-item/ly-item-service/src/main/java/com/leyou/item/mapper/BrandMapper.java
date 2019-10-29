package com.leyou.item.mapper;

import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<Brand> {

    @Insert("insert into tb_category_brand (category_id,brand_id) values(#{cid},#{bid})")
    void insertBrandCategory(@Param("bid") Long bid, @Param("cid") Long cid);

    @Delete("delete from tb_category_brand where brand_id = #{bid}")
    void deleteBrandCategory(@Param("bid") Long bid);


    @Select("select tb.* from tb_brand tb inner join tb_category_brand cb on cb.brand_id = tb.id where cb.category_id = #{cid} ")
    List<Brand> queryBrandByCategory(@Param("cid") Long cid);
}
