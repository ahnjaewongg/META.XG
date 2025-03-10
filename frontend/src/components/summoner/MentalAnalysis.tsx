import React, { useState, useEffect } from 'react';
import { Card, CardContent, Typography, Box, CircularProgress, Divider, List, ListItem, ListItemIcon, ListItemText, Chip, Alert, Paper, Button, AlertTitle } from '@mui/material';
import { Psychology, Warning, Insights, ReportProblem, SentimentSatisfied, SentimentVeryDissatisfied, HealthAndSafety, LocalHospital, AccessTime } from '@mui/icons-material';
import { RiotService } from '../../services/riotService';
import { useNavigate } from 'react-router-dom';
import styles from './MentalAnalysis.module.css';

interface LosingStreakAnalysis {
  currentLosingStreak: number;
  maxLosingStreak: number;
  inLosingStreak: boolean;
  message: string;
}

interface NegativePattern {
  type: string;
  message: string;
}

interface MentalCareRecommendation {
  type: string;
  message: string;
  explanation: string;
}

interface MentalData {
  losingStreak: LosingStreakAnalysis;
  negativePatterns: NegativePattern[];
  tiltProbability: number;
  recommendations: MentalCareRecommendation[];
  error?: string;
  suggestedSearches?: string[];
  foundSummonerName?: string;
}

interface MentalAnalysisProps {
  summonerName: string;
}

/**
 * 소환사의 심리적 상태 분석 및 멘탈 케어 추천사항을 보여주는 컴포넌트
 */
