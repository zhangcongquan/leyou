<template>
  <div>

    <v-card>
      <!-- 卡片的头部 -->
      <v-card-title>
        <v-btn color="primary" @click="addBrand">新增</v-btn>

        <v-spacer/>
        <v-text-field label="搜索" append-icon="search" hide-details v-model="key"></v-text-field>
      </v-card-title>

      <v-divider/>
      <v-data-table
        :headers="headers"
        :pagination.sync="pagination"
        :items="brands"
        :total-items="totalBrands"
        :loading="loading"
        class="elevation-1"
      >
        <template slot="items" slot-scope="props">
          <td class="text-xs-center">{{ props.item.id }}</td>
          <td class="text-xs-center">{{ props.item.name }}</td>
          <td class="text-xs-center"><img :src="props.item.image"></td>
          <td class="text-xs-center">{{ props.item.letter }}</td>
          <td class="text-xs-center">
            <v-btn color="info" @click="editBrand(props.item)">编辑</v-btn>
            <v-btn color="warning"> 删除</v-btn>
          </td>
        </template>
      </v-data-table>
    </v-card>


    <v-dialog
      v-model="dialog"
      width="500"
      persistent
    >

      <v-card>
        <!--对话框的标题-->
        <v-toolbar dense dark color="primary">
          <v-toolbar-title>{{edit?"修改":"新增"}}品牌</v-toolbar-title>
          <v-spacer/>
          <v-btn @click="closeWindow" icon>
            <v-icon>close</v-icon>
          </v-btn>
        </v-toolbar>
        <!--对话框的内容，表单-->
        <v-card-text class="px-5">
          <my-brand-form @close="closeWindow" :oldBrand="oldBrand" :edit="edit"></my-brand-form>
        </v-card-text>
      </v-card>
    </v-dialog>


  </div>
</template>

<script>

  import myBrandForm from "./MyBrandForm"

  export default {
    components: {
      myBrandForm
    },
    name: "MyBrand",
    data() {
      return {
        dialog: false,
        loading: true,
        totalBrands: 999,
        pagination: {},
        headers: [
          {text: 'id', align: 'center', value: 'id'},
          {text: '名称', align: 'center', sortable: false, value: 'name'},
          {text: 'LOGO', align: 'center', sortable: false, value: 'image'},
          {text: '首字母', align: 'center', value: 'letter', sortable: true,},
          {text: '操作', align: 'center', value: 'id', sortable: false}
        ],
        brands: [],
        key: "",
        edit: false,
        oldBrand:{}
      }
    },
    mounted() { // 渲染后执行
      // 查询数据
      this.getDataFromServer();
    },
    methods: {
      getDataFromServer() { // 从服务的加载数的方法。


        this.$http.get("item/brand/page", {
          params: {
            page: this.pagination.page,// 当前页
            rows: this.pagination.rowsPerPage,// 每页大小
            sortBy: this.pagination.sortBy,// 排序字段
            desc: this.pagination.descending,// 是否降序
            key: this.key
          }
        }).then(({data}) => {
          //把响应的品牌信息数据赋值给brands
          this.brands = data.items;

          //把响应的总元素的个数赋值给total
          this.totalBrands = data.total;

          this.loading = false;
        }).catch(resp => {

          console.log("请求失败")
        })
      },

      addBrand() {

        //改为新增
        this.edit = false;
        //显示对话框
        this.dialog = true;

        // 把oldBrand变为null
        this.oldBrand = null;
      },
      closeWindow() {
        this.dialog = false;

        //关闭窗口时应该更新table中的数据
        //TODO 把这个每次都查询的请求变成 增删改成功后再查询
        this.getDataFromServer();
      },

      editBrand(oldBrand) {

        // 根据品牌信息查询商品分类
        this.$http.get("/item/category/bid/" + oldBrand.id)
          .then(({data}) => {

            this.edit = true;

            // 获取要编辑的brand
            this.oldBrand = oldBrand;
            // 回显商品分类
            this.oldBrand.categories = data;


            // 控制弹窗可见：
            this.dialog = true;
          })
      }
    },
    watch: {
      key() {
        this.getDataFromServer();
      },
      pagination: {
        deep: true,
        handler() {
          //当pagination，变化后重新发起请求
          this.getDataFromServer();
        }
      }
    }
  }
</script>

<style scoped>

</style>
