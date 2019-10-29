package com.leyou.search.test;


import com.leyou.LySearchService;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.search.clients.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.service.IndexService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LySearchService.class)
public class IndexCreateTest {


    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private IndexService indexService;


    @Autowired
    private GoodsRepository goodsRepository;


    @Autowired
    private ElasticsearchTemplate esTemplate;

    @Test
    public void createIndexesAndPutMapping(){
        esTemplate.createIndex(Goods.class);

        esTemplate.putMapping(Goods.class);
    }


    @Test
    public void loadData(){
        int page = 1;

        while (true){

            //分页查询所有的spuBo
            PageResult<SpuBo> spuBoPageResult = goodsClient.querySpuByPage(null, null, page, 50);

            //如果没查到说明查完了，直接停
            if (null==spuBoPageResult){
                break;
            }
            page++;

            //从查询结果中获取到所有的spuBo信息
            List<SpuBo> spuBos = spuBoPageResult.getItems();

            List<Goods> goodsList = new ArrayList<>();
            spuBos.forEach(spuBo -> {
                //把spuBo转换为goods
                Goods goods = indexService.buildGoods(spuBo);
                goodsList.add(goods);
            });

            //批量保存goods到索引库
            goodsRepository.saveAll(goodsList);


        }

    }
}
