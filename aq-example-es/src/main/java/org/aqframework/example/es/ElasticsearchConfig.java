package org.aqframework.example.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@Configuration
public class ElasticsearchConfig {

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        // 自定义 RestClientBuilder
        RestClientBuilder builder = RestClient.builder(
                new HttpHost("localhost", 9200, "http")
        );

        // 设置超时时间
        builder.setRequestConfigCallback(requestConfigBuilder ->
                requestConfigBuilder
                        .setConnectTimeout(5000) // 连接超时（毫秒）
                        .setSocketTimeout(60000) // 套接字超时（毫秒）
        );

        // 设置连接池配置
        builder.setHttpClientConfigCallback(httpClientBuilder ->
                httpClientBuilder.setMaxConnTotal(100) // 最大连接数
                        .setMaxConnPerRoute(10) // 每个路由的最大连接数
        );

        // 创建 RestClient 和传输层
        RestClient restClient = builder.build();
        RestClientTransport transport = new RestClientTransport(restClient, jacksonJsonpMapper());

        // 创建 Elasticsearch 客户端
        return new ElasticsearchClient(transport);
    }

    @Bean
    public JacksonJsonpMapper jacksonJsonpMapper() {
        return new JacksonJsonpMapper(objectMapper());
    }

    @Bean
    public ObjectMapper objectMapper() {
        // 定义日期格式和时区
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                .withZone(TimeZone.getDefault().toZoneId());

        // 创建 ObjectMapper
        return JsonMapper.builder()
                .addModule(new JavaTimeModule()
                        .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter))
                        .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter)))
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES) // 忽略未知字段
                .build();

    }
}