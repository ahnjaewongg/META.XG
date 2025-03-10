package com.lolservice.dto;

import java.util.List;
import lombok.Data;

@Data
public class CurrentGameDTO {
    private String id;
    private Metadata metadata;
    private GameData gameData;
    private List<CurrentGameParticipant> participants;

    @Data
    public static class Metadata {
        private String gameVersion;
    }

    @Data
    public static class GameData {
        private String gameMode;
        private String gameType;
        private long gameStartTime;
        private long gameLength;
        private int mapId;
    }

    @Data
    public static class CurrentGameParticipant {
        private String summonerId;
        private String summonerName;
        private String puuid;
        private int championId;
        private String championName;
        private int teamId;
        private long spell1Id;
        private long spell2Id;
    }
}