package com.lolservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class AccountDTO {
    private String puuid;
    private String gameName;

    private String tagLine;

    private String summonerId;
    private String summonerName;
    private String queueType;
    private int leaguePoints;
    private String tier;
    private String rank;
    private int wins;
    private int losses;
}