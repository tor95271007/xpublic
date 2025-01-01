package org.aqframework.example.es;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

class NbaPlayersTests {

    @SneakyThrows
    @Test
    void test() throws IOException {
        String nbaPlayersPath = "/Users/tor/IdeaProjects/zgy/aq-framework/aq-examples/aq-example-es/src/test/resources/nba_players2.txt";
        List<String> nbaPlayers = new ArrayList<>();
        for (int i = 97; i < 97 + 26; i++) {
            char c = (char) i;
            nbaPlayers.addAll(getPlayers(String.valueOf(c)));
            Thread.sleep(5000);
        }
        Files.writeString(Paths.get(nbaPlayersPath), String.join(System.lineSeparator(), nbaPlayers));
    }

    List<String> getPlayers(String letter) throws IOException {
        List<String> nbaPlayers = new ArrayList<>();
        String url = "https://www.basketball-reference.com/players/" + letter;
        Elements rows = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36").get().getElementById("players").select("tbody tr");
        for (Element row : rows) {
            List<String> item = row.select("th,td").eachText();
            nbaPlayers.add(item.get(0));
        }
        return nbaPlayers;
    }

    @Test
    void testAToZ() {
        for (int i = 97; i < 97 + 26; i++) {
            char c = (char) i;
            System.out.println(c);
        }
    }

    @Test
    void testNextInt() {
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 100; i++) {
            System.out.println(now.plusSeconds(new Random().nextInt(-100000, 100000)));
        }
    }

    @Test
    void testTimeZone() {
        System.out.println(TimeZone.getDefault().toZoneId());
        System.out.println(LocalDateTime.now(TimeZone.getDefault().toZoneId()));
    }

    @Test
    void testKabana() throws IOException {
        String path = "/Users/tor/IdeaProjects/zgy/aq-framework/aq-examples/aq-example-es/src/test/resources/Untitled discover search.csv";
        List<String> lines = Files.readAllLines(Paths.get(path));
        int count = 0;
        for (String line : lines) {
            if (line.trim().toLowerCase().contains("charl")) {
                count++;
            }
        }
        System.out.println(count);
    }
}
