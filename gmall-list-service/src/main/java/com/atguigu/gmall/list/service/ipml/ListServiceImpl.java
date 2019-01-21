package com.atguigu.gmall.list.service.ipml;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParam;
import com.atguigu.gmall.bean.SkuLsResult;
import com.atguigu.gmall.constants.ElasticSearchConst;
import com.atguigu.gmall.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ListServiceImpl implements ListService {

    @Autowired
    JestClient jestClient;


    @Override
    public SkuLsResult getSkuLsInfos(SkuLsParam skuLsParam) {


        //通过传入的参数，设计dsl查询语句
        String dsl = getMyDsl(skuLsParam);
        System.out.println(dsl);
        //创建查询语句
        Search search = new Search.Builder(dsl).addIndex(ElasticSearchConst.index_name_gmall).addType(ElasticSearchConst.type_name_gmall).build();
        //执行查询语句
        SearchResult searchResult = null;
        try {
            searchResult = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SkuLsResult skuLsResult = makeResultForSearch(skuLsParam, searchResult);

        return skuLsResult;
    }

    private SkuLsResult makeResultForSearch(SkuLsParam skuLsParam, SearchResult searchResult) {

        SkuLsResult skuLsResult = new SkuLsResult();
        List<SkuLsInfo> skuLsInfoList = new ArrayList<>(skuLsParam.getPageSize());
        //获取查询结果的条目数
        int total = new Integer(searchResult.getTotal().toString());
        //判断查询结果条目数
        if (total > 0) {
            //获取查询到的数据
            List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);
            //遍历数据，封装为skuInfo的list集合
            for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {

                SkuLsInfo source = hit.source;

                if (hit.highlight != null && hit.highlight.size() > 0) {
                    List<String> list = hit.highlight.get("skuName");
                    //把带有高亮标签的字符串替换skuName
                    String skuNameHl = list.get(0);
                    source.setSkuName(skuNameHl);
                }
                skuLsInfoList.add(source);
            }
            skuLsResult.setSkuLsInfoList(skuLsInfoList);

            skuLsResult.setTotal(total);
        }

        //取记录个数并计算出总页数
        int totalPage = (total + skuLsParam.getPageSize() - 1) / skuLsParam.getPageSize();

        skuLsResult.setTotal(totalPage);

        return skuLsResult;

    }

    private String getMyDsl(SkuLsParam skuLsParam) {

        String catalog3Id = skuLsParam.getCatalog3Id();
        String keyword = skuLsParam.getKeyword();
        String[] valueId = skuLsParam.getValueId();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //联合查询
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //通过三级分类id过滤查询
        if (catalog3Id != null && catalog3Id.length() > 0) {
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("catalog3Id", catalog3Id);
            boolQueryBuilder.filter(matchQueryBuilder);
        }
        //通过平台属性值过滤查询
        if (valueId != null && valueId.length > 0) {
            for (String id : valueId) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", id);
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }
        //通过关键字匹配查询
        if (StringUtils.isNoneBlank(keyword)) {
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", keyword);
            boolQueryBuilder.must(matchQueryBuilder);
        }
//        聚合
//        TermsBuilder groupby_attr = AggregationBuilders.terms("groupby_attr").field("skuAttrValueList.valueId");
//        searchSourceBuilder.aggregation(groupby_attr);

        //高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.field("skuName");
        highlightBuilder.postTags("</span>");

        searchSourceBuilder.highlight(highlightBuilder);
        searchSourceBuilder.query(boolQueryBuilder);

        //分页查询数量
        skuLsParam.setPageSize(20);
        int from = (skuLsParam.getPageNo() - 1) * skuLsParam.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(skuLsParam.getPageSize());

//        //排序
//        searchSourceBuilder.sort("hotScore", SortOrder.DESC);

        return searchSourceBuilder.toString();
    }
}
