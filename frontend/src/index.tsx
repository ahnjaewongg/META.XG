import './config/processPolyfill';  // process 폴리필 먼저 import
import './index.css';
import React from 'react';
import ReactDOM from 'react-dom';
import App from './App';
// BrowserRouter 다시 추가
import { BrowserRouter } from 'react-router-dom';

ReactDOM.render(
  <React.StrictMode>
    {/* BrowserRouter 다시 추가 */}
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </React.StrictMode>,
  document.getElementById('root')
); 