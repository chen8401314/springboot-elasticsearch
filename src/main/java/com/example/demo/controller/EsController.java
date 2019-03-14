package com.example.demo.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.dto.EsModel;
import com.example.demo.dto.EsPage;
import com.example.demo.util.ElasticsearchUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

/**
 * @Author: cx
 * @Description:
 * @Date: Created in 11:23 2018/11/6
 * @Modified by:
 */
@Slf4j
@RestController("EsController")
@RequestMapping(value = "/es", produces = { APPLICATION_JSON_UTF8_VALUE })
@Api(value = "EsController", description = "es相关接口", produces = MediaType.ALL_VALUE)
public class EsController {

    /**
     * 测试索引
     */
    private String indexName = "test_index";

    /**
     * 类型
     */
    private String esType = "external";

    /**
     * 创建索引
     * @return
     */
    @GetMapping("/createIndex")
    public String createIndex() {
        if (!ElasticsearchUtil.isIndexExist(indexName)) {
            ElasticsearchUtil.createIndex(indexName);
        } else {
            return "索引已经存在";
        }
        return "索引创建成功";
    }

    /**
     * 插入记录
     *
     * @return
     */
    @GetMapping("/insertJson")
    public String insertJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", new Date());
        jsonObject.put("age", 25);
        jsonObject.put("name", "j-" + new Random(100).nextInt());
        jsonObject.put("date", new Date());
        return ElasticsearchUtil.addData(jsonObject, indexName, esType, jsonObject.getString("id"));
    }

    /**
     * 插入记录
     *
     * @return
     */
    @PostMapping("/insertModel")
    public String insertModel(@RequestBody EsModel esModel) {
        esModel.setId(UUID.randomUUID().toString().replaceAll("-", "").toUpperCase());
        esModel.setDate(new Date());
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(esModel);
        return ElasticsearchUtil.addData(jsonObject, indexName, esType, esModel.getId());
    }

    /**
     * 删除记录
     *
     * @return
     */
    @DeleteMapping("/delete")
    public String delete(String id) {
        if (StringUtils.isNotBlank(id)) {
            ElasticsearchUtil.deleteDataById(indexName, esType, id);
            return "删除id=" + id;
        } else {
            return "id为空";
        }
    }

    /**
     * 更新数据
     *
     * @return
     */
    @GetMapping("/update")
    public String update(String id) {
        if (StringUtils.isNotBlank(id)) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", id);
            jsonObject.put("age", 31);
            jsonObject.put("name", "修改");
            jsonObject.put("date", new Date());
            ElasticsearchUtil.updateDataById(jsonObject, indexName, esType, id);
            return "id=" + id;
        } else {
            return "id为空";
        }
    }

    /**
     * 更新数据
     *
     * @return
     */
    @PostMapping("/updateByModel")
    public String updateByModel(@RequestBody EsModel esModel) {
        if (StringUtils.isNotBlank(esModel.getId())) {
            JSONObject jsonObject = (JSONObject) JSONObject.toJSON(esModel);
            ElasticsearchUtil.updateDataById(jsonObject, indexName, esType, esModel.getId());
            return "id=" + (esModel.getId());
        } else {
            return "id为空";
        }
    }

    /**
     * 获取数据
     *
     * @param id
     * @return
     */
    @GetMapping("/getData")
    public String getData(String id) {
        if (StringUtils.isNotBlank(id)) {
            Map<String, Object> map = ElasticsearchUtil.searchDataById(indexName, esType, id, null);
            return JSONObject.toJSONString(map);
        } else {
            return "id为空";
        }
    }

    /**
     * 查询数据
     * 模糊查询
     *
     * @return
     */
    /**
     * 模糊查询
     * @param matchPhrase 完全匹配 true 模糊匹配 false
     * @param filedName 字段名称 例如“name”
     * @param matchStr 匹配字符 分词用“-”隔开
     * @return
     */
    @PostMapping("/queryMatchData")
    public String queryMatchData(@RequestParam Boolean matchPhrase,@RequestParam String filedName,@RequestParam String matchStr) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (matchPhrase) {
            //不进行分词搜索
            boolQuery.must(QueryBuilders.matchPhraseQuery(filedName, matchStr));
        } else {
            boolQuery.must(QueryBuilders.matchQuery(filedName, matchStr));
        }
        List<Map<String, Object>> list = ElasticsearchUtil.
                searchListData(indexName, esType, boolQuery, 10, filedName, null, filedName);
        return JSONObject.toJSONString(list);
    }

    /**
     * 通配符查询数据
     * 通配符查询 ?用来匹配1个任意字符，*用来匹配零个或者多个字符
     *
     * @return
     */
    @GetMapping("/queryWildcardData")
    public String queryWildcardData() {
        QueryBuilder queryBuilder = QueryBuilders.wildcardQuery("name.keyword", "j-?466");
        List<Map<String, Object>> list = ElasticsearchUtil.searchListData(indexName, esType, queryBuilder, 10, null, null, null);
        return JSONObject.toJSONString(list);
    }

    /**
     * 正则查询
     *
     * @return
     */
    @GetMapping("/queryRegexpData")
    public String queryRegexpData() {
        QueryBuilder queryBuilder = QueryBuilders.regexpQuery("name.keyword", "m--[0-9]{1,11}");
        List<Map<String, Object>> list = ElasticsearchUtil.searchListData(indexName, esType, queryBuilder, 10, null, null, null);
        return JSONObject.toJSONString(list);
    }

    /**
     * 查询数字范围数据
     *
     * @return
     */
    @GetMapping("/queryIntRangeData")
    public String queryIntRangeData() {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.must(QueryBuilders.rangeQuery("age").from(21)
                .to(25));
        List<Map<String, Object>> list = ElasticsearchUtil.searchListData(indexName, esType, boolQuery, 10, null, null, null);
        return JSONObject.toJSONString(list);
    }

    /**
     * 查询日期范围数据
     *
     * @return
     */
    @GetMapping("/queryDateRangeData")
    public String queryDateRangeData() {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.must(QueryBuilders.rangeQuery("date").from("2018-04-25T08:33:44.840Z")
                .to("2030-04-25T10:03:08.081Z"));
        List<Map<String, Object>> list = ElasticsearchUtil.searchListData(indexName, esType, boolQuery, 10, null, null, null);
        return JSONObject.toJSONString(list);
    }

    /**
     * 查询分页
     *
     * @param startPage 第几条记录开始 从0开始
     *
     * @param pageSize  每页大小
     * @return
     */
    @GetMapping("/queryPage")
    public String queryPage(String startPage, String pageSize) {
        if (StringUtils.isNotBlank(startPage) && StringUtils.isNotBlank(pageSize)) {
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            boolQuery.must(QueryBuilders.rangeQuery("date").from("2018-04-25T08:33:44.840Z")
                    .to("2019-04-25T10:03:08.081Z"));
            EsPage list = ElasticsearchUtil.searchDataPage(indexName, esType, Integer.parseInt(startPage), Integer.parseInt(pageSize), boolQuery, null, null, null);
            return JSONObject.toJSONString(list);
        } else {
            return "startPage或者pageSize缺失";
        }
    }
}