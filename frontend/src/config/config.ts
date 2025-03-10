const isDevelopment = window.process?.env?.NODE_ENV === 'development';

// API 관련 설정값들
export const config = {
    RIOT_API_KEY: 'RGAPI-c169db00-99c1-4bf0-b995-7eec14cada2c', 
    RIOT_API_BASE_URL: 'https://kr.api.riotgames.com',
    API_BASE_URL: window.process?.env?.REACT_APP_API_BASE_URL || 'http://localhost:8080'
};