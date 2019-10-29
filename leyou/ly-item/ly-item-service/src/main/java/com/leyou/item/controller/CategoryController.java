package com.leyou.item.controller;

import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("category")
public class CategoryController {


    @Autowired
    private CategoryService categoryService;

    @GetMapping("list") //根据分类的父id查询分类对象
    public ResponseEntity<List<Category>> queryByParentId(@RequestParam("pid")Long id){

        List<Category> categories = this.categoryService.queryByParentId(id);
        if (categories != null && 0!=categories.size()) {
            return ResponseEntity.ok(categories);//返回list集合同时返回状态码200
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("bid/{bid}")
    public ResponseEntity<List<Category>> queryByBrandId(@PathVariable("bid")Long bid){
        //根据品牌的id，查询对应的分类信息对象集合
        List<Category> categories = this.categoryService.queryByBrandId(bid);

        if (categories != null && 0!=categories.size()) {

            return ResponseEntity.ok(categories);
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    /**
     * 根据商品分类id查询名称
     * @param ids 要查询的分类id集合
     * @return 多个名称的集合
     */
    @GetMapping("names")
    public ResponseEntity<List<String>> queryNameByIds(@RequestParam("ids") List<Long> ids){
        List<String > list = this.categoryService.queryNamesByIds(ids);
        if (list == null || list.size() < 1) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(list);
    }
}
