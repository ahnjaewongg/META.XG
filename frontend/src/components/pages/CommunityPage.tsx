import React, { useState, useEffect, useCallback } from 'react';
import {
  Container, 
  Typography, 
  Box, 
  Tabs, 
  Tab, 
  Paper, 
  List, 
  ListItem, 
  ListItemText, 
  Divider, 
  Chip, 
  Button, 
  TextField, 
  Dialog, 
  CircularProgress
} from '@mui/material';
import {
  Favorite as FavoriteIcon,
  FavoriteBorder as FavoriteBorderIcon,
  Visibility as VisibilityIcon,
  Comment as CommentIcon,
  AccessTime as AccessTimeIcon,
  Person as PersonIcon,
  Add as AddIcon,
  Close as CloseIcon,
  Whatshot as WhatshotIcon,
  Star as StarIcon,
  EmojiEvents as EmojiEventsIcon
} from '@mui/icons-material';
import styles from './CommunityPage.module.css';
import { RiotService } from '../../services/riotService';

interface Post {
  id: number;
  title: string;
  content: string;
  author: string;
  date: string;
  position: string;
  likes: number;
  comments: Comment[];
  views: number;
  isFeatured: boolean;
}

interface Comment {
  id: number;
  postId: number;
  author: string;
  content: string;
  date: string;
  likes: number;
}

const positions = [
  { value: "all", label: "전체", color: "#9aa4af", icon: "🏆" },
  { value: "top", label: "탑", color: "#ee5a52", icon: "🛡️" },
  { value: "jungle", label: "정글", color: "#3b7a57", icon: "🌲" },
  { value: "mid", label: "미드", color: "#1e90ff", icon: "⚡" },
  { value: "bot", label: "바텀", color: "#9370db", icon: "🏹" },
  { value: "support", label: "서폿", color: "#f08080", icon: "✨" },
];

// Position icons mapping
const positionIcons = {
  top: <img src="/images/positions/top.png" alt="Top" width="20" height="20" />,
  jungle: <img src="/images/positions/jungle.png" alt="Jungle" width="20" height="20" />,
  mid: <img src="/images/positions/mid.png" alt="Mid" width="20" height="20" />,
  bot: <img src="/images/positions/bot.png" alt="Bot" width="20" height="20" />,
  support: <img src="/images/positions/support.png" alt="Support" width="20" height="20" />,
  all: <EmojiEventsIcon fontSize="small" />
};

