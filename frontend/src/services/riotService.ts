import axios from 'axios';
import { config } from '../config/config';

export class RiotService {
    private readonly headers = {
        'X-Riot-Token': config.RIOT_API_KEY
    };

    // Helper method to get authentication headers
    private getAuthHeaders() {
        const token = localStorage.getItem('token');
        return {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            ...(token ? { 'Authorization': `Bearer ${token}` } : {})
        };
    }

    async getChampionRecommendations(summonerName: string) {
        try {
            const response = await axios.get(
                `${config.API_BASE_URL}/api/recommendations/${encodeURIComponent(summonerName)}`,
                {
                    headers: this.getAuthHeaders()
                }
            );
            console.log('API 추천 챔피언 응답:', response.data);
            
            // 서버 응답에서 recommendedChampions 필드 추출
            if (response.data && response.data.recommendedChampions) {
                // 백엔드 응답을 프론트엔드 구조에 맞게 변환
                return response.data.recommendedChampions.map((champion: any) => ({
                    championId: champion.championName, // 일단 챔피언 이름을 ID로 사용
                    championName: champion.championName,
                    position: response.data.recommendedPosition || 'MIDDLE',
                    winRate: champion.totalGames > 0 ? Math.round((champion.wins / champion.totalGames) * 100) : 0,
                    pickRate: Math.round(champion.pickRate * 100),
                    banRate: Math.round(champion.banRate * 100),
                    kda: champion.avgKDA || 0
                }));
            }
            
            // 응답이 없거나 형식이 맞지 않으면 빈 배열 반환
            return [];
        } catch (error) {
            console.error('Error fetching champion recommendations:', error);
            return []; // 에러 발생 시 빈 배열 반환
        }
    }

    async getRankings(page: number = 0, tier?: string): Promise<any> {
        try {
            let url = `/api/rankings?page=${page}`;
            if (tier) {
                url += `&tier=${tier}`;
            }
            const response = await axios.get(
                url,
                {
                    headers: this.getAuthHeaders(),
                    withCredentials: true
                }
            );
            console.log('Rankings response:', response.data);
            return response.data;
        } catch (error) {
            console.error('Error fetching rankings:', error);
            throw error;
        }
    }

