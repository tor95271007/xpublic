package org.aqframework.example.es.nba;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class NbaMatchService {

    @Autowired
    private ElasticsearchClient client;

    public void bulkInsert(List<NbaMatch> items) throws IOException {
        BulkRequest.Builder bulkRequest = new BulkRequest.Builder();

        for (NbaMatch item : items) {
            bulkRequest.operations(op -> op.index(idx -> idx
                    .index(NbaMatch.INDEX_NAME)
                    .id(item.getId())
                    .document(item)
            ));
        }

        BulkResponse response = client.bulk(bulkRequest.build());
        log.info("bulkInsert done {}", response);
    }

    @SneakyThrows
    public List<NbaMatch> search(String key) {
        SearchResponse<NbaMatch> response = client.search(SearchRequest.of(req -> req
                .index(NbaMatch.INDEX_NAME)
                .query(q -> q.bool(
                        b -> b
                                .must(m -> m.multiMatch(
                                        match -> match
                                                .fields(NbaMatch.Props.playerName, NbaMatch.Props.teamName)
                                                .query(key)
                                ))
//                                .must(m -> m.range(
//                                        r -> r.field(NbaMatch.Props.eventDate)
//                                                .gte(JsonData.of("2023-01-01"))
//                                                .lte(JsonData.of("2025-01-01"))
//                                                .format("yyyy-MM-dd")
//                                ))
                ))
                .size(1000)
        ), NbaMatch.class);

        HitsMetadata<NbaMatch> hits = response.hits();
        return hits.hits().stream().map(Hit::source).toList();

    }
}