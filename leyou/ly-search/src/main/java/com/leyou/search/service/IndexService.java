package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.search.clients.CategoryClient;
import com.leyou.search.clients.GoodsClient;
import com.leyou.search.clients.SpecClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class IndexService {

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecClient specClient;

    @Autowired
    private GoodsRepository goodsRepository;


    //功能就是把分页查询的spuBo转换为goods
   public Goods buildGoods(SpuBo spuBo){

       Goods goods = new Goods();

       BeanUtils.copyProperties(spuBo,goods);

       //其余有四个属性无法匹配

       //all  标题  分类   品牌

       //跨服务根据分类的id查询到分类的名称
       List<String> names = categoryClient.queryNameByIds(Arrays.asList(spuBo.getCid1(), spuBo.getCid2(), spuBo.getCid3()));

       //把查询到的name转换为字符串，并拼接到title中
       String all = spuBo.getTitle() + StringUtils.join(names,' ');

       goods.setAll(all);


       //price  就是每个sku的价格


       //skus  就是sku的集合

       List<Sku> skus = this.goodsClient.querySkuBySpuId(spuBo.getId());

       //存放当前spu中的所有的sku的map形态（只取部分属性）
       List<Map<String,Object>> skuMapList = new ArrayList<>();


       List<Long> prices = new ArrayList<>();

       skus.forEach(sku -> {

           prices.add(sku.getPrice());
           Map<String,Object> skuMap = new HashMap<>();
           skuMap.put("id",sku.getId());
           skuMap.put("title",sku.getTitle());
           skuMap.put("image", StringUtils.isBlank(sku.getImages()) ? "":sku.getImages().split(",")[0]);
           skuMap.put("price",sku.getPrice());
           skuMapList.add(skuMap);
       });

       goods.setPrice(prices);

       //把sku的map形态的集合转为字符串存入
       goods.setSkus(JsonUtils.serialize(skuMapList));
       //specs

       //TODO 获取到所有的可搜索的规格参数
       List<SpecParam> specParams = this.specClient.querySpecParam(null, null, true,null);

       //接下来给specs赋值
       Map<String,Object> specs = new HashMap<>();

       //由于接下来要进行给specs赋值，值来自于spuDetail中genricSpec以及specailSpec的描述
       SpuDetail spuDetail = this.goodsClient.querySpuDetailBySpuId(spuBo.getId());


       //包获取的通用的规格参数json字符串转换为map，
       Map<Long,Object> genericMap = JsonUtils.nativeRead(spuDetail.getGenericSpec(), new TypeReference<Map<Long, Object>>() {
       });

        //包获取的特有的规格参数json字符串转换为map，
       Map<Long,List<String>> specialMap = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {
       });


       //遍历获取到所有的可搜索的规格参数
       specParams.forEach(specParam -> {


           //规格参数的id    根据id匹配值
           Long paramId = specParam.getId();
           //specs的key
           String name = specParam.getName();
           //通用参数
           Object value = null;
           if (specParam.getGeneric()){
               //通用参数，则直接从通用的规格参数map中取值
               value = genericMap.get(paramId);

               if (null!=value){
                   //如果是数值类型，要考虑，要不要加单位，要不要加区段
                   if (specParam.getNumeric()){
                       //数值类型需要加分段
                       value = this.chooseSegment(value.toString(),specParam);
                   }
               }

           }
           else {//特有参数，直接从特有规格的结果中取值
               value = specialMap.get(paramId);

           }
           if (null==value){
               value="其他";
           }
           specs.put(name,value);


       });

       //把得到的可查询的规格参数的简直对集合赋值给goods
       goods.setSpecs(specs);


       return goods;
   }

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    public void createIndex(Long id) {

       //spuID---->spu----->goods-----goodsRepository.save()


        //根据spuId，查询spu
        Spu spu = this.goodsClient.querySpuById(id);

        SpuBo spuBo = new SpuBo();

        //把spu转换为spuBo
        BeanUtils.copyProperties(spu,spuBo);

        //spu----》goods
        Goods goods = this.buildGoods(spuBo);

        this.goodsRepository.save(goods);

    }

    public void deleteIndex(Long id) {

       this.goodsRepository.deleteById(id);

    }
}
