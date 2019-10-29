package com.leyou.item.service;

import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import com.sun.org.apache.regexp.internal.RE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {


    @Autowired
    private CategoryMapper categoryMapper;

    public List<Category> queryByParentId(Long id) {

        Category record = new Category();
        record.setParentId(id);

        //根据父id执行查询，使用通用mapper
        return this.categoryMapper.select(record);
    }

    public List<Category> queryByBrandId(Long bid) {
        return this.categoryMapper.queryByBrandId(bid);
    }



    public List<String> queryNamesByIds(List<Long> cids) {

        List<String> names = new ArrayList<>();
        //一次性查询所有的id信息
        List<Category> categories = this.categoryMapper.selectByIdList(cids);

        //变量分类信息取得名称
        categories.forEach(category -> {
            names.add(category.getName());
        });


        return names;
    }
}
