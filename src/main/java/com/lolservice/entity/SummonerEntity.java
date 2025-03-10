package com.lolservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "summoners")
@Data
public class SummonerEntity {
    @Id
    private String id;
    private String accountId;
    private String puuid;
    private String name;
    private int profileIconId;
    private long revisionDate;
    private long summonerLevel;

    @Column(name = "last_updated")
    private long lastUpdated;
}