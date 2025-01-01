package org.aqframework.example.es.nba;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Conflicts;
import co.elastic.clients.elasticsearch._types.OpType;
import co.elastic.clients.elasticsearch.core.ReindexRequest;
import co.elastic.clients.elasticsearch.core.ReindexResponse;
import co.elastic.clients.elasticsearch.indices.*;
import co.elastic.clients.json.JsonData;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class NbaMatchIndexService {

    @Autowired
    private ElasticsearchClient client;
    private static final String DATE_PATTERNS = "yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss||yyyy-MM-dd||yyyy/MM/dd HH:mm:ss||yyyy/MM/dd||MM-dd-yyyy||dd-MM-yyyy||yyyyMMdd||yyyy-MM-dd'T'HH:mm:ss.SSSZ||yyyy-MM-dd'T'HH:mm:ss.SSSXXX||epoch_millis||epoch_second";

    public void createIndex() throws IOException {
        // 构建索引创建请求
        CreateIndexRequest request = CreateIndexRequest.of(req -> req
                // index
                .index(NbaMatch.INDEX_NAME)
                // settings
                .settings(settings -> settings
                        // 设置刷新间隔
                        .refreshInterval(v -> v.time("1500ms"))
                        .analysis(analysis -> analysis
                                // 分析器
                                .analyzer("edge_ngram_analyzer",
                                        analyzer -> analyzer.custom(
                                                custom -> custom
                                                        .tokenizer("keyword")
                                                        .filter("pattern_replace_filter", "lowercase", "edge_ngram_filter")
                                        )
                                )
                                .analyzer("truncate_search_analyzer",
                                        analyzer -> analyzer.custom(
                                                custom -> custom
                                                        .tokenizer("keyword")
                                                        .filter("pattern_replace_filter", "lowercase", "truncate_search_filter")
                                        )
                                )
                                // 过滤器
                                .filter("edge_ngram_filter",
                                        filter -> filter.definition(
                                                f -> f.edgeNgram(
                                                        v -> v.minGram(1).maxGram(5)
                                                )
                                        )

                                )
                                .filter("pattern_replace_filter",
                                        filter -> filter.definition(
                                                f -> f.patternReplace(
                                                        v -> v.pattern("[^a-zA-Z0-9]").replacement("")
                                                )
                                        )

                                )
                                .filter("truncate_search_filter",
                                        filter -> filter.definition(
                                                f -> f.truncate(
                                                        v -> v.length(5)
                                                )
                                        )
                                )
                        )
                )
                // mappings
                .mappings(mappings -> mappings
                        .properties(NbaMatch.Props.teamName, prop -> prop.text(text -> text
                                .analyzer("edge_ngram_analyzer")
                                .searchAnalyzer("truncate_search_analyzer")))
                        .properties(NbaMatch.Props.playerName, prop -> prop.text(text -> text
                                .analyzer("edge_ngram_analyzer")
                                .searchAnalyzer("truncate_search_analyzer")))
                        .properties(NbaMatch.Props.eventDate, prop -> prop.date(date -> date.format(DATE_PATTERNS)))
                        .properties(NbaMatch.Props.location, prop -> prop.text(text -> text))
                        .properties(NbaMatch.Props.sportType, prop -> prop.keyword(keyword -> keyword))
                        .properties(NbaMatch.Props.id, prop -> prop.keyword(keyword -> keyword))
                )
        );

        // 执行索引创建
        CreateIndexResponse response = client.indices().create(request);
        log.info("createNbaMatchIndex done {}", response);
    }

    public void recreateIndex() throws IOException {
        client.indices().delete(d -> d.index(NbaMatch.INDEX_NAME));
        createIndex();
    }

    // 删除整个索引
    public void deleteIndex() throws Exception {
        DeleteIndexRequest request = DeleteIndexRequest.of(d -> d.index(NbaMatch.INDEX_NAME));
        DeleteIndexResponse response = client.indices().delete(request);
        log.info("deleteIndex done {}", response);
    }

    @SneakyThrows
    public void reindexData() {
        String targetIndex = NbaMatch.INDEX_NAME + "_copy";
        GetIndicesSettingsResponse settings = client.indices().getSettings(v -> v.index(targetIndex));
        String time = settings.get(targetIndex).settings().index().refreshInterval().time();
        updateRefreshInterval(targetIndex, "-1");

        // 创建 Reindex 请求
        ReindexRequest request = ReindexRequest.of(r -> r
                .source(s -> s.index(NbaMatch.INDEX_NAME)
                        .query(q -> q.range(m -> m
                                        .field("eventDate")
                                        .lte(JsonData.of("2024-12-30 21:45:00"))
                                        .gte(JsonData.of("2024-12-30 21:42:00"))
                                        .timeZone("Asia/Shanghai")
                                        .format(DATE_PATTERNS)
                                )
                        )
                )
                .dest(d -> d.index(targetIndex).opType(OpType.Create))
                .conflicts(Conflicts.Proceed)
                .maxDocs(100000L)

        );
        // 执行 Reindex
        ReindexResponse response = client.reindex(request);
        log.info("reindexData done {}", response);

        updateRefreshInterval(targetIndex, time);
    }

    public void updateRefreshInterval(String indexName, String refreshInterval) throws Exception {
        PutIndicesSettingsRequest request = PutIndicesSettingsRequest.of(settings -> settings
                .index(indexName)
                .settings(s -> s.refreshInterval(t -> t.time(refreshInterval)))
        );
        PutIndicesSettingsResponse response = client.indices().putSettings(request);
        log.info("updateRefreshInterval done {}", response);
    }
}