package com.leyou.item.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {

    @Autowired
    private BrandMapper brandMapper;

    public PageResult<Brand> pageQuery(Integer page, Integer rows,String sortBy,Boolean desc,String key) {

        //开启分页查询
        PageHelper.startPage(page,rows);


        Example example = new Example(Brand.class);



        if (StringUtils.isNotBlank(key)){

            //创建一个查询条件的构造对象
            Example.Criteria criteria = example.createCriteria();

            criteria.andLike("name","%"+key+"%");
        }


        //判断排序字段不为空
        if (StringUtils.isNotBlank(sortBy)){

            //desc 为true ，倒序
            example.setOrderByClause(sortBy+ (desc ? " DESC":" ASC")); //order by id desc
        }



        //直接查询并转换为分页条件结果
        Page<Brand> brandPage = (Page<Brand>) brandMapper.selectByExample(example);


        //封装分页的结果对象
        return new PageResult<>(brandPage.getTotal(),new Long(brandPage.getPages()),brandPage);
    }


    @Transactional // 进行事务管理
    public void addBrand(Brand brand, List<Long> cids) {

        //直接保存brand,由于是自增主键，并且配置主键生成策略，所以这里会主键回显
        this.brandMapper.insertSelective(brand);

        cids.forEach(cid->{
            this.brandMapper.insertBrandCategory(brand.getId(),cid);
        });


    }

    /**
     * 首先对于brand来说，所有的属性都有，即包含了最重要的id，所以可以直接修改
     *
     *
     * @param brand
     * @param cids
     */
    @Transactional
    public void updateBrand(Brand brand, List<Long> cids) {

        //直接修改
        this.brandMapper.updateByPrimaryKeySelective(brand);

        //删除这个品牌之前的所有的关联分类，然后重新关联品牌和分类


        //根据品牌的id删除
        this.brandMapper.deleteBrandCategory(brand.getId());

        //然后重新建立品牌和分类的关系
        cids.forEach(cid->{
            this.brandMapper.insertBrandCategory(brand.getId(),cid);
        });

    }

    public Brand queryBrandById(Long brandId) {
        return this.brandMapper.selectByPrimaryKey(brandId);
    }

    public List<Brand> queryBrandByCategory(Long cid) {



        return this.brandMapper.queryBrandByCategory(cid);
    }
}
