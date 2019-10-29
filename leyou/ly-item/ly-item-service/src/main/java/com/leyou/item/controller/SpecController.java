package com.leyou.item.controller;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("spec")
public class SpecController {

    @Autowired
    private SpecService specService;


    /**
     *
     * 根据分类的id 查询返回规格参数组，由于一个分类对应一个规格参数模版，一个规格参数模版对应多个规格组
     * 所以 查询返回的是个集合
     * @param cid
     * @return
     */
    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> querySpecGroups(@PathVariable("cid")Long cid){

        List<SpecGroup> specGroups = this.specService.querySpecGroups(cid);

        if (specGroups != null && 0!=specGroups.size()) {
            return ResponseEntity.ok(specGroups);
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    /**
     *
     * 根据规格组的id查询组内参数
     * 以及根据分类的id查询规格参数
     * 至于有多个查询参数，不知道，有什么用什么
     * @param gid
     * @return
     */
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> querySpecParam(
            @RequestParam(value = "gid",required = false)Long gid,
            @RequestParam(value = "cid",required = false)Long cid,
            @RequestParam(value = "searching",required = false)Boolean searching,
            @RequestParam(value = "generic",required = false)Boolean generic){

        List<SpecParam> specParams =  this.specService.querySpecParam(gid,cid,searching,generic);

        if (specParams != null && 0!=specParams.size()) {
            return ResponseEntity.ok(specParams);
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();//204

    }
}
