import axios from 'axios';

// Champion 인터페이스 정의
export interface Champion {
    id: number;
    name: string;
    // ... 다른 속성들
}

// window.process.env 사용으로 변경
const API_BASE_URL = window.process?.env?.REACT_APP_API_BASE_URL || 'http://localhost:8080';

export const getChampionStats = async (): Promise<Champion[]> => {
    try {
        const response = await axios.get<Champion[]>(`${API_BASE_URL}/api/champions`);
        return response.data;
    } catch (error) {
        console.error('챔피언 정보를 가져오는데 실패했습니다:', error);
        return [];
    }
}; 