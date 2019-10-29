package com.leyou.page.controller;

import com.leyou.page.service.PageService;
import com.leyou.page.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {

    @Autowired
    private PageService pageService;

    @Autowired
    private FileService fileService;

    //根据spu的id对应展示，详情页面
    @GetMapping("item/{spuId}.html")
    public String toPage(@PathVariable("spuId")Long spuId, Model model){

        //根据spu的id查询所有的前台需要的数据，并封装到map中返回
        model.addAllAttributes(this.pageService.loadData(spuId));

        //存在吗？如果不存在则给你创建一个静态页面
        if(!this.fileService.exists(spuId)){
            //this.fileService.syncCreateHtml(spuId);
        }
        return "item";
    }
}