    async getSummonerByName(summonerName: string) {
        try {
            console.log('Searching for summoner:', summonerName);
            
            const response = await axios.get(
                `${config.API_BASE_URL}/api/summoner/${encodeURIComponent(summonerName)}`,
                {
                    headers: this.getAuthHeaders(),
                    withCredentials: true
                }
            );

            if (response.status === 200 && response.data) {
                console.log('Summoner data received:', response.data);
                
                // 서버에서 평균 통계와 챔피언 통계가 오지 않는 경우를 대비한 더미 데이터
                // 실제 환경에서는 서버에서 이러한 데이터를 제공하거나 추가 API 호출로 가져와야 함
                const enhancedData = {
                    ...response.data,
                    averageStats: response.data.averageStats || this.getDummyAverageStats(),
                    championStats: response.data.championStats || this.getDummyChampionStats()
                };
                
                console.log('Enhanced summoner data:', enhancedData);
                return enhancedData;
            } else {
                console.log('No data in response');
                throw new Error('소환사를 찾을 수 없습니다.');
            }
        } catch (error: any) {
            console.error('Error fetching summoner:', error);
            if (error.response?.status === 500) {
                throw new Error('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
            } else if (error.response?.status === 404) {
                throw new Error('소환사를 찾을 수 없습니다. 소환사명을 확인해주세요.');
            } else {
                throw new Error(error.message || '알 수 없는 오류가 발생했습니다.');
            }
        }
    }

    // 평균 통계 더미 데이터 (서버에서 데이터가 안 올 때 사용)
    private getDummyAverageStats() {
        return {
            kills: 5.2,
            deaths: 4.8,
            assists: 7.5,
            cs: 185.3,
            visionScore: 18.7
        };
    }
    
    // 챔피언 통계 더미 데이터 (서버에서 데이터가 안 올 때 사용)
    private getDummyChampionStats() {
        return [
            {
                championId: 'Aatrox',
                games: 12,
                wins: 8,
                losses: 4,
                kills: 7.2,
                deaths: 4.1,
                assists: 5.3,
                winRate: 67
            },
            {
                championId: 'Ahri',
                games: 8,
                wins: 5,
                losses: 3,
                kills: 8.5,
                deaths: 3.8,
                assists: 7.2,
                winRate: 63
            },
            {
                championId: 'Akali',
                games: 10,
                wins: 6,
                losses: 4,
                kills: 9.1,
                deaths: 5.2,
                assists: 4.7,
                winRate: 60
            }
        ];
    }

    async getMatchList(puuid: string, start = 0, count = 20) {
        try {
            const response = await axios.get(
                `${config.API_BASE_URL}/api/matches/by-puuid/${puuid}`,
                {
                    params: { start, count },
                    headers: this.getAuthHeaders(),
                    withCredentials: true
                }
            );
            return response.data;
        } catch (error) {
            console.error('Error fetching match list:', error);
            throw error;
        }
    }

    async getMatchDetails(matchId: string) {
        try {
            const response = await axios.get(
                `${config.RIOT_API_BASE_URL}/lol/match/v5/matches/${matchId}`,
                { headers: this.headers }
            );
            return response.data;
        } catch (error) {
            console.error('Error fetching match details:', error);
            throw error;
        }
    }

    async getMatchHistory(summonerName: string) {
        try {
            const response = await axios.get(
                `${config.API_BASE_URL}/api/summoner/${encodeURIComponent(summonerName)}/matches`,
                {
                    headers: this.getAuthHeaders(),
                    withCredentials: true
                }
            );
            return response.data;
        } catch (error) {
            console.error('Error fetching match history:', error);
            return [];
        }
    }

    async getSummonerRank(summonerId: string) {
        try {
            console.log('Requesting rank data for summoner:', summonerId);
            const response = await axios.get(
                `${config.API_BASE_URL}/api/summoner/rank/${summonerId}`,
                {
                    headers: this.getAuthHeaders(),
                    withCredentials: true
                }
            );
            console.log('Rank data response:', response.data);
            return response.data;
        } catch (error) {
            console.error('Error fetching summoner rank:', error);
            throw error;
        }
    }

    async getCurrentGame(summonerId: string) {
        try {
            console.log('Requesting current game data for summoner:', summonerId);
            const response = await axios.get(
                `${config.API_BASE_URL}/api/summoner/current-game/${summonerId}`,
                {
                    headers: this.getAuthHeaders(),
                    withCredentials: true
                }
            );
            
            if (response.data && Object.keys(response.data).length > 0) {
                console.log('Summoner is currently in game');
                return response.data;
            } else {
                console.log('Summoner is not in game');
                return null;
            }
        } catch (error) {
            if (axios.isAxiosError(error)) {
                if (error.response?.status === 404) {
                    console.log('Summoner is not in game (404 response)');
                    return null; // 게임 중이 아님
                }
                if (error.response?.status === 403) {
                    console.error('API 키 권한 오류');
                    throw new Error('API 키 권한이 없습니다.');
                }
            }
            console.error('Error fetching current game:', error);
            throw error;
        }
    }

    async refreshMatchHistory(summonerName: string) {
        try {
            const response = await axios.post(
                `${config.API_BASE_URL}/api/matches/refresh/${encodeURIComponent(summonerName)}`,
                {},
                {
                    headers: this.getAuthHeaders(),
                    withCredentials: true
                }
            );
            return response.data;
        } catch (error) {
            console.error('Error refreshing match history:', error);
            throw error;
        }
    }

    async analyzeMentalState(summonerName: string) {
        console.log(`Sending mental analysis request for: ${summonerName}`);
        try {
            const response = await axios.get(
                `${config.API_BASE_URL}/api/mental/analyze/${encodeURIComponent(summonerName)}`,
                {
                    headers: this.getAuthHeaders(),
                    withCredentials: true
                }
            );
            console.log('Mental analysis API response:', response.status);
            return response.data;
        } catch (error) {
            console.error('Error analyzing mental state:', error);
            if (axios.isAxiosError(error) && error.response) {
                console.error('API error response:', error.response.data);
                return error.response.data; // Return the error response for handling in the component
            }
            throw error;
        }
    }

    async getCommunityPosts(position: string = 'all') {
        try {
            const url = position === 'all' 
                ? `${config.API_BASE_URL}/api/community/posts`
                : `${config.API_BASE_URL}/api/community/posts/position/${position}`;
            
            const response = await axios.get(url, {
                headers: this.getAuthHeaders(),
                withCredentials: true
            });
            return response.data;
        } catch (error) {
            console.error('Error fetching community posts:', error);
            return [];
        }
    }

    async getFeaturedPosts() {
        try {
            const response = await axios.get(
                `${config.API_BASE_URL}/api/community/posts/featured`,
                {
                    headers: this.getAuthHeaders(),
                    withCredentials: true
                }
            );
            return response.data;
        } catch (error) {
            console.error('Error fetching featured posts:', error);
            return [];
        }
    }

    async getPopularPosts() {
        try {
            const response = await axios.get(
                `${config.API_BASE_URL}/api/community/posts/popular`,
                {
                    headers: this.getAuthHeaders(),
                    withCredentials: true
                }
            );
            return response.data;
        } catch (error) {
            console.error('Error fetching popular posts:', error);
            return [];
        }
    }

    async getPostById(postId: number) {
        try {
            const response = await axios.get(
                `${config.API_BASE_URL}/api/community/posts/${postId}`,
                {
                    headers: this.getAuthHeaders(),
                    withCredentials: true
                }
            );
            return response.data;
        } catch (error) {
            console.error(`Error fetching post with ID ${postId}:`, error);
            throw error;
        }
    }

    async createPost(postData: any) {
        try {
            const response = await axios.post(
                `${config.API_BASE_URL}/api/community/posts`,
                postData,
                {
                    headers: this.getAuthHeaders(),
                    withCredentials: true
                }
            );
            return response.data;
        } catch (error) {
            console.error('Error creating post:', error);
            throw error;
        }
    }

    async likePost(postId: number) {
        try {
            const response = await axios.post(
                `${config.API_BASE_URL}/api/community/posts/${postId}/like`,
                {},
                {
                    headers: this.getAuthHeaders(),
                    withCredentials: true
                }
            );
            return response.data;
        } catch (error) {
            console.error(`Error liking post with ID ${postId}:`, error);
            throw error;
        }
    }

    async addComment(postId: number, commentData: any) {
        try {
            const response = await axios.post(
                `${config.API_BASE_URL}/api/community/posts/${postId}/comments`,
                commentData,
                {
                    headers: this.getAuthHeaders(),
                    withCredentials: true
                }
            );
            return response.data;
        } catch (error) {
            console.error(`Error adding comment to post with ID ${postId}:`, error);
            throw error;
        }
    }

    async likeComment(commentId: number) {
        try {
            const response = await axios.post(
                `${config.API_BASE_URL}/api/community/comments/${commentId}/like`,
                {},
                {
                    headers: this.getAuthHeaders(),
                    withCredentials: true
                }
            );
            return response.data;
        } catch (error) {
            console.error(`Error liking comment with ID ${commentId}:`, error);
            throw error;
        }
    }

    // 소환사 데이터 새로고침을 위한 API 호출
    async refreshSummonerData(summonerName: string) {
        try {
            const response = await axios.post(
                `${config.API_BASE_URL}/api/summoner/refresh/${encodeURIComponent(summonerName)}`,
                {},
                {
                    headers: this.getAuthHeaders(),
                    withCredentials: true
                }
            );
            return response.data;
        } catch (error) {
            console.error('서버 캐시 새로고침 오류:', error);
            throw error;
        }
    }
}
