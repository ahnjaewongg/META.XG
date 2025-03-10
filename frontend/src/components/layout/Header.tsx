import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import styles from './Header.module.css';
import { Button } from '@mui/material';

interface HeaderProps {
    onSearch: (name: string) => void;
    onResetSummoner?: () => void;
    patchNotes?: any[];
    showSearchBar?: boolean;
}

export const Header: React.FC<HeaderProps> = ({ onSearch, onResetSummoner, patchNotes = [], showSearchBar = true }) => {
    const [searchTerm, setSearchTerm] = useState('');
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const navigate = useNavigate();

    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        if (searchTerm.trim()) {
            onSearch(searchTerm.trim());
        }
    };

    const handleHomeClick = () => {
        if (onResetSummoner) {
            onResetSummoner();
        }
        navigate('/');
    };

    return (
        <header className={styles.header}>
            <div className={styles.headerContent}>
                <div className={styles.logoContainer}>
                    <a onClick={handleHomeClick} className={styles.logo}>
                        META<span className={styles.highlight}>.XG</span>
                    </a>
                </div>
                
                <button 
                    className={styles.mobileMenuToggle} 
                    onClick={() => setIsMenuOpen(!isMenuOpen)}
                    aria-label="메뉴 토글"
                >
                    <span className={styles.hamburger}></span>
                </button>
                
                <nav className={`${styles.navigation} ${isMenuOpen ? styles.open : ''}`}>
                    <ul className={styles.navLinks}>
                        <li><a onClick={handleHomeClick}>홈</a></li>
                        <li><Link to="/rankings">랭킹</Link></li>
                        <li><Link to="/champions">챔피언 분석</Link></li>
                        <li><Link to="/community">커뮤니티</Link></li>
                    </ul>
                </nav>
                
                {showSearchBar && (
                    <form className={styles.searchBar} onSubmit={handleSearch}>
                        <input 
                            type="text" 
                            placeholder="소환사 이름..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className={styles.searchInput}
                        />
                        <button type="submit" className={styles.searchButton}>
                            <span className={styles.searchIcon}>
                                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M15.5 14H14.71L14.43 13.73C15.41 12.59 16 11.11 16 9.5C16 5.91 13.09 3 9.5 3C5.91 3 3 5.91 3 9.5C3 13.09 5.91 16 9.5 16C11.11 16 12.59 15.41 13.73 14.43L14 14.71V15.5L19 20.49L20.49 19L15.5 14ZM9.5 14C7.01 14 5 11.99 5 9.5C5 7.01 7.01 5 9.5 5C11.99 5 14 7.01 14 9.5C14 11.99 11.99 14 9.5 14Z" fill="currentColor"/>
                                </svg>
                            </span>
                        </button>
                    </form>
                )}
            </div>
            
            <div className={`${styles.mobileMenu} ${isMenuOpen ? styles.open : ''}`}>
                <ul className={styles.mobileLinks}>
                    <li><a onClick={handleHomeClick}>홈</a></li>
                    <li><Link to="/rankings">랭킹</Link></li>
                    <li><Link to="/champions">챔피언 분석</Link></li>
                    <li><Link to="/community">커뮤니티</Link></li>
                </ul>
                <form className={styles.mobileSearchBar} onSubmit={handleSearch}>
                    <input 
                        type="text" 
                        placeholder="소환사 이름..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                    <button type="submit" className={styles.searchButton}>
                        <span className={styles.searchIcon}>
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M15.5 14H14.71L14.43 13.73C15.41 12.59 16 11.11 16 9.5C16 5.91 13.09 3 9.5 3C5.91 3 3 5.91 3 9.5C3 13.09 5.91 16 9.5 16C11.11 16 12.59 15.41 13.73 14.43L14 14.71V15.5L19 20.49L20.49 19L15.5 14ZM9.5 14C7.01 14 5 11.99 5 9.5C5 7.01 7.01 5 9.5 5C11.99 5 14 7.01 14 9.5C14 11.99 11.99 14 9.5 14Z" fill="currentColor"/>
                            </svg>
                        </span>
                    </button>
                </form>
            </div>
        </header>
    );
};
