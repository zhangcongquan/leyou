package com.leyou.item.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.item.pojo.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.common.base.insert.InsertSelectiveMapper;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SpuDetailMapper spuDetailsMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    private Logger logger = LoggerFactory.getLogger(GoodsService.class);

    /**
     * 四个参数有两个是分页参数
     *
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    public PageResult<SpuBo> querySpuByPage(String key, Boolean saleable, Integer page, Integer rows) {

        //使用分页助手来实现分页
        PageHelper.startPage(page, rows);


        //以下要根据查询的key来封装动态的查询条件，查询的数据库字段为spu的title


        Example example = new Example(Spu.class);//动态的拼接条件

        //条件封装对象
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(key)) {

            criteria.andLike("title", "%" + key + "%");
        }

        if (null != saleable) {//当salable不为空，要么是false，要么是true
            criteria.andEqualTo("saleable", saleable);
        }

        //根据动态sql执行查询，返回spu的集合
        Page<Spu> spus = (Page<Spu>) this.spuMapper.selectByExample(example);


        List<Spu> spusResult = spus.getResult();

        //最终封装好的spuBo数据
        List<SpuBo> spuBos = new ArrayList<>();

        //把查询到的spu转换为spuBo
        spusResult.forEach(spu -> {
            SpuBo spuBo = new SpuBo();
            //对应属性拷贝值，相当于spuBo.setName(spu.getName());
            BeanUtils.copyProperties(spu, spuBo);

            Brand brand = this.brandService.queryBrandById(spu.getBrandId());

            spuBo.setBname(brand.getName());

            //根据分类的id查询，对应的分类的名称
            List<String> names = this.categoryService.queryNamesByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));

            //StringUtils.join()//把数组的内容进行拼接操作，类似于js中的join
            spuBo.setCname(StringUtils.join(names, "/"));

            spuBos.add(spuBo);
        });

        //根据封装返回的spu的集合再次封装成pageResult 分页结果对象

        return new PageResult<>(spus.getTotal(), new Long(spus.getPages()), spuBos);
    }

    @Transactional
    public void addGoods(SpuBo spuBo) {

        //spuBo包含了，spu，spuDetail，sku，skuStock

        //spu 还缺4个值，salable,valid,createtime,updatetitme
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setCreateTime(new Date());
        spuBo.setLastUpdateTime(spuBo.getCreateTime());


        //保存spu，主键自动回显
        this.spuMapper.insertSelective(spuBo);


        //保存spuDetail，由于缺失spuId，上面保存完后会返回id，所以

        spuBo.getSpuDetail().setSpuId(spuBo.getId());
        //保存spuDetail
        this.spuDetailsMapper.insertSelective(spuBo.getSpuDetail());


        //处理sku
        List<Sku> skus = spuBo.getSkus();

        skus.forEach(sku -> {
            sku.setSpuId(spuBo.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());

            //保存sku,sku的主键回显
            this.skuMapper.insertSelective(sku);

            //保存sku的同时保存库存的信息
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());

            this.stockMapper.insertSelective(stock);
        });

        sendMessage(spuBo.getId(),"insert");
    }

    public SpuDetail querySpuDetailBySpuId(Long spuId) {
        return this.spuDetailsMapper.selectByPrimaryKey(spuId);
    }

    public List<Sku> querySkuBySpuId(Long spuId) {

        Sku record = new Sku();
        record.setSpuId(spuId);

        //查询sku
        List<Sku> skus = this.skuMapper.select(record);

        //在查询sku时同步查询stock
        skus.forEach(sku -> {
            //查询stock
            //sku的id就是stock表的主键，所以直接查询
            sku.setStock(this.stockMapper.selectByPrimaryKey(sku.getId()).getStock());
        });

        return skus;
    }

    @Transactional
    public void updateGoods(SpuBo spuBo) {

        //由于spu的数据都来自于前台，所以直接修改
        spuBo.setLastUpdateTime(new Date());

        this.spuMapper.updateByPrimaryKeySelective(spuBo);

        //对spuDetail也需要做直接修改
        this.spuDetailsMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());

        //查询到所有的旧的sku

        Sku record = new Sku();
        record.setSpuId(spuBo.getId());

        //根据spuId查询sku
        List<Sku> oldSkus = this.skuMapper.select(record);

        //新的sku
        List<Sku> newSkus = spuBo.getSkus();

        newSkus.forEach(newSku -> {

            //新增的
            if (null == newSku.getId()) {
                newSku.setSpuId(spuBo.getId());
                newSku.setCreateTime(new Date());
                newSku.setLastUpdateTime(newSku.getCreateTime());

                //保存sku,sku的主键回显
                this.skuMapper.insertSelective(newSku);

                //保存sku的同时保存库存的信息
                Stock stock = new Stock();
                stock.setSkuId(newSku.getId());
                stock.setStock(newSku.getStock());

                this.stockMapper.insertSelective(stock);
            } else {//有id就是要被修改的，同时要在oldSkus中排除，因为oldSkus中最后剩余的是要下架的

                newSku.setSpuId(spuBo.getId());

                //修改最后的修改事件,时间为当前时间
                newSku.setLastUpdateTime(new Date());

                //直接更新sku
                this.skuMapper.updateByPrimaryKeySelective(newSku);

                //保存sku的同时修改库存的信息
                Stock stock = new Stock();
                stock.setSkuId(newSku.getId());
                stock.setStock(newSku.getStock());

                this.stockMapper.updateByPrimaryKeySelective(stock);

                //TODO 要从oldSkus中排除被修改的sku，剩余的就是要下架的sku

                oldSkus.removeIf(oldSku -> oldSku.getId().longValue() == newSku.getId().longValue());


            }

        });

        //此时oldSkus未必有值（没有要下架的）
        oldSkus.forEach(oldSku -> {

            //设置最后修改的时间
            oldSku.setLastUpdateTime(new Date());

            //做下架处理
            oldSku.setEnable(false);

            //执行update，下架不改库存
            this.skuMapper.updateByPrimaryKeySelective(oldSku);
        });


        sendMessage(spuBo.getId(),"update");
    }

    public Spu querySpuById(Long id) {
        return this.spuMapper.selectByPrimaryKey(id);
    }


    private void sendMessage(Long id, String type) {
        // 发送消息
        try {
            this.amqpTemplate.convertAndSend("item." + type, id);
        } catch (Exception e) {
            logger.error("{}商品消息发送异常，商品id：{}", type, id, e);
        }
    }
}