export const MentalAnalysis: React.FC<MentalAnalysisProps> = ({ summonerName }) => {
  const [mentalData, setMentalData] = useState<MentalData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();
  const riotService = new RiotService();
  
  // 검색한 이름과 실제 찾은 이름이 다를 경우 표시할 메시지
  const [showNameMismatch, setShowNameMismatch] = useState(false);
  const [actualSummonerName, setActualSummonerName] = useState<string | null>(null);

  // 에러가 "소환사를 찾을 수 없습니다" 메시지인지 확인
  const isSummonerNotFoundError = error?.includes("소환사를 찾을 수 없습니다");

  // 소환사 멘탈 데이터 불러오기
  useEffect(() => {
    if (!summonerName) return;

    const fetchMentalData = async () => {
      setLoading(true);
      setError(null);
      
      try {
        console.log('Fetching mental analysis for summoner:', summonerName);
        const response = await riotService.analyzeMentalState(summonerName);
        console.log('Mental analysis response:', response);
        
        if (response.error) {
          console.log('Mental analysis API error:', response.error);
          setError(response.error);
        } else {
          setMentalData(response);
          
          // 검색한 이름과 찾은 이름이 다른지 확인
          if (response.foundSummonerName && 
              response.foundSummonerName.toLowerCase() !== summonerName.toLowerCase()) {
            setShowNameMismatch(true);
            setActualSummonerName(response.foundSummonerName);
          }
        }
      } catch (err) {
        console.error('Error in mental analysis:', err);
        setError('맨탈 분석 데이터를 가져오는 중 오류가 발생했습니다.');
      } finally {
        setLoading(false);
      }
    };

    fetchMentalData();
  }, [summonerName]);

  // 틸트 확률을 색상으로 표현
  const getTiltColor = (probability: number): string => {
    if (probability < 0.3) return '#4caf50'; // 낮은 틸트 - 초록
    if (probability < 0.6) return '#ff9800'; // 중간 틸트 - 주황
    return '#f44336'; // 높은 틸트 - 빨강
  };

  // 틸트 확률에 따른 CSS 클래스 반환
  const getTiltColorClass = (probability: number): string => {
    if (probability < 0.3) return styles.progressBarLow;
    if (probability < 0.6) return styles.progressBarMedium;
    return styles.progressBarHigh;
  };

  // 틸트 확률에 따른 메시지 반환
  const getTiltMessage = (probability: number): string => {
    if (probability < 0.3) return '안정적인 상태입니다';
    if (probability < 0.6) return '주의가 필요한 상태입니다';
    return '틸트 위험이 높은 상태입니다';
  };

  // 추천사항 아이콘 선택
  const getRecommendationIcon = (type: string): React.ReactNode => {
    switch (type) {
      case 'BREAK_TIME':
        return <AccessTime />;
      case 'GAME_MODE_CHANGE':
        return <Insights />;
      case 'PLAYSTYLE_ADJUSTMENT':
        return <Psychology />;
      case 'FOCUS_IMPROVEMENT':
        return <Insights />;
      case 'STRATEGIC_FOCUS':
        return <Insights />;
      case 'MINDFULNESS':
        return <HealthAndSafety />;
      case 'POSITIVE_REINFORCEMENT':
        return <SentimentSatisfied />;
      default:
        return <Psychology />;
    }
  };

  // 패턴 유형 아이콘 선택
  const getPatternIcon = (type: string): React.ReactNode => {
    switch (type) {
      case 'HIGH_DEATH_RATIO':
        return <LocalHospital color="error" />;
      case 'EARLY_TOWER_LOSS':
        return <ReportProblem color="warning" />;
      case 'CS_DECLINE':
        return <Warning color="warning" />;
      case 'VISION_DECLINE':
        return <Insights color="warning" />;
      default:
        return <Psychology />;
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <Typography variant="h5" component="h1" className={styles.title}>
          {summonerName} - 멘탈 분석
        </Typography>
        <Typography variant="subtitle1" className={styles.subtitle}>
          최근 게임을 바탕으로 소환사의 멘탈 상태를 분석합니다.
        </Typography>
      </div>

      {loading ? (
        <div className={styles.loadingContainer}>
          <CircularProgress sx={{ color: 'var(--accent-color)' }} />
        </div>
      ) : error ? (
        <Alert 
          severity="error" 
          className={styles.alertMargin}
          sx={{ 
            backgroundColor: 'rgba(244, 67, 54, 0.15)', 
            color: '#f44336',
            '& .MuiAlert-icon': { color: '#f44336' }
          }}
        >
          <AlertTitle sx={{ color: '#f44336', fontWeight: 600 }}>분석 오류</AlertTitle>
          {isSummonerNotFoundError ? (
            <>
              <Typography variant="body1" gutterBottom sx={{ color: 'var(--text-primary)' }}>
                {error}
              </Typography>
              <Typography variant="body2" sx={{ color: 'var(--text-secondary)' }} gutterBottom>
                소환사 이름을 정확히 입력했는지 확인해주세요. 이름에 공백이 있거나 태그라인(#KR1 등)을 포함해야 할 수 있습니다.
              </Typography>
              <Button
                variant="outlined"
                size="small"
                sx={{ 
                  mt: 1, 
                  color: 'var(--gold-accent)', 
                  borderColor: 'var(--gold-accent)',
                  '&:hover': {
                    borderColor: 'var(--gold-accent)',
                    backgroundColor: 'rgba(201, 170, 113, 0.08)'
                  }
                }}
                onClick={() => navigate('/')}
              >
                다시 검색하기
              </Button>
            </>
          ) : (
            error
          )}
        </Alert>
      ) : !mentalData ? (
        <Alert 
          severity="info" 
          className={styles.alertMargin}
          sx={{ 
            backgroundColor: 'rgba(29, 169, 204, 0.15)', 
            color: 'var(--accent-color)',
            '& .MuiAlert-icon': { color: 'var(--accent-color)' }
          }}
        >
          <AlertTitle sx={{ color: 'var(--accent-color)', fontWeight: 600 }}>데이터 없음</AlertTitle>
          분석할 데이터가 없습니다.
        </Alert>
      ) : (
        <>
          {/* 이름 불일치 알림 */}
          {showNameMismatch && actualSummonerName && (
            <Alert 
              severity="info" 
              className={styles.alertMargin}
              sx={{ 
                backgroundColor: 'rgba(29, 169, 204, 0.15)', 
                color: 'var(--accent-color)',
                '& .MuiAlert-icon': { color: 'var(--accent-color)' }
              }}
            >
              <AlertTitle sx={{ color: 'var(--accent-color)', fontWeight: 600 }}>소환사 이름 확인</AlertTitle>
              <Typography variant="body2" sx={{ color: 'var(--text-primary)' }}>
                검색하신 "{summonerName}"의 정확한 소환사 이름은 <strong>{actualSummonerName}</strong>입니다.
              </Typography>
            </Alert>
          )}

          {/* 개발 중 알림 표시 - 실제 분석 데이터가 없을 때 */}
          {mentalData.recommendations.some(rec => rec.explanation.includes('개발 중')) && (
            <Alert 
              severity="info" 
              className={styles.alertMargin}
              icon={<Psychology sx={{ color: 'var(--accent-color)' }} />}
              sx={{ 
                backgroundColor: 'rgba(29, 169, 204, 0.08)', 
                color: 'var(--accent-color)',
                border: '1px solid rgba(29, 169, 204, 0.3)',
                borderLeft: '4px solid var(--accent-color)',
                '& .MuiAlert-icon': { color: 'var(--accent-color)' }
              }}
            >
              <AlertTitle sx={{ 
                color: 'var(--accent-color)', 
                fontWeight: 600,
                display: 'flex',
                alignItems: 'center',
                gap: '8px'
              }}>
                <span>개발 중인 기능</span>
                <Chip
                  label="Beta"
                  size="small"
                  sx={{
                    backgroundColor: 'rgba(29, 169, 204, 0.2)',
                    color: 'var(--accent-color)',
                    height: '20px',
                    fontSize: '0.7rem',
                    fontWeight: 'bold'
                  }}
                />
              </AlertTitle>
              <Typography variant="body2" sx={{ 
                color: 'var(--text-primary)',
                lineHeight: 1.6,
                '& strong': { color: 'var(--gold-accent)' }
              }}>
                맨탈 분석 기능은 현재 <strong>베타 단계</strong>입니다. 더 정확한 분석과 맞춤형 추천을 위해 계속 개선 중입니다. 피드백은 언제든 환영합니다!
              </Typography>
            </Alert>
          )}

          {/* 틸트 확률 */}
          <Paper className={styles.paper}>
            <Typography variant="h6" className={styles.sectionTitle}>
              <SentimentVeryDissatisfied sx={{ mr: 1, verticalAlign: 'middle' }} />
              틸트 확률
            </Typography>
            <div className={styles.tiltProgressContainer}>
              <div className={styles.progressBar}>
                <div 
                  style={{ 
                    width: `${mentalData.tiltProbability * 100}%`, 
                    height: '100%',
                    borderRadius: '5px'
                  }}
                  className={getTiltColorClass(mentalData.tiltProbability)}
                ></div>
              </div>
              <div className={styles.percentageBox}>
                <Typography variant="body2" className={styles.percentageText}>
                  {Math.round(mentalData.tiltProbability * 100)}%
                </Typography>
              </div>
            </div>
            
            <Typography variant="body2" className={styles.messageText}>
              {getTiltMessage(mentalData.tiltProbability)}
            </Typography>
            
            {/* 연패 정보 */}
            {mentalData.losingStreak && (
              <div className={styles.chipContainer}>
                <Chip 
                  icon={mentalData.losingStreak.inLosingStreak ? <Warning fontSize="small" /> : <SentimentSatisfied fontSize="small" />}
                  label={`${mentalData.losingStreak.inLosingStreak ? '현재 연패중' : '연패중이 아님'}`}
                  className={`${styles.chip} ${mentalData.losingStreak.inLosingStreak ? styles.chipError : styles.chipSuccess}`}
                  size="small"
                />
                <Chip 
                  icon={<LocalHospital fontSize="small" />}
                  label={`최대 연패: ${mentalData.losingStreak.maxLosingStreak}게임`}
                  className={`${styles.chip} ${styles.chipDefault}`}
                  size="small"
                />
                <Chip 
                  icon={<AccessTime fontSize="small" />}
                  label={`현재 연패: ${mentalData.losingStreak.currentLosingStreak}게임`}
                  className={`${styles.chip} ${styles.chipDefault}`}
                  size="small"
                />
                <Typography variant="body2" className={styles.messageContainer}>
                  {mentalData.losingStreak.message}
                </Typography>
              </div>
            )}
          </Paper>

          {/* 부정적 패턴 */}
          {mentalData.negativePatterns.length > 0 && (
            <Paper className={styles.paper}>
              <Typography variant="h6" className={styles.sectionTitle}>
                <ReportProblem sx={{ mr: 1, verticalAlign: 'middle' }} />
                부정적 패턴
              </Typography>
              <List className={styles.list}>
                {mentalData.negativePatterns.map((pattern, index) => (
                  <ListItem key={index} className={styles.listItem}>
                    <ListItemIcon className={styles.listItemIcon}>
                      {getPatternIcon(pattern.type)}
                    </ListItemIcon>
                    <ListItemText 
                      primary={
                        <Typography variant="body1" sx={{ 
                          color: 'var(--text-primary)',
                          fontWeight: 500
                        }}>
                          {pattern.message}
                        </Typography>
                      }
                    />
                  </ListItem>
                ))}
              </List>
            </Paper>
          )}

          {/* 멘탈 케어 추천 */}
          <Paper className={styles.paper}>
            <Typography variant="h6" className={styles.sectionTitle}>
              <HealthAndSafety sx={{ mr: 1, verticalAlign: 'middle' }} />
              멘탈 케어 추천
            </Typography>
            <List className={styles.list}>
              {mentalData.recommendations.map((rec, index) => (
                <ListItem key={index} className={`${styles.listItem} ${styles.recommendationItem}`}>
                  <ListItemIcon className={styles.listItemIcon}>
                    {getRecommendationIcon(rec.type)}
                  </ListItemIcon>
                  <ListItemText 
                    primary={
                      <Typography variant="body1" sx={{ 
                        color: 'var(--gold-accent)',
                        fontWeight: 500
                      }}>
                        {rec.message}
                      </Typography>
                    }
                    secondary={
                      <Typography variant="body2" className={styles.secondaryText}>
                        {rec.explanation}
                      </Typography>
                    }
                  />
                </ListItem>
              ))}
            </List>
          </Paper>
        </>
      )}
    </div>
  );
}; 