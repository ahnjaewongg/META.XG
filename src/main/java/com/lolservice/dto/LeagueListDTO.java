package com.lolservice.dto;

import lombok.Data;
import java.util.List;
import lombok.ToString;

@Data
@ToString
public class LeagueListDTO {
    private String leagueId;
    private String tier;
    private String queue;
    private String name;
    private List<LeagueEntryDTO> entries;
}