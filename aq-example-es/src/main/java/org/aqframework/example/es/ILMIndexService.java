package org.aqframework.example.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch.ilm.IlmPolicy;
import co.elastic.clients.elasticsearch.ilm.Phases;
import co.elastic.clients.elasticsearch.ilm.PutLifecycleRequest;
import co.elastic.clients.elasticsearch.ilm.PutLifecycleResponse;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.PutIndexTemplateResponse;
import co.elastic.clients.json.JsonData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Map;

@Service
public class ILMIndexService {
    private static final Logger log = LoggerFactory.getLogger(ILMIndexService.class);
    @Resource
    private ElasticsearchClient client;

    public void createILMPolicy() throws Exception {
        PutLifecycleResponse response = client.ilm().putLifecycle(
                PutLifecycleRequest.of(
                        p -> p.name("test1-ilm-policy")

                                .policy(
                                        IlmPolicy.of(ilmPolicy -> ilmPolicy.phases(
                                                        Phases.of(builder -> builder
                                                                .hot(
                                                                        phase -> phase.actions(
                                                                                JsonData.of(Map.of("rollover", Map.of(
                                                                                        "max_age", "10s",
                                                                                        "max_docs", 30L)
                                                                                ))
                                                                        )
                                                                )
                                                                .delete(
                                                                        phase -> phase.minAge(Time.of(t -> t.time("1m")))
                                                                                .actions(JsonData.of(
                                                                                        Map.of("delete", Collections.EMPTY_MAP)
                                                                                ))
                                                                )
                                                        )
                                                )
                                        )
                                )
                )
        );
        log.info("createILMPolicy done {}", response);
    }

    public void createTemplate() throws Exception {
        PutIndexTemplateResponse response = client.indices().putIndexTemplate(template -> template
                .name("test1-template")  // 模板名称
                .indexPatterns("test1-*")  // 匹配的索引模式
                .template(t -> t
                        .settings(settings -> settings
                                .index(index -> index.lifecycle(
                                        lifecycle -> lifecycle
                                                .name("test1-ilm-policy")
                                                .rolloverAlias("test1-alias")
                                ))
                        )
                        .mappings(mappings -> mappings
                                .properties("name", p -> p.text(text -> text))
                                .properties("age", p -> p.keyword(keyword -> keyword))
                        )
                )

        );
        log.info("createTemplate done {}", response);
    }

    public void createInitialIndex() throws Exception {
//        client.indices().delete(DeleteIndexRequest.of(i -> i.index("test1-000002")));
//        client.indices().delete(DeleteIndexRequest.of(i -> i.index("test1-alias")));

        CreateIndexResponse response = client.indices().create(CreateIndexRequest.of(req -> req
                .index("test1-001") // 初始索引名称
                .aliases("test1-alias", alias -> alias.isWriteIndex(true)) // 配置写入别名

        ));
        log.info("createInitialIndex done {}", response);
    }

    public void createIndex() throws Exception {
        createILMPolicy();
        createTemplate();
        createInitialIndex();
    }
}
