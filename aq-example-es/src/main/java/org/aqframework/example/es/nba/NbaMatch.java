package org.aqframework.example.es.nba;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NbaMatch {
    private String id;
    private String teamName;
    private String playerName;
    private String sportType;
    private LocalDateTime eventDate;
    private String location;

    public static final String INDEX_NAME = "nba_match";

    public interface Props {
        String id = "id";
        String teamName = "teamName";
        String playerName = "playerName";
        String sportType = "sportType";
        String eventDate = "eventDate";
        String location = "location";
    }
}