package com.lolservice.service;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.Arrays;
import java.util.Set;
import java.util.ArrayList;

import com.lolservice.entity.ChallengerParticipantEntity;
import com.lolservice.entity.ChampionStatisticsEntity;
import com.lolservice.repository.MatchRepository;
import com.lolservice.repository.ChampionStatisticsRepository;
import com.lolservice.repository.ChampionStatisticsPositionsRepository;
import com.lolservice.repository.ChampionBanRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class ChampionStatisticsService {
    private final MatchRepository matchRepository;
    private final ChampionStatisticsRepository championStatisticsRepository;
    private final ChampionBanRepository championBanRepository;
    private final Map<Integer, ChampionInfo> championData = new HashMap<>() {
        {
            // A로 시작하는 챔피언들
            put(266, new ChampionInfo("Aatrox", "아트록스", "TOP"));
            put(103, new ChampionInfo("Ahri", "아리", "MIDDLE"));
            put(84, new ChampionInfo("Akali", "아칼리", "MIDDLE,TOP"));
            put(166, new ChampionInfo("Akshan", "아크샨", "MIDDLE,TOP"));
            put(12, new ChampionInfo("Alistar", "알리스타", "SUPPORT"));
            put(32, new ChampionInfo("Amumu", "아무무", "JUNGLE,SUPPORT"));
            put(34, new ChampionInfo("Anivia", "애니비아", "MIDDLE"));
            put(1, new ChampionInfo("Annie", "애니", "MIDDLE,SUPPORT"));
            put(523, new ChampionInfo("Aphelios", "아펠리오스", "BOTTOM"));
            put(22, new ChampionInfo("Ashe", "애쉬", "BOTTOM,SUPPORT"));
            put(136, new ChampionInfo("AurelionSol", "아우렐리온 솔", "MIDDLE"));
            put(268, new ChampionInfo("Azir", "아지르", "MIDDLE"));

            // B로 시작하는 챔피언들
            put(432, new ChampionInfo("Bard", "바드", "SUPPORT"));
            put(200, new ChampionInfo("Belveth", "벨베스", "JUNGLE"));
            put(53, new ChampionInfo("Blitzcrank", "블리츠크랭크", "SUPPORT"));
            put(63, new ChampionInfo("Brand", "브랜드", "SUPPORT,MIDDLE"));
            put(201, new ChampionInfo("Braum", "브라움", "SUPPORT"));
            put(233, new ChampionInfo("Briar", "브라이어", "JUNGLE"));

            // C로 시작하는 챔피언들
            put(51, new ChampionInfo("Caitlyn", "케이틀린", "BOTTOM"));
            put(164, new ChampionInfo("Camille", "카밀", "TOP"));
            put(69, new ChampionInfo("Cassiopeia", "카시오페아", "MIDDLE"));
            put(31, new ChampionInfo("Chogath", "초가스", "TOP"));
            put(42, new ChampionInfo("Corki", "코르키", "MIDDLE"));

            // D로 시작하는 챔피언들
            put(122, new ChampionInfo("Darius", "다리우스", "TOP"));
            put(131, new ChampionInfo("Diana", "다이애나", "JUNGLE,MIDDLE"));
            put(119, new ChampionInfo("Draven", "드레이븐", "BOTTOM"));
            put(36, new ChampionInfo("DrMundo", "문도 박사", "TOP,JUNGLE"));

            // E로 시작하는 챔피언들
            put(245, new ChampionInfo("Ekko", "에코", "JUNGLE,MIDDLE"));
            put(60, new ChampionInfo("Elise", "엘리스", "JUNGLE"));
            put(28, new ChampionInfo("Evelynn", "이블린", "JUNGLE"));
            put(81, new ChampionInfo("Ezreal", "이즈리얼", "BOTTOM"));

            // F로 시작하는 챔피언들
            put(9, new ChampionInfo("Fiddlesticks", "피들스틱", "JUNGLE"));
            put(114, new ChampionInfo("Fiora", "피오라", "TOP"));
            put(105, new ChampionInfo("Fizz", "피즈", "MIDDLE"));

            // G로 시작하는 챔피언들
            put(3, new ChampionInfo("Galio", "갈리오", "MIDDLE,SUPPORT"));
            put(41, new ChampionInfo("Gangplank", "갱플랭크", "TOP"));
            put(86, new ChampionInfo("Garen", "가렌", "TOP"));
            put(150, new ChampionInfo("Gnar", "나르", "TOP"));
            put(79, new ChampionInfo("Gragas", "그라가스", "JUNGLE,TOP"));
            put(104, new ChampionInfo("Graves", "그레이브즈", "JUNGLE"));
            put(887, new ChampionInfo("Gwen", "그웬", "TOP"));

            // H로 시작하는 챔피언들
            put(120, new ChampionInfo("Hecarim", "헤카림", "JUNGLE"));
            put(74, new ChampionInfo("Heimerdinger", "하이머딩거", "MIDDLE,BOTTOM"));

            // I로 시작하는 챔피언들
            put(420, new ChampionInfo("Illaoi", "일라오이", "TOP"));
            put(39, new ChampionInfo("Irelia", "이렐리아", "TOP,MIDDLE"));
            put(427, new ChampionInfo("Ivern", "아이번", "JUNGLE"));

            // J로 시작하는 챔피언들
            put(40, new ChampionInfo("Janna", "잔나", "SUPPORT"));
            put(59, new ChampionInfo("JarvanIV", "자르반 4세", "JUNGLE"));
            put(24, new ChampionInfo("Jax", "잭스", "TOP,JUNGLE"));
            put(126, new ChampionInfo("Jayce", "제이스", "TOP,MIDDLE"));
            put(202, new ChampionInfo("Jhin", "진", "BOTTOM"));
            put(222, new ChampionInfo("Jinx", "징크스", "BOTTOM"));

            // K로 시작하는 챔피언들
            put(141, new ChampionInfo("Kaisa", "카이사", "BOTTOM"));
            put(38, new ChampionInfo("Kassadin", "카사딘", "MIDDLE"));
            put(55, new ChampionInfo("Katarina", "카타리나", "MIDDLE"));
            put(10, new ChampionInfo("Kayle", "케일", "TOP"));
            put(141, new ChampionInfo("Kayn", "케인", "JUNGLE"));
            put(85, new ChampionInfo("Kennen", "케넨", "TOP"));
            put(121, new ChampionInfo("Khazix", "카직스", "JUNGLE"));
            put(203, new ChampionInfo("Kindred", "킨드레드", "JUNGLE"));
            put(240, new ChampionInfo("Kled", "클레드", "TOP"));
            put(96, new ChampionInfo("KogMaw", "코그모", "BOTTOM"));

            // L로 시작하는 챔피언들
            put(7, new ChampionInfo("Leblanc", "르블랑", "MIDDLE"));
            put(64, new ChampionInfo("LeeSin", "리 신", "JUNGLE"));
            put(89, new ChampionInfo("Leona", "레오나", "SUPPORT"));
            put(876, new ChampionInfo("Lillia", "릴리아", "JUNGLE"));
            put(127, new ChampionInfo("Lissandra", "리산드라", "MIDDLE"));
            put(236, new ChampionInfo("Lucian", "루시안", "BOTTOM,MIDDLE"));
            put(117, new ChampionInfo("Lulu", "룰루", "SUPPORT"));
            put(99, new ChampionInfo("Lux", "럭스", "SUPPORT,MIDDLE"));

            // M로 시작하는 챔피언들
            put(54, new ChampionInfo("Malphite", "말파이트", "TOP,SUPPORT"));
            put(90, new ChampionInfo("Malzahar", "말자하", "MIDDLE"));
            put(57, new ChampionInfo("Maokai", "마오카이", "SUPPORT,TOP"));
            put(11, new ChampionInfo("MasterYi", "마스터 이", "JUNGLE"));
            put(21, new ChampionInfo("MissFortune", "미스 포츈", "BOTTOM"));
            put(62, new ChampionInfo("MonkeyKing", "오공", "TOP,JUNGLE"));
            put(82, new ChampionInfo("Mordekaiser", "모데카이저", "TOP"));
            put(25, new ChampionInfo("Morgana", "모르가나", "SUPPORT,MIDDLE"));

            // N로 시작하는 챔피언들
            put(518, new ChampionInfo("Naafiri", "나피리", "MIDDLE"));
            put(267, new ChampionInfo("Nami", "나미", "SUPPORT"));
            put(75, new ChampionInfo("Nasus", "나서스", "TOP"));
            put(111, new ChampionInfo("Nautilus", "노틸러스", "SUPPORT"));
            put(518, new ChampionInfo("Neeko", "니코", "MIDDLE,SUPPORT"));
            put(76, new ChampionInfo("Nidalee", "니달리", "JUNGLE"));
            put(56, new ChampionInfo("Nocturne", "녹턴", "JUNGLE"));
            put(20, new ChampionInfo("Nunu", "누누와 윌럼프", "JUNGLE"));

            // O로 시작하는 챔피언들
            put(2, new ChampionInfo("Olaf", "올라프", "JUNGLE,TOP"));
            put(61, new ChampionInfo("Orianna", "오리아나", "MIDDLE"));
            put(516, new ChampionInfo("Ornn", "오른", "TOP"));

            // P로 시작하는 챔피언들
            put(80, new ChampionInfo("Pantheon", "판테온", "SUPPORT,MIDDLE,TOP"));
            put(78, new ChampionInfo("Poppy", "뽀삐", "TOP,JUNGLE"));
            put(555, new ChampionInfo("Pyke", "파이크", "SUPPORT"));

            // Q로 시작하는 챔피언들
            put(246, new ChampionInfo("Qiyana", "키아나", "MIDDLE"));
            put(133, new ChampionInfo("Quinn", "퀸", "TOP"));

            // R로 시작하는 챔피언들
            put(497, new ChampionInfo("Rakan", "라칸", "SUPPORT"));
            put(33, new ChampionInfo("Rammus", "람머스", "JUNGLE"));
            put(421, new ChampionInfo("RekSai", "렉사이", "JUNGLE"));
            put(526, new ChampionInfo("Rell", "렐", "SUPPORT"));
            put(58, new ChampionInfo("Renekton", "레넥톤", "TOP"));
            put(107, new ChampionInfo("Rengar", "렝가", "JUNGLE,TOP"));
            put(92, new ChampionInfo("Riven", "리븐", "TOP"));
            put(68, new ChampionInfo("Rumble", "럼블", "TOP,MIDDLE"));
            put(13, new ChampionInfo("Ryze", "라이즈", "MIDDLE"));

            // S로 시작하는 챔피언들
            put(360, new ChampionInfo("Samira", "사미라", "BOTTOM"));
            put(113, new ChampionInfo("Sejuani", "세주아니", "JUNGLE,TOP"));
            put(235, new ChampionInfo("Senna", "세나", "SUPPORT,BOTTOM"));
            put(147, new ChampionInfo("Seraphine", "세라핀", "SUPPORT,MIDDLE"));
            put(875, new ChampionInfo("Sett", "세트", "TOP,SUPPORT"));
            put(35, new ChampionInfo("Shaco", "샤코", "JUNGLE"));
            put(98, new ChampionInfo("Shen", "쉔", "TOP"));
            put(102, new ChampionInfo("Shyvana", "쉬바나", "JUNGLE"));
            put(27, new ChampionInfo("Singed", "신지드", "TOP"));
            put(14, new ChampionInfo("Sion", "사이온", "TOP"));
            put(15, new ChampionInfo("Sivir", "시비르", "BOTTOM"));
            put(72, new ChampionInfo("Skarner", "스카너", "JUNGLE"));
            put(37, new ChampionInfo("Sona", "소나", "SUPPORT"));
            put(16, new ChampionInfo("Soraka", "소라카", "SUPPORT"));
            put(50, new ChampionInfo("Swain", "스웨인", "SUPPORT,MIDDLE"));
            put(517, new ChampionInfo("Sylas", "사일러스", "MIDDLE"));
            put(134, new ChampionInfo("Syndra", "신드라", "MIDDLE"));

            // T로 시작하는 챔피언들
            put(223, new ChampionInfo("TahmKench", "탐 켄치", "SUPPORT,TOP"));
            put(163, new ChampionInfo("Taliyah", "탈리야", "JUNGLE,MIDDLE"));
            put(91, new ChampionInfo("Talon", "탈론", "MIDDLE,JUNGLE"));
            put(44, new ChampionInfo("Taric", "타릭", "SUPPORT"));
            put(17, new ChampionInfo("Teemo", "티모", "TOP"));
            put(412, new ChampionInfo("Thresh", "쓰레쉬", "SUPPORT"));
            put(18, new ChampionInfo("Tristana", "트리스타나", "BOTTOM"));
            put(48, new ChampionInfo("Trundle", "트런들", "JUNGLE,TOP"));
            put(23, new ChampionInfo("Tryndamere", "트린다미어", "TOP"));
            put(4, new ChampionInfo("TwistedFate", "트위스티드 페이트", "MIDDLE"));
            put(29, new ChampionInfo("Twitch", "트위치", "BOTTOM"));

            // U로 시작하는 챔피언들
            put(77, new ChampionInfo("Udyr", "우디르", "JUNGLE"));
            put(6, new ChampionInfo("Urgot", "우르곳", "TOP"));

            // V로 시작하는 챔피언들
            put(110, new ChampionInfo("Varus", "바루스", "BOTTOM"));
            put(67, new ChampionInfo("Vayne", "베인", "BOTTOM,TOP"));
            put(45, new ChampionInfo("Veigar", "베이가", "MIDDLE,BOTTOM"));
            put(161, new ChampionInfo("Velkoz", "벨코즈", "SUPPORT,MIDDLE"));
            put(711, new ChampionInfo("Vex", "벡스", "MIDDLE"));
            put(254, new ChampionInfo("Vi", "바이", "JUNGLE"));
            put(234, new ChampionInfo("Viego", "비에고", "JUNGLE"));
            put(112, new ChampionInfo("Viktor", "빅토르", "MIDDLE"));
            put(8, new ChampionInfo("Vladimir", "블라디미르", "MIDDLE,TOP"));
            put(106, new ChampionInfo("Volibear", "볼리베어", "TOP,JUNGLE"));

            // W로 시작하는 챔피언들
            put(19, new ChampionInfo("Warwick", "워윅", "JUNGLE,TOP"));
            put(498, new ChampionInfo("Xayah", "자야", "BOTTOM"));
            put(101, new ChampionInfo("Xerath", "제라스", "SUPPORT,MIDDLE"));
            put(5, new ChampionInfo("XinZhao", "신 짜오", "JUNGLE"));

            // Y로 시작하는 챔피언들
            put(157, new ChampionInfo("Yasuo", "야스오", "MIDDLE,TOP"));
            put(777, new ChampionInfo("Yone", "요네", "MIDDLE,TOP"));
            put(83, new ChampionInfo("Yorick", "요릭", "TOP"));
            put(350, new ChampionInfo("Yuumi", "유미", "SUPPORT"));

            // Z로 시작하는 챔피언들
            put(154, new ChampionInfo("Zac", "자크", "JUNGLE"));
            put(238, new ChampionInfo("Zed", "제드", "MIDDLE"));
            put(115, new ChampionInfo("Ziggs", "직스", "MIDDLE,BOTTOM"));
            put(26, new ChampionInfo("Zilean", "질리언", "SUPPORT"));
            put(142, new ChampionInfo("Zoe", "조이", "MIDDLE"));
            put(143, new ChampionInfo("Zyra", "자이라", "SUPPORT"));

            // 누락된 챔피언들 추가/수정
            put(30, new ChampionInfo("Karthus", "카서스", "JUNGLE,MIDDLE"));
            put(43, new ChampionInfo("Karma", "카르마", "SUPPORT"));
            put(145, new ChampionInfo("Kaisa", "카이사", "BOTTOM"));
            put(429, new ChampionInfo("Kalista", "칼리스타", "BOTTOM"));
            put(800, new ChampionInfo("Renata", "레나타 글라스크", "SUPPORT"));
            put(221, new ChampionInfo("Zeri", "제리", "BOTTOM"));
            put(895, new ChampionInfo("Nilah", "닐라", "BOTTOM"));
            put(897, new ChampionInfo("KSante", "케이산테", "TOP"));
            put(902, new ChampionInfo("Milio", "밀리오", "SUPPORT"));
            put(893, new ChampionInfo("Aurora", "오로라", "MIDDLE"));
            put(910, new ChampionInfo("Hwei", "흐웨이", "MIDDLE"));
            put(903, new ChampionInfo("Mel", "멜", "MIDDLE"));
            put(904, new ChampionInfo("Ambessa", "암베사", "TOP"));
        }
    };

    @Data
    private static class ChampionInfo {
        private String name;
        private String nameKo;
        private String positions; // 콤마로 구분된 포지션 문자열

        public ChampionInfo(String name, String nameKo, String positions) {
            this.name = name;
            this.nameKo = nameKo;
            this.positions = positions;
        }
    }

    public ChampionStatisticsService(MatchRepository matchRepository,
            ChampionStatisticsRepository championStatisticsRepository,
            ChampionStatisticsPositionsRepository positionsRepository,
            ChampionBanRepository championBanRepository) {
        this.matchRepository = matchRepository;
        this.championStatisticsRepository = championStatisticsRepository;
        this.championBanRepository = championBanRepository;
    }

    @Scheduled(initialDelay = 30000000, fixedRate = 780000)
    @Transactional
    public void updateChampionStatistics() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        Set<ChallengerParticipantEntity> participants = matchRepository.findMatchesAfterWithDetails(cutoff);
        int totalMatches = participants.size() / 10; // 매치당 10명의 참가자

        // 기존 데이터 삭제 코드 제거
        // championStatisticsRepository.deleteAll();
        // positionsRepository.deleteAll();

        // 현재 존재하는 챔피언 통계 정보 가져오기
        List<ChampionStatisticsEntity> existingStatsList = championStatisticsRepository.findAll();
        Map<String, ChampionStatisticsEntity> existingStats = existingStatsList.stream()
                .collect(Collectors.toMap(ChampionStatisticsEntity::getChampionId, stats -> stats,
                        (existing, replacement) -> existing));

        // 챔피언별 통계 수집
        Map<String, ChampionStatisticsEntity> championStats = new HashMap<>();

        // 게임 데이터 수집
        for (ChallengerParticipantEntity participant : participants) {
            String championId = participant.getChampionId();

            // 기존 통계가 있으면 사용, 없으면 새로 생성
            ChampionStatisticsEntity stats;
            if (existingStats.containsKey(championId)) {
                stats = existingStats.get(championId);
                // 기존 데이터를 재설정 (과거 데이터 삭제 효과)
                resetStatsForEntity(stats);
            } else {
                stats = createNewChampionStats(championId, participant.getChampionName());
            }

            if (stats != null) {
                stats.addGame(participant);
                championStats.put(championId, stats);
            }
        }

        // 밴 데이터 수집 - championBanRepository 사용
        List<String> bannedChampions = championBanRepository.findBannedChampionsAfter(cutoff);
        for (String championId : bannedChampions) {
            // 기존 통계가 있으면 사용, 없으면 새로 생성
            ChampionStatisticsEntity stats;
            if (championStats.containsKey(championId)) {
                stats = championStats.get(championId);
            } else if (existingStats.containsKey(championId)) {
                stats = existingStats.get(championId);
                resetStatsForEntity(stats);
                championStats.put(championId, stats);
            } else {
                stats = createNewChampionStats(championId, getChampionNameById(championId));
                if (stats != null) {
                    championStats.put(championId, stats);
                }
            }

            if (stats != null) {
                stats.addBan();
            }
        }

        // 통계 계산 및 저장
        championStats.values().forEach(stats -> {
            stats.calculateRates(totalMatches);
            championStatisticsRepository.save(stats);
        });

        log.info("Updated champion statistics: {} champions, {} total matches",
                championStats.size(), totalMatches);
    }

    // 기존 통계 데이터 리셋을 위한 헬퍼 메서드
    private void resetStatsForEntity(ChampionStatisticsEntity entity) {
        if (entity == null)
            return;

        // 실제 엔티티에 존재하는 필드와 메서드만 사용
        try {
            // 원래 엔티티의 ID와 이름 등 고유 정보는 유지하고
            // 통계 데이터만 초기화
            if (entity.getWins() > 0)
                entity.setWins(0);
            if (entity.getTotalGames() > 0)
                entity.setTotalGames(0);
            if (entity.getBans() > 0)
                entity.setBans(0);

            // 계산된 비율 정보 초기화
            entity.setWinRate(0.0);
            entity.setPickRate(0.0);
            entity.setBanRate(0.0);

            // 스코어와 티어 정보 유지 (이후 계산에서 업데이트됨)
        } catch (Exception e) {
            log.error("Error resetting stats for champion {}: {}",
                    entity.getChampionId(), e.getMessage());
        }
    }

    private ChampionStatisticsEntity createNewChampionStats(String championId, String championName) {
        if (championId == null || championId.isEmpty()) {
            log.warn("Empty or null championId received");
            return null;
        }

        log.debug("Creating new stats for champion: {} ({})", championName, championId);
        ChampionStatisticsEntity stats = new ChampionStatisticsEntity(championId, championName);

        try {
            ChampionInfo info = championData.get(Integer.parseInt(championId));
            if (info != null) {
                stats.setNameKo(info.getNameKo());
                stats.setPositions(Arrays.asList(info.getPositions().split(",")));
                log.debug("Set additional info for champion: {}", championId);
            } else {
                log.warn("No champion info found for ID: {}", championId);
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid champion ID: {}", championId);
        }
        return stats;
    }

    public List<ChampionStatisticsEntity> calculateChampionStatistics() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        Set<ChallengerParticipantEntity> participants = matchRepository.findMatchesAfterWithDetails(cutoff);

        if (participants.isEmpty()) {
            return new ArrayList<>();
        }

        // 현재 존재하는 챔피언 통계 정보 가져오기
        List<ChampionStatisticsEntity> existingStatsList = championStatisticsRepository.findAll();
        Map<String, ChampionStatisticsEntity> existingStats = existingStatsList.stream()
                .collect(Collectors.toMap(ChampionStatisticsEntity::getChampionId, stats -> stats,
                        (existing, replacement) -> existing));

        Map<String, ChampionStatisticsEntity> championStats = new HashMap<>();
        int totalMatches = participants.size() / 10;

        // 게임 데이터 수집
        participants.stream()
                .filter(participant -> participant.getChampionId() != null
                        && !participant.getChampionId().isEmpty())
                .forEach(participant -> {
                    String championId = participant.getChampionId();

                    // 기존 통계가 있으면 사용, 없으면 새로 생성
                    ChampionStatisticsEntity stats;
                    if (existingStats.containsKey(championId)) {
                        stats = existingStats.get(championId);
                        resetStatsForEntity(stats); // 기존 데이터 초기화
                    } else {
                        stats = createNewChampionStats(championId, participant.getChampionName());
                    }

                    if (stats != null) {
                        stats.addGame(participant);
                        championStats.put(championId, stats);
                    }
                });

        // 밴 데이터 수집
        List<String> bannedChampions = championBanRepository.findBannedChampionsAfter(cutoff);
        for (String championId : bannedChampions) {
            ChampionStatisticsEntity stats;
            if (championStats.containsKey(championId)) {
                stats = championStats.get(championId);
            } else if (existingStats.containsKey(championId)) {
                stats = existingStats.get(championId);
                resetStatsForEntity(stats);
                championStats.put(championId, stats);
            } else {
                stats = createNewChampionStats(championId, getChampionNameById(championId));
                if (stats != null) {
                    championStats.put(championId, stats);
                }
            }

            if (stats != null) {
                stats.addBan();
            }
        }

        // 통계 계산
        List<ChampionStatisticsEntity> statsList = championStats.values().stream()
                .map(stats -> {
                    stats.calculateRates(totalMatches);
                    return stats;
                })
                .collect(Collectors.toList());

        // 각 지표별 최대값 찾기
        double maxWinRate = statsList.stream()
                .filter(s -> s.getWinRate() > 0)
                .mapToDouble(ChampionStatisticsEntity::getWinRate)
                .max().orElse(100);
        double maxPickRate = statsList.stream()
                .filter(s -> s.getPickRate() > 0)
                .mapToDouble(ChampionStatisticsEntity::getPickRate)
                .max().orElse(100);
        double maxBanRate = statsList.stream()
                .filter(s -> s.getBanRate() > 0)
                .mapToDouble(ChampionStatisticsEntity::getBanRate)
                .max().orElse(100);

        // 점수 계산 및 티어 결정
        List<ChampionStatisticsEntity> finalStats = statsList.stream()
                .map(stats -> {
                    if (stats.getTotalGames() > 0 || stats.getBans() > 0) {
                        double score = calculateScore(
                                stats.getWinRate(),
                                stats.getPickRate(),
                                stats.getBanRate(),
                                maxWinRate,
                                maxPickRate,
                                maxBanRate);
                        stats.setScore(score);
                        stats.setTier(calculateTier(score));
                    }
                    return stats;
                })
                .sorted(Comparator
                        .comparing(ChampionStatisticsEntity::getTier, (t1, t2) -> {
                            String[] tiers = { "S+", "S", "A+", "A", "B+", "B", "C+", "C", "D+", "D", "E" };
                            return Integer.compare(
                                    Arrays.asList(tiers).indexOf(t1),
                                    Arrays.asList(tiers).indexOf(t2));
                        })
                        .thenComparing(ChampionStatisticsEntity::getScore, Comparator.reverseOrder()))
                .collect(Collectors.toList());

        // 저장 - deleteAll 호출하지 않고 개별 저장
        championStatisticsRepository.saveAll(finalStats);

        log.info("Updated champion statistics: {} champions, {} total matches",
                finalStats.size(), totalMatches);

        return finalStats;
    }

    private double calculateScore(double winRate, double pickRate, double banRate, double maxWinRate,
            double maxPickRate, double maxBanRate) {
        // 상대적 비율 계산
        double relativeWinRate = (winRate / maxWinRate) * 100;
        double relativePickRate = (pickRate / maxPickRate) * 100;
        double relativeBanRate = (banRate / maxBanRate) * 100;
        double totalPickBanRate = pickRate + banRate;

        // 픽밴률이 너무 낮은 경우 (5% 미만) 상위 티어 제한
        if (totalPickBanRate < 5.0) {
            // 승률이 아무리 높아도 최대 B+ 티어까지만 가능
            double baseScore = 20.0; // C 등급 기본
            double winRateBonus = Math.min((winRate - 50.0) * 0.5, 15.0); // 최대 15점 보너스
            return Math.min(baseScore + winRateBonus, 40.0); // 최대 B+ 등급
        }

        // 픽밴률이 낮은 경우 (10% 미만) 상위 티어 제한
        if (totalPickBanRate < 10.0) {
            // 승률이 아무리 높아도 최대 A 티어까지만 가능
            double baseScore = 25.0; // C+ 등급 기본
            double winRateBonus = Math.min((winRate - 50.0) * 0.6, 20.0); // 최대 20점 보너스
            return Math.min(baseScore + winRateBonus, 45.0); // 최대 A 등급
        }

        // 기본 가중치 설정
        double winRateMultiplier = 0.60;
        double pickRateMultiplier = 0.25;
        double banRateMultiplier = 0.15;

        // 승률이 매우 낮은 경우 (40% 미만) 강제 하향
        if (winRate < 40.0) {
            double baseScore = 10.0;
            double pickBanBonus = Math.min(totalPickBanRate * 0.2, 5.0);
            return Math.min(baseScore + pickBanBonus, 15.0);
        }

        // 승률이 47% 미만인 경우의 차등 제한
        if (winRate < 47.0) {
            // 높은 픽밴률(30% 이상)인 경우 덜 엄격한 제한
            if (totalPickBanRate >= 30.0) {
                if (winRate >= 46.0) {
                    return Math.min(35.0, 30.0 + (totalPickBanRate - 30.0) * 0.2); // 최대 B+ 티어
                } else {
                    return Math.min(30.0, 25.0 + (totalPickBanRate - 30.0) * 0.15); // 최대 B 티어
                }
            }
            // 중간 픽밴률(15~30%)인 경우
            else if (totalPickBanRate >= 15.0) {
                if (winRate >= 46.0) {
                    return Math.min(30.0, 25.0 + (totalPickBanRate - 15.0) * 0.3); // 최대 B 티어
                } else {
                    return Math.min(25.0, 20.0 + (totalPickBanRate - 15.0) * 0.25); // 최대 C+ 티어
                }
            }
            // 낮은 픽밴률인 경우 기존 제한 유지
            else {
                return Math.min(20.0, 15.0 + totalPickBanRate * 0.2); // 최대 C 티어
            }
        }

        // 승률 보너스는 픽밴률에 따라 차등 적용
        double winRateBonus = 0.0;
        if (winRate >= 52.0) {
            if (totalPickBanRate >= 20.0) {
                winRateBonus = (winRate - 52.0) * 1; // 높은 픽밴률일 때 더 큰 보너스
                winRateMultiplier += 0.05;
            } else {
                winRateBonus = (winRate - 52.0) * 0.4; // 낮은 픽밴률일 때 작은 보너스
            }
        }

        // 픽밴률 보너스도 승률에 따라 차등 적용
        if (totalPickBanRate >= 20.0) {
            double pickBanBonus = Math.min((totalPickBanRate - 20.0) * 0.3, 5.0);
            if (winRate >= 50.0) {
                winRateBonus += pickBanBonus; // 승률이 50% 이상일 때만 픽밴 보너스 전체 적용
            } else {
                winRateBonus += pickBanBonus * 0.5; // 승률이 50% 미만일 때는 픽밴 보너스 절반만 적용
            }
        }

        double adjustedWinRate = relativeWinRate + winRateBonus;
        return (adjustedWinRate * winRateMultiplier) +
                (relativePickRate * pickRateMultiplier) +
                (relativeBanRate * banRateMultiplier);
    }

    private String calculateTier(double score) {
        // 티어 결정 (11단계)
        if (score >= 70)
            return "S+"; // 상위 3% - 절대적 메타
        if (score >= 60)
            return "S"; // 상위 7% - 최상위 메타
        if (score >= 50)
            return "A+"; // 상위 15% - 강력한 챔피언
        if (score >= 45)
            return "A"; // 상위 25% - 준수한 성능
        if (score >= 40)
            return "B+"; // 상위 35% - 평균 이상
        if (score >= 30)
            return "B"; // 상위 50% - 평균
        if (score >= 25)
            return "C+"; // 상위 65% - 평균 이하
        if (score >= 20)
            return "C"; // 상위 75% - 약한 성능
        if (score >= 15)
            return "D+"; // 상위 85% - 매우 약함
        if (score >= 10)
            return "D"; // 상위 95% - 극히 약함
        return "E"; // 하위 5% - 메타에서 벗어남
    }

    public String getChampionNameById(String championId) {
        try {
            ChampionInfo info = championData.get(Integer.parseInt(championId));
            return info != null ? info.getName() : "Unknown";
        } catch (NumberFormatException e) {
            return "Unknown";
        }
    }
}