const CommunityPage: React.FC = () => {
  const [currentPosition, setCurrentPosition] = useState('all');
  const [posts, setPosts] = useState<Post[]>([]);
  const [filteredPosts, setFilteredPosts] = useState<Post[]>([]);
  const [popularPosts, setPopularPosts] = useState<Post[]>([]);
  const [selectedPost, setSelectedPost] = useState<Post | null>(null);
  const [newComment, setNewComment] = useState('');
  const [postDialogOpen, setPostDialogOpen] = useState(false);
  const [newPostDialogOpen, setNewPostDialogOpen] = useState(false);
  const [newPost, setNewPost] = useState<Partial<Post>>({
    title: '',
    content: '',
    position: 'all'
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const riotService = new RiotService();

  const positionLabels: { [key: string]: string } = {
    all: '전체',
    top: '탑',
    jungle: '정글',
    middle: '미드',
    bottom: '바텀',
    support: '서포터'
  };

  const positionCounts: { [key: string]: number } = {
    all: 0,
    top: 0,
    jungle: 0,
    middle: 0,
    bottom: 0,
    support: 0
  };

  // 데이터 로드 및 초기화
  useEffect(() => {
    fetchPosts();
    fetchPopularPosts();
  }, []);

  // 포스트 데이터 가져오기
  const fetchPosts = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await riotService.getCommunityPosts();
      setPosts(data);

      // 포지션별 게시글 수 계산
      const counts = { all: data.length, top: 0, jungle: 0, mid: 0, bot: 0, support: 0 };
      data.forEach((post: Post) => {
        if (counts[post.position as keyof typeof counts] !== undefined) {
          counts[post.position as keyof typeof counts]++;
        }
      });
      
      // 필터링된 포스트 설정
      filterPostsByPosition(data, currentPosition);
      
      // 포지션 카운트 설정
      Object.keys(positionCounts).forEach(key => {
        positionCounts[key] = counts[key as keyof typeof counts] || 0;
      });
      
    } catch (error) {
      console.error('Failed to fetch posts:', error);
      setError('게시글을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  // 인기 게시글 가져오기
  const fetchPopularPosts = async () => {
    try {
      const data = await riotService.getPopularPosts();
      // 좋아요 수를 기준으로 내림차순 정렬하여 상위 5개만 표시
      setPopularPosts(data.slice(0, 5));
    } catch (error) {
      console.error('Failed to fetch popular posts:', error);
      // 인기 게시글 로드 실패 시 빈 배열 설정
      setPopularPosts([]);
    }
  };

  // 포지션별 게시글 필터링
  const filterPostsByPosition = (allPosts: Post[], position: string) => {
    if (position === 'all') {
      setFilteredPosts(allPosts);
    } else {
      setFilteredPosts(allPosts.filter(post => post.position === position));
    }
  };

  // 포지션 변경 핸들러
  const handlePositionChange = (newPosition: string) => {
    setCurrentPosition(newPosition);
    filterPostsByPosition(posts, newPosition);
  };

  // 게시글 클릭 핸들러
  const handlePostClick = async (post: Post) => {
    try {
      // 게시글 상세 정보 가져오기
      const postDetails = await riotService.getPostById(post.id);
      setSelectedPost(postDetails);
      setPostDialogOpen(true);
    } catch (error) {
      console.error('Failed to fetch post details:', error);
      // API 호출 실패 시 현재 post 객체로 대체
      setSelectedPost(post);
      setPostDialogOpen(true);
    }
  };

  // 좋아요 버튼 클릭 핸들러
  const handleLikeClick = async (postId: number) => {
    try {
      await riotService.likePost(postId);
      
      // 메인 게시글 목록 업데이트
      setPosts(prevPosts => prevPosts.map(post => 
        post.id === postId 
          ? { ...post, likes: post.likes + 1 } 
          : post
      ));
      
      // 필터링된 게시글 목록 업데이트
      setFilteredPosts(prevPosts => prevPosts.map(post => 
        post.id === postId 
          ? { ...post, likes: post.likes + 1 } 
          : post
      ));
      
      // 인기 게시글 목록 업데이트
      setPopularPosts(prevPosts => prevPosts.map(post => 
        post.id === postId 
          ? { ...post, likes: post.likes + 1 } 
          : post
      ));
      
      // 선택된 게시글이 있고 ID가 일치하는 경우 업데이트
      if (selectedPost && selectedPost.id === postId) {
        setSelectedPost({
          ...selectedPost,
          likes: selectedPost.likes + 1
        });
      }
    } catch (error) {
      console.error('Failed to like post:', error);
      // 실패 시 사용자에게 알림 (실제 구현에서는 Snackbar 등으로 대체)
      alert('좋아요를 처리하는데 실패했습니다.');
    }
  };

  // 댓글 좋아요 버튼 클릭 핸들러
  const handleCommentLikeClick = async (postId: number, commentId: number) => {
    try {
      await riotService.likeComment(commentId);
      
      // 메인 게시글 목록의 댓글 업데이트
      setPosts(prevPosts => prevPosts.map(post => 
        post.id === postId 
          ? {
              ...post,
              comments: post.comments.map(comment => 
                comment.id === commentId 
                  ? { ...comment, likes: comment.likes + 1 } 
                  : comment
              )
            } 
          : post
      ));
      
      // 선택된 게시글의 댓글 업데이트
      if (selectedPost && selectedPost.id === postId) {
        setSelectedPost({
          ...selectedPost,
          comments: selectedPost.comments.map(comment => 
            comment.id === commentId 
              ? { ...comment, likes: comment.likes + 1 } 
              : comment
          )
        });
      }
    } catch (error) {
      console.error('Failed to like comment:', error);
      // 실패 시 사용자에게 알림
      alert('댓글 좋아요를 처리하는데 실패했습니다.');
    }
  };

  // 댓글 추가 핸들러
  const handleAddComment = async () => {
    if (!selectedPost || !newComment.trim()) return;
    
    try {
      setLoading(true);
      
      // 새 댓글 데이터 생성
      const commentData = {
        postId: selectedPost.id,
        content: newComment,
        author: '익명', // 실제 구현에서는 로그인한 사용자 정보 사용
        date: new Date().toISOString()
      };
      
      // API를 통한 댓글 추가
      const result = await riotService.addComment(selectedPost.id, commentData);
      
      // 새 댓글 객체 (API 응답 또는 임시 생성)
      const newCommentObj: Comment = result || {
        id: Math.floor(Math.random() * 10000), // 임시 ID
        postId: selectedPost.id,
        author: '익명',
        content: newComment,
        date: new Date().toISOString(),
        likes: 0
      };
      
      // 게시글의 댓글 목록 업데이트
      const updatedPost = {
        ...selectedPost,
        comments: [...selectedPost.comments, newCommentObj]
      };
      
      // 선택된 게시글 업데이트
      setSelectedPost(updatedPost);
      
      // 메인 게시글 목록 업데이트
      setPosts(prevPosts => prevPosts.map(post => 
        post.id === selectedPost.id ? updatedPost : post
      ));
      
      // 필터링된 게시글 목록 업데이트
      setFilteredPosts(prevPosts => prevPosts.map(post => 
        post.id === selectedPost.id ? updatedPost : post
      ));
      
      // 댓글 입력 필드 초기화
      setNewComment('');
    } catch (error) {
      console.error('Failed to add comment:', error);
      alert('댓글을 추가하는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  // 새 게시글 다이얼로그 열기
  const handleOpenPostDialog = () => {
    setNewPostDialogOpen(true);
  };

  // 새 게시글 다이얼로그 닫기
  const handleClosePostDialog = () => {
    setNewPostDialogOpen(false);
    setNewPost({
      title: '',
      content: '',
      position: 'all'
    });
  };

  // 새 게시글 추가 핸들러
  const handleAddPost = async () => {
    if (!newPost.title || !newPost.content || !newPost.position) return;
    
    try {
      setLoading(true);
      
      // 새 게시글 데이터 생성
      const postData = {
        ...newPost,
        author: '익명', // 실제 구현에서는 로그인한 사용자 정보 사용
        date: new Date().toISOString(),
        likes: 0,
        comments: [],
        views: 0,
        isFeatured: false
      };
      
      // API를 통한 게시글 추가
      const result = await riotService.createPost(postData);
      
      // 새 게시글 객체 (API 응답 또는 임시 생성)
      const newPostObj: Post = result || {
        id: Math.floor(Math.random() * 10000), // 임시 ID
        title: newPost.title || '',
        content: newPost.content || '',
        position: newPost.position || 'all',
        author: '익명',
        date: new Date().toISOString(),
        likes: 0,
        comments: [],
        views: 0,
        isFeatured: false
      };
      
      // 게시글 목록에 새 게시글 추가
      const updatedPosts = [newPostObj, ...posts];
      setPosts(updatedPosts);
      
      // 현재 선택된 포지션에 맞게 필터링된 목록 업데이트
      filterPostsByPosition(updatedPosts, currentPosition);
      
      // 포지션 카운트 업데이트
      positionCounts.all++;
      if (newPost.position && positionCounts[newPost.position as keyof typeof positionCounts] !== undefined) {
        positionCounts[newPost.position as keyof typeof positionCounts]++;
      }
      
      // 다이얼로그 닫기 및 폼 초기화
      handleClosePostDialog();
    } catch (error: any) {
      console.error('Failed to create post:', error);
      alert('게시글을 작성하는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  // 날짜 형식화 함수
  const formatDate = (dateString: string): string => {
    const date = new Date(dateString);
    const now = new Date();
    const diffTime = Math.abs(now.getTime() - date.getTime());
    const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));
    
    if (diffDays === 0) {
      // 오늘인 경우 시간 표시
      return `오늘 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
    } else if (diffDays < 7) {
      // 일주일 이내인 경우 n일 전 표시
      return `${diffDays}일 전`;
    } else {
      // 일주일 이상인 경우 년-월-일 표시
      return `${date.getFullYear()}.${(date.getMonth() + 1).toString().padStart(2, '0')}.${date.getDate().toString().padStart(2, '0')}`;
    }
  };

  return (
    <Container className={styles.container}>
      <Typography variant="h4" component="h1" sx={{ marginBottom: '1.5rem' }}className={styles.pageTitle}>
        League of Legends 커뮤니티
      </Typography>
      
      <div className={styles.mainGrid}>
        {/* 사이드 패널 */}
        <div className={styles.sidePanel}>
          {/* 포지션별 네비게이션 */}
          <div className={styles.positionNav}>
            <div className={styles.navHeader}>
              <Typography className={styles.navTitle}>
                <EmojiEventsIcon fontSize="small" />
                포지션별 게시판
              </Typography>
            </div>
            <div className={styles.positionGrid}>
              {['all', 'top', 'jungle', 'middle', 'bottom', 'support'].map((pos) => (
                <div 
                  key={pos} 
                  className={`${styles.positionCard} ${currentPosition === pos ? styles.positionCardActive : ''}`}
                  onClick={() => handlePositionChange(pos)}
                >
                  <div className={styles.positionIconWrapper}>
                    {pos === 'all' ? (
                      <EmojiEventsIcon fontSize="small" sx={{ color: 'var(--gold-accent)' }} />
                    ) : (
                      <img 
                        src={`/images/positions/${pos}.png`} 
                        alt={positionLabels[pos]} 
                        width="24" 
                        height="24" 
                      />
                    )}
                  </div>
                  <Typography className={styles.positionName}>
                    {positionLabels[pos]}
                  </Typography>
                  <div className={styles.positionCount}>
                    {positionCounts[pos]}
                  </div>
                </div>
              ))}
            </div>
          </div>
          
          {/* 인기 게시글 */}
          <div className={styles.popularPosts}>
            <div className={styles.popularHeader}>
              <Typography className={styles.popularTitle}>
                <WhatshotIcon fontSize="small" />
                인기 게시글
              </Typography>
            </div>
            <div className={styles.popularList}>
              {loading ? (
                <div className={styles.loadingContainer}>
                  <CircularProgress size={28} className={styles.spinner} />
                </div>
              ) : popularPosts.length > 0 ? (
                popularPosts.map((post) => (
                  <div 
                    key={post.id} 
                    className={styles.popularItem}
                    onClick={() => handlePostClick(post)}
                  >
                    <div className={styles.popularItemHeader}>
                      <span className={`${styles.positionBadge} ${styles[`${post.position}Badge`] || styles.allBadge}`}>
                        {positionLabels[post.position]}
                      </span>
                      {post.isFeatured && (
                        <StarIcon fontSize="small" sx={{ color: 'var(--gold-accent)', marginLeft: 'auto' }} />
                      )}
                    </div>
                    <Typography className={styles.popularItemTitle}>
                      {post.title}
                    </Typography>
                    <div className={styles.popularItemStats}>
                      <span className={styles.popularItemStat}>
                        <FavoriteIcon fontSize="small" />
                        {post.likes}
                      </span>
                      <span className={styles.popularItemStat}>
                        <CommentIcon fontSize="small" />
                        {post.comments.length}
                      </span>
                      <span className={styles.popularItemStat}>
                        <VisibilityIcon fontSize="small" />
                        {post.views}
                      </span>
                    </div>
                  </div>
                ))
              ) : (
                <Typography variant="body2" sx={{ padding: '1rem', color: 'var(--text-secondary)' }}>
                  인기 게시글이 없습니다.
                </Typography>
              )}
            </div>
          </div>
        </div>
        
        {/* 메인 컨텐츠 */}
        <div className={styles.contentContainer}>
          {/* 포지션 선택 탭 */}
          <div className={styles.positionTabsContainer}>
            <div className={styles.positionTabs}>
              {['all', 'top', 'jungle', 'middle', 'bottom', 'support'].map((pos) => (
                <button
                  key={pos}
                  className={`${styles.positionTab} ${currentPosition === pos ? styles.positionTabActive : ''}`}
                  onClick={() => handlePositionChange(pos)}
                >
                  <span className={styles.tabIcon}>
                    {pos === 'all' ? (
                      <EmojiEventsIcon fontSize="small" />
                    ) : (
                      <img 
                        src={`/images/positions/${pos}.png`} 
                        alt={positionLabels[pos]} 
                        width="16" 
                        height="16" 
                      />
                    )}
                  </span>
                  {positionLabels[pos]}
                </button>
              ))}
            </div>
          </div>
          
          {/* 헤더 및 글쓰기 버튼 */}
          <div className={styles.contentHeader}>
            <div className={styles.currentPosition}>
              {currentPosition === 'all' ? (
                <EmojiEventsIcon fontSize="medium" />
              ) : (
                <img 
                  src={`/images/positions/${currentPosition}.png`} 
                  alt={positionLabels[currentPosition]} 
                  width="24" 
                  height="24" 
                />
              )}
              {positionLabels[currentPosition]} 게시판
            </div>
            <button 
              className={styles.createPostButton}
              onClick={handleOpenPostDialog}
            >
              <AddIcon fontSize="small" />
              새 글 작성
            </button>
          </div>
          
          {/* 게시글 목록 */}
          <div className={styles.postsList}>
            <div className={styles.postsHeader}>
              <Typography className={styles.postsTitle}>
                <CommentIcon fontSize="small" />
                게시글 목록
              </Typography>
            </div>
            
            {loading ? (
              <div className={styles.loadingContainer}>
                <CircularProgress size={32} className={styles.spinner} />
              </div>
            ) : error ? (
              <div className={styles.noPostsMessage}>
                <Typography>{error}</Typography>
              </div>
            ) : filteredPosts.length > 0 ? (
              <div className={styles.postsGrid}>
                {filteredPosts.map((post) => (
                  <div 
                    key={post.id} 
                    className={styles.postItem}
                    onClick={() => handlePostClick(post)}
                  >
                    <div 
                      className={styles.postPosition}
                      data-position={post.position}
                    >
                      {post.position === 'all' ? (
                        <EmojiEventsIcon fontSize="small" sx={{ color: 'var(--gold-accent)' }} />
                      ) : (
                        <img 
                          src={`/images/positions/${post.position}.png`} 
                          alt={positionLabels[post.position]} 
                          width="18" 
                          height="18" 
                        />
                      )}
                    </div>
                    
                    <div className={styles.postContent}>
                      <Typography className={styles.postTitle}>
                        {post.title}
                        {post.comments.length > 0 && (
                          <span className={styles.commentCount}>{post.comments.length}</span>
                        )}
                      </Typography>
                      <div className={styles.postInfo}>
                        <div className={styles.postAuthor}>
                          <PersonIcon fontSize="small" />
                          {post.author}
                        </div>
                        <span>•</span>
                        <div>{formatDate(post.date)}</div>
                      </div>
                    </div>
                    
                    <div className={styles.postStats}>
                      <div className={styles.postDate}>
                        {new Date(post.date).toLocaleDateString()}
                      </div>
                      <div className={styles.postMetrics}>
                        <div className={styles.postMetric}>
                          <FavoriteIcon fontSize="small" />
                          {post.likes}
                        </div>
                        <div className={styles.postMetric}>
                          <CommentIcon fontSize="small" />
                          {post.comments.length}
                        </div>
                        <div className={styles.postMetric}>
                          <VisibilityIcon fontSize="small" />
                          {post.views}
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className={styles.noPostsMessage}>
                <div className={styles.emptyIcon}>
                  <CommentIcon fontSize="inherit" />
                </div>
                <Typography variant="h6">게시글이 없습니다</Typography>
                <Typography variant="body2">첫 번째 게시글을 작성해보세요!</Typography>
              </div>
            )}
          </div>
        </div>
      </div>
      
      {/* 게시글 상세 다이얼로그 */}
      <Dialog 
        open={postDialogOpen} 
        onClose={() => setPostDialogOpen(false)}
        maxWidth="md"
        fullWidth
        PaperProps={{
          className: styles.postDialog
        }}
      >
        {selectedPost && (
          <>
            <div className={styles.dialogHeader}>
              <Typography variant="h5" className={styles.dialogTitle}>
                {selectedPost.title}
              </Typography>
              <button 
                className={styles.closeButton}
                onClick={() => setPostDialogOpen(false)}
              >
                <CloseIcon />
              </button>
            </div>
            <div className={styles.dialogContent}>
              <div className={styles.postMeta}>
                <div className={styles.metaItem}>
                  <PersonIcon fontSize="small" />
                  {selectedPost.author}
                </div>
                <div className={styles.metaItem}>
                  <AccessTimeIcon fontSize="small" />
                  {formatDate(selectedPost.date)}
                </div>
                <div className={styles.metaItem}>
                  <span className={`${styles.positionBadge} ${styles[`${selectedPost.position}Badge`] || styles.allBadge}`}>
                    {positionLabels[selectedPost.position]}
                  </span>
                </div>
                <div className={styles.metaItem}>
                  <VisibilityIcon fontSize="small" />
                  {selectedPost.views} 조회
                </div>
              </div>
              
              <div className={styles.postBody}>
                {selectedPost.content}
              </div>
              
              <div className={styles.postActions}>
                <button 
                  className={styles.likeButton}
                  onClick={() => handleLikeClick(selectedPost.id)}
                >
                  <FavoriteIcon />
                  좋아요 {selectedPost.likes}
                </button>
              </div>
              
              <div className={styles.commentsDivider}></div>
              
              <div className={styles.commentsHeader}>
                <CommentIcon fontSize="small" />
                댓글 {selectedPost.comments.length}
              </div>
              
              {selectedPost.comments.length > 0 ? (
                <div className={styles.commentsList}>
                  {selectedPost.comments.map((comment) => (
                    <div key={comment.id} className={styles.commentItem}>
                      <div className={styles.commentHeader}>
                        <div className={styles.commentAuthorInfo}>
                          <div className={styles.commentAvatar}>
                            <PersonIcon fontSize="small" />
                          </div>
                          <div>
                            <div className={styles.commentAuthorName}>{comment.author}</div>
                            <div className={styles.commentDate}>{formatDate(comment.date)}</div>
                          </div>
                        </div>
                      </div>
                      <div className={styles.commentBody}>{comment.content}</div>
                      <div className={styles.commentActions}>
                        <button 
                          className={styles.commentLikeButton}
                          onClick={() => handleCommentLikeClick(selectedPost.id, comment.id)}
                        >
                          <FavoriteIcon fontSize="small" />
                          {comment.likes}
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className={styles.noCommentsMessage}>
                  아직 댓글이 없습니다. 첫 댓글을 작성해보세요!
                </div>
              )}
              
              <div className={styles.addCommentForm}>
                <textarea
                  className={styles.commentInput}
                  placeholder="댓글 작성"
                  value={newComment}
                  onChange={(e) => setNewComment(e.target.value)}
                />
                <button 
                  className={styles.commentSubmitButton}
                  onClick={handleAddComment}
                  disabled={loading || !newComment.trim()}
                >
                  {loading ? '게시 중...' : '댓글 작성'}
                </button>
              </div>
            </div>
          </>
        )}
      </Dialog>
      
      {/* 새 게시글 작성 다이얼로그 */}
      <Dialog 
        open={newPostDialogOpen} 
        onClose={handleClosePostDialog}
        maxWidth="md"
        fullWidth
        PaperProps={{
          className: styles.postDialog
        }}
      >
        <div className={styles.dialogHeader}>
          <Typography variant="h5" className={styles.dialogTitle}>
            새 게시글 작성
          </Typography>
          <button 
            className={styles.closeButton}
            onClick={handleClosePostDialog}
          >
            <CloseIcon />
          </button>
        </div>
        <div className={styles.dialogContent}>
          <div className={styles.newPostForm}>
            <div className={styles.formField}>
              <label className={styles.fieldLabel}>제목</label>
              <input
                type="text"
                className={styles.textInput}
                placeholder="제목을 입력하세요"
                value={newPost.title || ''}
                onChange={(e: React.ChangeEvent<HTMLInputElement>) => setNewPost({...newPost, title: e.target.value})}
              />
            </div>
            
            <div className={styles.formField}>
              <label className={styles.fieldLabel}>내용</label>
              <textarea
                className={styles.textArea}
                placeholder="내용을 입력하세요"
                value={newPost.content || ''}
                onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setNewPost({...newPost, content: e.target.value})}
              />
            </div>
            
            <div className={styles.formField}>
              <label className={styles.fieldLabel}>포지션</label>
              <div className={styles.positionOptions}>
                {['all', 'top', 'jungle', 'middle', 'bottom', 'support'].map((pos) => (
                  <div 
                    key={pos} 
                    className={`${styles.positionOption} ${newPost.position === pos ? styles.positionOptionSelected : ''}`}
                    onClick={() => setNewPost({...newPost, position: pos})}
                  >
                    {pos === 'all' ? (
                      <EmojiEventsIcon fontSize="small" />
                    ) : (
                      <img 
                        src={`/images/positions/${pos}.png`} 
                        alt={positionLabels[pos]} 
                        width="18" 
                        height="18" 
                      />
                    )}
                    {positionLabels[pos]}
                  </div>
                ))}
              </div>
            </div>
            
            <div className={styles.formActions}>
              <button 
                className={styles.cancelButton}
                onClick={handleClosePostDialog}
              >
                취소
              </button>
              <button 
                className={styles.submitButton}
                onClick={handleAddPost}
                disabled={loading || !newPost.title || !newPost.content}
              >
                {loading ? '게시 중...' : '게시하기'}
              </button>
            </div>
          </div>
        </div>
      </Dialog>
    </Container>
  );
};

export default CommunityPage;
export { CommunityPage }; 