package org.aqframework.example.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch.cluster.PutClusterSettingsResponse;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.json.JsonData;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Map;
import java.util.Random;

@SpringBootTest
class IlmIndexTests {
    @Resource
    ElasticsearchClient client;

    @SneakyThrows
    @Test
    void test() {
        bulkInsert();
    }

    public void updatePollInterval(String interval) throws Exception {
        PutClusterSettingsResponse response = client.cluster().putSettings(req -> req
                .persistent("indices.lifecycle.poll_interval", JsonData.of(Time.of(t -> t.time(interval))))
        );
        System.out.println(response);

    }

    @SneakyThrows
    public void bulkInsert() {
        updatePollInterval("10s");
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            BulkRequest.Builder bulkRequest = new BulkRequest.Builder();
            for (int j = 0; j < 10; j++) {
                Map<String, ? extends Serializable> item = Map.of("name", RandomStringUtils.randomAlphanumeric(5, 10), "age", random.nextInt(18, 100));
                bulkRequest.operations(op -> op.index(idx -> idx
                        .index("test1-alias")
                        .requireAlias(true)
                        .document(item)
                ));
            }
            BulkResponse response = client.bulk(bulkRequest.build());
            System.out.println(response);
            Thread.sleep(2000);
        }
    }
}
