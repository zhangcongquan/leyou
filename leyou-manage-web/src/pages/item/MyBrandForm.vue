<template>
  <div>

    <!--    ref 表示把对应的对象关联给vue对象，并且vue对象可以通过关联的key来获取对应的对象-->
    <v-form v-model="valid" ref="haha">
      <v-text-field label="品牌名称" v-model="brand.name" required :rules="nameRules"></v-text-field>
      <v-text-field label="品牌首字母" v-model="brand.letter" required :rules="letterRules"></v-text-field>


      <v-layout row>
        <v-flex xs3>
          <span style="font-size: 16px; color: #444">品牌LOGO：</span>
        </v-flex>
        <v-flex>
          <v-upload
            v-model="brand.image"
            url="/upload/image"
            :multiple="false"
            :pic-width="250"
            :pic-height="90"
          />
        </v-flex>
      </v-layout>

      <v-cascader
        url="/item/category/list"
        multiple
        required
        v-model="brand.categories"
        label="请选择商品分类"/>

      <v-layout class="my-4" row>
        <v-spacer/>
        <v-btn @click="submit" color="primary">提交</v-btn>
        <v-btn @click="clear">重置</v-btn>
      </v-layout>

    </v-form>

  </div>
</template>

<script>
  export default {
    name: "MyBrandForm",

    props: {
      oldBrand: {
        type: Object,
        default: {}
      },
      edit:{
        type:Boolean,
        default: false
      }
    },
    data() {
      return {
        valid: false, //表示表单的校验结果，如果表单校验通过返回true
        brand: {
          categories: [],
          name: "",
          letter: "",
          image: ""
        },
        nameRules: [
          v => !!v || "品牌名称不能为空", //多个校验逻辑要不通过最多同时只能有一个，校验要么返回true，要么返回消息
          v => v.length > 1 || "品牌名称至少2位"
        ],
        letterRules: [
          v => !!v || "首字母不能为空",
          v => /^[A-Z]{1}$/.test(v) || "品牌字母只能是A~Z的大写字母"
        ]
      }
    },
    methods: {
      clear() {

        this.$refs.haha.reset();

        //手动清空分类中的数据
        this.brand.categories = [];

      },
      submit() {

        //如果if通过了表示校验通过了
        if (this.$refs.haha.validate()) {
          //提交表单的数据

          //解构分类数组
          let {categories, ...others} = this.brand;

          //遍历分类对象，获取到每个对象的id
          categories = categories.map(category => category.id);

          //把分类id数组转为分类的id的组合字符串
          others.cids = categories.join();

          //需要把others转为查询字符串

          this.$http({
            method: this.edit ? 'put' : 'post', // 动态判断是POST还是PUT
            url: '/item/brand',
            data: this.$qs.stringify(others)
          })
            .then(resp => {
              this.$message.success((this.edit?"修改":"新增")+"品牌成功");

              this.$emit("close");
            }).catch(resp => {
            console.log((this.edit?"修改":"新增")+"品牌失败")
          });


        } else {
          this.$message.error("表单数据有误，请纠正后提交")
        }
      }
    },

    watch:{
      oldBrand:{
        deep:true,
        handler(val){
          if(val){
            // 注意不要直接复制，否则这边的修改会影响到父组件的数据，copy属性即可
            this.brand =  Object.deepCopy(val)
          }else{
            // 为空，初始化brand
            this.brand = {
              name: '',
              letter: '',
              image: '',
              categories: [],
            }
          }
        }
      }
    }
  }
</script>

<style scoped>

</style>
