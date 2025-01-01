package org.aqframework.example.es;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class ILMIndexServiceTests {
    @Resource
    ILMIndexService ilmIndexService;

    @SneakyThrows
    @Test
    void test() {
        ilmIndexService.createILMPolicy();
    }

    @SneakyThrows
    @Test
    void testCreateIndex() {
        ilmIndexService.createIndex();
    }


}
