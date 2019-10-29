package com.leyou.search.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.search.pojo.Goods;
import com.leyou.search.service.SearchService;
import com.leyou.search.utils.SearchRequest;
import com.leyou.search.utils.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchController {

    @Autowired
    private SearchService searchService;


    /**
     *
     * 根据前台传入的searchRequest 中的参数信息进行数据的查询并返回
     * @param searchRequest
     * @return
     */

    @PostMapping("page")
    public ResponseEntity<SearchResult> page(@RequestBody SearchRequest searchRequest){

        SearchResult goodsPageResult = this.searchService.page(searchRequest);

        if (goodsPageResult != null && null!=goodsPageResult.getItems() && 0!=goodsPageResult.getItems().size()) {
            return ResponseEntity.ok(goodsPageResult);
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }
}
