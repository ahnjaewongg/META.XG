// process 객체가 없을 경우 전역으로 정의 (브라우저 환경에서 폴리필)
declare global {
  interface Window {
    process?: {
      env?: {
        NODE_ENV?: string;
        REACT_APP_API_BASE_URL?: string;
        REACT_APP_API_URL?: string;
        [key: string]: string | undefined;
      };
    };
  }
}

if (typeof window !== 'undefined') {
  window.process = window.process || {};
  window.process.env = window.process.env || {
    NODE_ENV: 'development',
    REACT_APP_API_BASE_URL: 'http://localhost:8080',
    REACT_APP_API_URL: 'http://localhost:8080',
  };
}

export {};
