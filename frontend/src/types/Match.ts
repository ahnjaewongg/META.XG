export interface Match {
    metadata: {
        matchId: string;
        participants: string[];
    };
    info: {
        gameCreation: number;
        gameDuration: number;
        gameMode: string;
        queueId: number;
        participants: {
            puuid: string;
            summonerName: string;
            championId: number;
            championName: string;
            kills: number;
            deaths: number;
            assists: number;
            win: boolean;
            item0: number;
            item1: number;
            item2: number;
            item3: number;
            item4: number;
            item5: number;
            item6: number;
            totalMinionsKilled: number;
            neutralMinionsKilled: number;
            goldEarned: number;
        }[];
    };
} 