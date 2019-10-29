package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("brand")
public class BrandController {

    @Autowired
    private BrandService brandService;

    @GetMapping("page")
    public ResponseEntity<PageResult<Brand>> pageQuery(
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "rows",defaultValue = "10")Integer rows,
            @RequestParam(value = "sortBy",required = false)String sortBy,
            @RequestParam(value = "desc",required = false)Boolean desc,
            @RequestParam(value = "key",required = false)String key
            ){
        PageResult<Brand> brandPageResult = this.brandService.pageQuery(page,rows,sortBy,desc,key);
        //判断分页数据不为空
        if (brandPageResult != null && null!=brandPageResult.getItems() && 0!=brandPageResult.getItems().size()) {
            return ResponseEntity.ok(brandPageResult);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping()
    public ResponseEntity<Void> addBrand(Brand brand,@RequestParam("cids") List<Long> cids){


        this.brandService.addBrand(brand,cids);

        return ResponseEntity.status(HttpStatus.CREATED).build();//201
    }

    @PutMapping
    public ResponseEntity<Void> updateBrand(Brand brand,@RequestParam("cids") List<Long> cids){

        this.brandService.updateBrand(brand,cids);

        return ResponseEntity.status(HttpStatus.CREATED).build();//201
    }

    /**
     * 根据分类的id查询分类对应品牌的信息
     * @param cid
     * @return
     */
    @GetMapping("cid/{cid}")
    public ResponseEntity<List<Brand>> queryBrandByCategory(@PathVariable("cid")Long cid){
        List<Brand> brands = this.brandService.queryBrandByCategory(cid);
        if (brands != null && 0!=brands.size()) {
            return ResponseEntity.ok(brands);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    /**
     * 根据品牌的id查询对应品牌的信息
     * @param bid
     * @return
     */
    @GetMapping("bid/{bid}")
    public ResponseEntity<Brand> queryBrandById(@PathVariable("bid")Long bid){
        Brand brand = this.brandService.queryBrandById(bid);
        if (brand != null ) {
            return ResponseEntity.ok(brand);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
