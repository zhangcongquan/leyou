package com.leyou.search.service;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import com.leyou.item.pojo.SpecParam;
import com.leyou.search.clients.BrandClient;
import com.leyou.search.clients.CategoryClient;
import com.leyou.search.clients.SpecClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.utils.SearchRequest;
import com.leyou.search.utils.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchService {

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private SpecClient specClient;


    public SearchResult page(SearchRequest searchRequest) {


        String key = searchRequest.getKey();

        //看是否有查询的条件
        if (!StringUtils.isNotBlank(key)){
            return null;
        }


        //自定义查询
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        // 通过sourceFilter设置返回的结果字段,我们只需要id、skus、subTitle
        queryBuilder.withSourceFilter(new FetchSourceFilter(
                new String[]{"id", "skus", "subTitle"}, null));


        //调用返回构造好的查询条件
        QueryBuilder query = buildBasicQueryWithFilter(searchRequest);

        //添加查询条件
        queryBuilder.withQuery(query);

        //添加分页条件，分页从0开始
        queryBuilder.withPageable(PageRequest.of(searchRequest.getPage()-1,searchRequest.getSize()));


        //由于categories和brands来自于聚合，所以要对数据进行聚合，由于每个商品一定有分类和品牌，所以不用判断

        // 1.3、聚合
        String categoryAggName = "category"; // 商品分类聚合名称
        String brandAggName = "brand"; // 品牌聚合名称

        //分类聚合使用cid3
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        //品牌聚合使用brandId
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));


        //执行查询获取分页的结果
        AggregatedPage<Goods> search = (AggregatedPage<Goods>) this.goodsRepository.search(queryBuilder.build());


        //从聚合结果中根据聚合名称获取聚合的结果
        LongTerms categoryTerms = (LongTerms) search.getAggregation(categoryAggName);


        //获取分类的所有的桶
        List<LongTerms.Bucket> categoryTermsBuckets = categoryTerms.getBuckets();

        //用来存放聚合而来的分类的信息
        List<Long> categoryIds = new ArrayList<>();

        categoryTermsBuckets.forEach(bucket ->categoryIds.add( bucket.getKeyAsNumber().longValue()));


        //根据得到的分类的id信息查询对应的分类的名称
        List<String> names = this.categoryClient.queryNameByIds(categoryIds);

        //扩展响应的结果
        List<Category> categories = new ArrayList<>();

        //手动根据id和name组装对象
        for (int i = 0; i < names.size(); i++) {
            Category c = new Category();
            c.setId(categoryIds.get(i));
            c.setName(names.get(i));
            categories.add(c);
        }

        //从聚合结果中根据聚合名称获取聚合的结果
        LongTerms brandTerms = (LongTerms) search.getAggregation(brandAggName);

        //从品牌的聚合结果中获取品牌的桶信息
        List<LongTerms.Bucket> brandTermsBuckets = brandTerms.getBuckets();

        //用来存放品牌id的聚合的结果
        List<Long> brandIds = new ArrayList<>();
        brandTermsBuckets.forEach(bucket -> {
            //把具体的品牌的id的值存入brandIds集合中
            brandIds.add(bucket.getKeyAsNumber().longValue());
        });


        List<Brand> brands = new ArrayList<>();

        //TODO 需要根据品牌的id集合转换为品牌的对象集合


        //循环根据品牌的id查询对应的品牌对象信息并转载到brands中
        brandIds.forEach(brandId-> brands.add(this.brandClient.queryBrandById(brandId)));

        List<Map<String,Object>> specs = null;

        if (categories.size()==1){
            //如果分类唯一，则要获取 当前 分类 中所有的可以搜索的规格参数的过滤项
            //传入的为分类的id
            specs = getSpecs(categories.get(0).getId(),query);
        }


        return new SearchResult(search.getTotalElements(),new Long(search.getTotalPages()),search.getContent(),categories,brands,specs);
    }

    //用来查询获取我们的规格参数的信息
    private List<Map<String, Object>> getSpecs(Long cid,QueryBuilder query) {

        List<Map<String,Object>> specs = new ArrayList<>();


        //1,首先要根据分类获取当前分类id对应的所有的可搜索的规格参数

        //获取到所有的可搜索规格参数
        List<SpecParam> specParams = this.specClient.querySpecParam(null, cid, true,null);

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        //规格参数的过滤聚合要根据查询条件而来
        queryBuilder.withQuery(query);


        //循环添加聚合条件
        specParams.forEach(specParam -> {
            //可搜索规格参数的名称,也就是聚合的名称
            String name = specParam.getName();

            queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs."+name+".keyword"));

        });

        //执行查询并获取到聚合的结果
        AggregatedPage<Goods> search = (AggregatedPage<Goods>) this.goodsRepository.search(queryBuilder.build());


        //循环的解析聚合条件
        specParams.forEach(specParam -> {
            //可搜索规格参数的名称
            String name = specParam.getName();
            StringTerms aggregation = (StringTerms) search.getAggregation(name);

            //获取到某个聚合的桶信息
            List<StringTerms.Bucket> buckets = aggregation.getBuckets();

            //获取聚合的结果
            List<String> results = new ArrayList<>();
            buckets.forEach(bucket -> {
                results.add(bucket.getKeyAsString());
            });

            //由于前台的filters数组中需要的对象为key，value，所以后台提供
            Map<String,Object> spec = new HashMap<>();
            spec.put("k",name);
            spec.put("options",results);

            specs.add(spec);
        });

        return specs;
    }

    // 构建基本查询条件
    private QueryBuilder buildBasicQueryWithFilter(SearchRequest request) {
        //创建布尔查询
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        // 基本查询条件
        queryBuilder.must(QueryBuilders.matchQuery("all", request.getKey()).operator(Operator.AND));

        // 过滤条件构建器
        BoolQueryBuilder filterQueryBuilder = QueryBuilders.boolQuery();
        // 整理过滤条件
        Map<String, String> filter = request.getFilter();
        for (Map.Entry<String, String> entry : filter.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            // 商品分类和品牌可以直接查询不需要拼接
            if (key != "cid3" && key != "brandId") {
                key = "specs." + key + ".keyword";
            }
            // 字符串类型，进行term查询
            filterQueryBuilder.must(QueryBuilders.termQuery(key, value));
        }
        // 添加过滤条件
        queryBuilder.filter(filterQueryBuilder);
        return queryBuilder;
    }
}
