import React, { useState } from 'react';
import styles from './SearchBar.module.css';

// SearchBar 컴포넌트의 props 타입 정의
interface SearchBarProps {
    onSearch: (summonerName: string) => void; // 검색 실행 함수
}

// SearchBar 컴포넌트
export const SearchBar: React.FC<SearchBarProps> = ({ onSearch }) => {
    // 입력된 소환사명을 저장할 상태
    const [summonerName, setSummonerName] = useState('');


    // 폼 제출 처리 함수
    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault(); // 기본 폼 제출 동작 방지
        if (summonerName.trim()) {
            onSearch(summonerName); // 부모 컴포넌트의 검색 함수 호출
        }
    };

    return (
        <form onSubmit={handleSubmit} className={styles["search-bar"]}>
            <input
                type="text"
                value={summonerName}
                // 입력값이 변경될 때마다 상태 업데이트
                onChange={(e) => setSummonerName(e.target.value)}
                placeholder="소환사명#태그 (예: Hide on bush#KR1)"
                aria-label="소환사 검색"
            />
            <button type="submit">검색</button>
        </form>
    );
};