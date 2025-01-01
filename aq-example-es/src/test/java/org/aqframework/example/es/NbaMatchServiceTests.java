package org.aqframework.example.es;

import lombok.SneakyThrows;
import org.aqframework.example.es.nba.NbaMatch;
import org.aqframework.example.es.nba.NbaMatchIndexService;
import org.aqframework.example.es.nba.NbaMatchService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@SpringBootTest
class NbaMatchServiceTests {
    @Resource
    NbaMatchIndexService indexService;

    @Resource
    NbaMatchService service;

    @SneakyThrows
    @Test
    void testCreateIndex() {
        indexService.createIndex();
    }

    @SneakyThrows
    @Test
    void testBulkInsert() throws IOException {
        indexService.recreateIndex();

        String basePath = "/Users/tor/IdeaProjects/zgy/aq-framework/aq-examples/aq-example-es/src/test/resources";
        String playersPath = basePath + "/nba_players.txt";
        String teamsPath = basePath + "/nba_teams.txt";
        List<String> players = load(playersPath);
        List<String> teams = load(teamsPath);
        Random random = new Random();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            List<NbaMatch> items = new ArrayList<>();
            for (int j = 0; j < 1000; j++) {
                String player = players.get(random.nextInt(players.size()));
                String team = teams.get(random.nextInt(teams.size()));
                NbaMatch item = new NbaMatch();
                item.setId(UUID.randomUUID().toString().replace("-", ""));
                item.setSportType("Basketball");
                item.setLocation("Los Angeles, USA");
                item.setEventDate(LocalDateTime.now().plusSeconds(random.nextInt(-100000, 0)));
                item.setTeamName(team);
                item.setPlayerName(player);
                items.add(item);
            }
            service.bulkInsert(items);
            Thread.sleep(500);
        }
        System.out.println(System.currentTimeMillis() - start);
    }

    @SneakyThrows
    List<String> load(String path) {
        List<String> lines = Files.readAllLines(Paths.get(path));
        return lines.stream().filter(v -> !v.startsWith("#") && !v.trim().isEmpty()).map(v -> v.trim().split(",")[0]).toList();
    }

    @SneakyThrows
    @Test
    void testRecreateIndex() {
        indexService.recreateIndex();
    }

    @Test
    void testSearch() {
        List<NbaMatch> items = service.search("Warriors");
        System.out.println(items.size());
        List<NbaMatch> sorted = items.stream().sorted(Comparator.comparing(NbaMatch::getEventDate).reversed()).toList();
        for (NbaMatch item : sorted) {
            System.out.println(item);
        }
    }
    // 447
    // 223

    @Test
    void testReindexData() {
        indexService.reindexData();
    }
}
