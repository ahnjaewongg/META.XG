package com.lolservice.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
public class MatchDTO {
    private MetadataDTO metadata;
    private InfoDTO info;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MetadataDTO {
        private String matchId;
        private List<String> participants;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InfoDTO {
        private long gameCreation;
        private long gameDuration;
        private String gameVersion;
        private int queueId;
        private List<ParticipantDTO> participants;
        private List<TeamDTO> teams;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ParticipantDTO {
        private String puuid;
        private String summonerName;
        private String championName;
        private Integer championId;
        private String teamPosition;
        private boolean win;
        private int kills;
        private int deaths;
        private int assists;
        private int totalDamageDealtToChampions;
        private int visionScore;
        private int goldEarned;

        private int doubleKills;
        private int tripleKills;
        private int quadraKills;
        private int pentaKills;
        private boolean firstBloodKill;
        private int turretKills;
        private int totalMinionsKilled;

        private int neutralMinionsKilled;

        private int item0;
        private int item1;
        private int item2;
        private int item3;
        private int item4;
        private int item5;
        private int item6;

        private int primaryRune;
        private int secondaryRune;
        private int spell1;
        private int spell2;

        private Long gameDuration;
        private LocalDateTime gameCreation;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TeamDTO {
        private List<BanDTO> bans;
        private int teamId;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BanDTO {
        private int championId;
        private int pickTurn;
    }
}