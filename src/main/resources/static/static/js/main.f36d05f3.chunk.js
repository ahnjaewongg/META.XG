(this["webpackJsonplol-match-history"]=this["webpackJsonplol-match-history"]||[]).push([[0],{18:function(e,t,a){},24:function(e,t,a){},26:function(e,t,a){},27:function(e,t,a){},28:function(e,t,a){},53:function(e,t,a){},54:function(e,t,a){},55:function(e,t,a){},56:function(e,t,a){},57:function(e,t,a){},58:function(e,t,a){"use strict";a.r(t);var s=a(1),c=a.n(s),n=a(19),i=a.n(n),r=(a(24),a(0));const o=e=>{let{onSearch:t}=e;const[a,c]=Object(s.useState)("");return Object(r.jsxs)("form",{onSubmit:e=>{e.preventDefault(),a.trim()&&t(a)},className:"search-bar",children:[Object(r.jsx)("input",{type:"text",value:a,onChange:e=>c(e.target.value),placeholder:"\uc18c\ud658\uc0ac\uba85#\ud0dc\uadf8 (\uc608: Hide on bush#KR1)","aria-label":"\uc18c\ud658\uc0ac \uac80\uc0c9"}),Object(r.jsx)("button",{type:"submit",children:"\uac80\uc0c9"})]})};a(26);const l=e=>{let{onSearch:t}=e;const a=()=>{window.location.href="/"};return Object(r.jsx)("header",{className:"app-header",children:Object(r.jsxs)("div",{className:"header-container",children:[Object(r.jsxs)("div",{className:"header-left",children:[Object(r.jsx)("h1",{className:"logo",onClick:a,children:"META.XG"}),Object(r.jsxs)("nav",{className:"main-nav",children:[Object(r.jsx)("button",{className:"nav-btn active",onClick:a,children:"\ud648"}),Object(r.jsx)("button",{className:"nav-btn",onClick:()=>{window.location.href="/champions"},children:"\ucc54\ud53c\uc5b8 \ubd84\uc11d"}),Object(r.jsx)("button",{className:"nav-btn",children:"\ub7ad\ud0b9"}),Object(r.jsx)("button",{className:"nav-btn",children:"\uba40\ud2f0\uc11c\uce58"})]})]}),Object(r.jsx)("div",{className:"header-center",children:Object(r.jsx)(o,{onSearch:t})}),Object(r.jsxs)("div",{className:"header-right",children:[Object(r.jsx)("button",{className:"setting-btn mode-toggle",children:Object(r.jsx)("img",{src:"/images/ico_moon.svg",alt:"\ub2e4\ud06c\ubaa8\ub4dc",width:"20",height:"20"})}),Object(r.jsx)("button",{className:"setting-btn language-toggle",children:Object(r.jsx)("img",{src:"/images/ico_globe.svg",alt:"\uc5b8\uc5b4 \ubcc0\uacbd",width:"20",height:"20"})})]})]})})};a(27),a(28);const d=()=>Object(r.jsxs)("div",{className:"game-filter",children:[Object(r.jsxs)("div",{className:"queue-filters",children:[Object(r.jsx)("button",{className:"filter-btn active",children:"\uc804\uccb4"}),Object(r.jsx)("button",{className:"filter-btn",children:"\uc194\ub85c\ub7ad\ud06c"}),Object(r.jsx)("button",{className:"filter-btn",children:"\uc790\uc720\ub7ad\ud06c"}),Object(r.jsx)("button",{className:"filter-btn",children:"\ube60\ub978\ub300\uc804"}),Object(r.jsx)("button",{className:"filter-btn",children:"\uce7c\ubc14\ub78c\ub098\ub77d"})]}),Object(r.jsx)("div",{className:"champion-search",children:Object(r.jsx)("input",{type:"text",placeholder:"\ucc54\ud53c\uc5b8 \uac80\uc0c9",className:"champion-input"})})]}),j=e=>{let{matches:t}=e;const[a,c]=Object(s.useState)(null);return Object(r.jsxs)("div",{className:"match-history",children:[Object(r.jsx)(d,{}),Object(r.jsx)("div",{className:"match-list",children:t.map((e=>{const s=(e=>{const a=t[0].info.participants[0].puuid;return e.info.participants.find((e=>e.puuid===a))})(e);return s?Object(r.jsxs)("div",{className:"match-card ".concat(s.win?"victory":"defeat"),onClick:()=>{return t=e.metadata.matchId,void c(a===t?null:t);var t},children:[Object(r.jsxs)("div",{className:"match-info",children:[Object(r.jsxs)("div",{className:"champion-info",children:[Object(r.jsx)("img",{src:"http://ddragon.leagueoflegends.com/cdn/13.24.1/img/champion/".concat(s.championName,".png"),alt:s.championName,className:"champion-icon"}),Object(r.jsx)("span",{children:s.championName})]}),Object(r.jsxs)("div",{className:"game-stats",children:[Object(r.jsxs)("div",{className:"kda",children:[s.kills," / ",s.deaths," / ",s.assists]}),Object(r.jsxs)("div",{className:"kda-ratio",children:[((s.kills+s.assists)/Math.max(1,s.deaths)).toFixed(2)," KDA"]})]}),Object(r.jsxs)("div",{className:"game-info",children:[Object(r.jsx)("div",{children:s.win?"\uc2b9\ub9ac":"\ud328\ubc30"}),Object(r.jsxs)("div",{children:[Math.floor(e.info.gameDuration/60),"\ubd84"]}),Object(r.jsx)("div",{children:new Date(e.info.gameCreation).toLocaleDateString()})]})]}),a===e.metadata.matchId&&Object(r.jsxs)("div",{className:"match-details",children:[Object(r.jsx)("div",{className:"items",children:s.items.map(((e,t)=>Object(r.jsx)("img",{src:"http://ddragon.leagueoflegends.com/cdn/13.1.1/img/item/".concat(e,".png"),alt:"Item ".concat(e)},t)))}),Object(r.jsxs)("div",{className:"additional-stats",children:[Object(r.jsxs)("p",{children:["CS: ",s.cs," (",(s.cs/(e.info.gameDuration/60)).toFixed(1),")"]}),Object(r.jsxs)("p",{children:["Gold: ",s.goldEarned.toLocaleString()]})]})]})]},e.metadata.matchId):null}))})]})};var h=a(3),m=a.n(h);const b={RIOT_API_KEY:"RGAPI-b036156f-fe4b-45e1-a7df-f0cab0392c6c",RIOT_API_BASE_URL:"https://kr.api.riotgames.com",API_BASE_URL:""};class u{constructor(){this.headers={"X-Riot-Token":b.RIOT_API_KEY}}async getSummonerByName(e){try{return(await m.a.get("".concat(b.API_BASE_URL,"/api/summoner/").concat(encodeURIComponent(e)),{headers:{Accept:"application/json","Content-Type":"application/json"}})).data}catch(t){throw console.error("Error fetching summoner:",t),t}}async getMatchList(e){let t=arguments.length>1&&void 0!==arguments[1]?arguments[1]:0,a=arguments.length>2&&void 0!==arguments[2]?arguments[2]:20;try{return(await m.a.get("".concat(b.RIOT_API_BASE_URL,"/lol/match/v5/matches/by-puuid/").concat(e,"/ids"),{headers:this.headers,params:{start:t,count:a}})).data}catch(s){throw console.error("Error fetching match list:",s),s}}async getMatchDetails(e){try{return(await m.a.get("".concat(b.RIOT_API_BASE_URL,"/lol/match/v5/matches/").concat(e),{headers:this.headers})).data}catch(t){throw console.error("Error fetching match details:",t),t}}async getMatchHistory(e){try{const t=encodeURIComponent(e.trim());console.log("Requesting:","".concat(b.API_BASE_URL,"/api/matches/by-summoner/").concat(t));return(await m.a.get("".concat(b.API_BASE_URL,"/api/matches/by-summoner/").concat(t),{headers:{Accept:"application/json","Content-Type":"application/json"},withCredentials:!0})).data}catch(a){if(m.a.isAxiosError(a)){var t;if(console.error("API \ud638\ucd9c \uc911 \uc624\ub958:",a.message),404===(null===(t=a.response)||void 0===t?void 0:t.status))throw new Error("\uc18c\ud658\uc0ac\ub97c \ucc3e\uc744 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4.");throw new Error("\uc11c\ubc84 \uc624\ub958\uac00 \ubc1c\uc0dd\ud588\uc2b5\ub2c8\ub2e4.")}throw a}}async getSummonerRank(e){try{return(await m.a.get("".concat(b.API_BASE_URL,"/api/summoner/rank/").concat(e),{headers:{Accept:"application/json","Content-Type":"application/json"}})).data}catch(t){throw console.error("Error fetching summoner rank:",t),t}}async getCurrentGame(e){try{return(await m.a.get("".concat(b.API_BASE_URL,"/api/summoner/current-game/").concat(e),{headers:{Accept:"application/json","Content-Type":"application/json"}})).data}catch(a){var t;if(m.a.isAxiosError(a)&&404===(null===(t=a.response)||void 0===t?void 0:t.status))return null;throw console.error("Error fetching current game:",a),a}}}a(18),a(53);const p=e=>{let{summonerName:t}=e;const[a,c]=Object(s.useState)(null),[n,i]=Object(s.useState)(null),[o,l]=Object(s.useState)(null),d=new u,[j,h]=t.split("#");if(Object(s.useEffect)((()=>{(async()=>{try{const e=await d.getSummonerByName(t);c(e);const a=await d.getSummonerRank(e.id);i(a);const s=await d.getCurrentGame(e.id);l(s)}catch(e){console.error("Error loading summoner data:",e)}})()}),[t]),!a)return null;return Object(r.jsxs)("div",{className:"profile-header",children:[Object(r.jsxs)("div",{className:"profile-icon",children:[Object(r.jsx)("img",{src:"https://ak-deeplol-ddragon-cdn.deeplol.gg/cdn/img/profileicon/".concat(a.profileIconId,"__100.webp"),alt:a.name}),Object(r.jsx)("span",{className:"level",children:a.summonerLevel})]}),Object(r.jsxs)("div",{className:"profile-info",children:[Object(r.jsxs)("div",{className:"name-update",children:[Object(r.jsxs)("div",{className:"name",children:[Object(r.jsx)("span",{children:j}),Object(r.jsxs)("span",{className:"tag",children:["#",h]})]}),Object(r.jsxs)("div",{className:"buttons",children:[Object(r.jsxs)("button",{className:"update-btn",children:[Object(r.jsxs)("svg",{width:"14",height:"14",viewBox:"0 0 24 24",fill:"none",xmlns:"http://www.w3.org/2000/svg",children:[Object(r.jsx)("path",{d:"M2 12C2 17.5228 6.47715 22 12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2",stroke:"currentColor",strokeWidth:"2",strokeLinecap:"round"}),Object(r.jsx)("path",{d:"M2 12C2 6.47715 6.47715 2 12 2",stroke:"currentColor",strokeWidth:"2",strokeLinecap:"round",strokeDasharray:"1 4"}),Object(r.jsx)("path",{d:"M2 4L5 7M5 7L8 4M5 7V4",stroke:"currentColor",strokeWidth:"2",strokeLinecap:"round",strokeLinejoin:"round"})]}),Object(r.jsx)("span",{children:"\uc804\uc801 \uac31\uc2e0"})]}),Object(r.jsxs)("button",{className:"favorite-btn",children:[Object(r.jsx)("svg",{width:"14",height:"14",viewBox:"0 0 24 24",fill:"none",xmlns:"http://www.w3.org/2000/svg",children:Object(r.jsx)("path",{d:"M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z",stroke:"currentColor",strokeWidth:"2",strokeLinecap:"round",strokeLinejoin:"round"})}),Object(r.jsx)("span",{children:"\uc990\uaca8\ucc3e\uae30"})]})]})]}),Object(r.jsx)("div",{className:"last-update",children:Object(r.jsx)("span",{children:"\ucd5c\uadfc \uac31\uc2e0: \ubc29\uae08 \uc804"})})]})]})};a(54);const O=e=>{let{rank:t,currentGame:a}=e;return Object(r.jsx)("div",{className:"summoner-content",children:Object(r.jsxs)("div",{className:"rank-section",children:[Object(r.jsxs)("div",{className:"rank-header",children:[Object(r.jsx)("span",{children:"\uc194\ub85c \ub7ad\ud06c"}),!t&&Object(r.jsx)("span",{className:"unranked",children:"Unranked"})]}),t&&Object(r.jsxs)("div",{className:"rank-info",children:[Object(r.jsx)("div",{className:"tier-emblem",children:Object(r.jsx)("img",{src:"https://raw.communitydragon.org/latest/plugins/rcp-fe-lol-static-assets/global/default/ranked-emblem/emblem-".concat(t.tier.toLowerCase(),".png"),alt:t.tier})}),Object(r.jsxs)("div",{className:"rank-details",children:[Object(r.jsxs)("div",{className:"tier-info",children:[Object(r.jsxs)("span",{className:"tier",children:[t.tier," ",t.rank]}),Object(r.jsxs)("span",{className:"lp",children:[t.leaguePoints," LP"]})]}),Object(r.jsxs)("div",{className:"record",children:[Object(r.jsxs)("span",{className:"wins",children:[t.wins,"\uc2b9"]}),Object(r.jsxs)("span",{className:"losses",children:[t.losses,"\ud328"]}),Object(r.jsxs)("span",{className:"win-rate",children:["\uc2b9\ub960 ",t?(t.wins/(t.wins+t.losses)*100).toFixed(1):null,"%"]})]})]})]})]})})};a(55);const x=e=>{let{onTabChange:t}=e;const[a,c]=Object(s.useState)("overview"),n=e=>{c(e),t(e)};return Object(r.jsx)("div",{className:"navigation-tabs",children:Object(r.jsxs)("div",{className:"tab-buttons",children:[Object(r.jsx)("button",{className:"tab ".concat("overview"===a?"active":""),onClick:()=>n("overview"),children:"\uc885\ud569"}),Object(r.jsx)("button",{className:"tab ".concat("champions"===a?"active":""),onClick:()=>n("champions"),children:"\ucc54\ud53c\uc5b8"}),Object(r.jsx)("button",{className:"tab ".concat("ingame"===a?"active":""),onClick:()=>n("ingame"),children:"\uc778\uac8c\uc784"})]})})},v=(e,t,a,s)=>e.filter((e=>{if("\uc804\uccb4"!==t){if((e=>{const t=e.charCodeAt(0)-44032;return t<0?e[0]:["\u3131","\u3132","\u3134","\u3137","\u3138","\u3139","\u3141","\u3142","\u3143","\u3145","\u3146","\u3147","\u3148","\u3149","\u314a","\u314b","\u314c","\u314d","\u314e"][Math.floor(t/28/21)]||e[0]})(e.nameKo)!==t)return!1}if("\uc804\uccb4"!==a&&!e.positions.includes(a))return!1;if(s){const t=s.toLowerCase();return e.nameKo.toLowerCase().includes(t)||e.name.toLowerCase().includes(t)}return!0}));a(56);const g=e=>{let{champion:t,onClick:a}=e;return Object(r.jsxs)("div",{className:"champion-card",onClick:()=>a(t),children:[Object(r.jsxs)("div",{className:"champion-image",children:[Object(r.jsx)("img",{src:t.imageUrl,alt:t.nameKo}),Object(r.jsx)("span",{className:"tier tier-".concat(t.tier.toLowerCase()),children:t.tier})]}),Object(r.jsxs)("div",{className:"champion-info",children:[Object(r.jsx)("h3",{children:t.nameKo}),Object(r.jsx)("div",{className:"positions",children:t.positions.map((e=>Object(r.jsx)("img",{src:"/images/positions/".concat(e.toLowerCase(),".svg"),alt:e,className:"position-icon"},e)))}),Object(r.jsxs)("div",{className:"stats",children:[Object(r.jsxs)("span",{className:"win-rate",children:["\uc2b9\ub960 ",t.winRate.toFixed(1),"%"]}),Object(r.jsxs)("span",{className:"pick-rate",children:["\ud53d\ub960 ",t.pickRate.toFixed(1),"%"]}),Object(r.jsxs)("span",{className:"ban-rate",children:["\ubc34\ub960 ",t.banRate.toFixed(1),"%"]})]})]})]})};a(57);var N=a(8);const f=()=>{const[e,t]=Object(s.useState)([]),[a,c]=Object(s.useState)([]),[n,i]=Object(s.useState)("\uc804\uccb4"),[o,l]=Object(s.useState)("\uc804\uccb4"),[d,j]=Object(s.useState)(""),[h,b]=Object(s.useState)("tier"),[u,p]=Object(s.useState)(!0),[O,x]=Object(s.useState)(null);Object(s.useEffect)((()=>{(async()=>{try{p(!0);const e=await(async()=>{try{return(await m.a.get("".concat("http://localhost:8080","/api/champions/stats"))).data.map((e=>Object(N.a)(Object(N.a)({},e),{},{winRate:Number(e.winRate.toFixed(1)),pickRate:Number(e.pickRate.toFixed(1)),banRate:Number(e.banRate.toFixed(1))})))}catch(O){throw console.error("Failed to fetch champion stats:",O),O}})();t(e),c(e)}catch(e){x("\ucc54\ud53c\uc5b8 \ub370\uc774\ud130\ub97c \ubd88\ub7ec\uc624\ub294\ub370 \uc2e4\ud328\ud588\uc2b5\ub2c8\ub2e4.")}finally{p(!1)}})()}),[]),Object(s.useEffect)((()=>{if(e.length>0){let t=v(e,n,o,d);t=((e,t)=>[...e].sort(((e,a)=>{switch(t){case"winRate":return a.winRate-e.winRate;case"pickRate":return a.pickRate-e.pickRate;case"banRate":return a.banRate-e.banRate;case"tier":return e.tier.localeCompare(a.tier);default:return 0}})))(t,h),c(t)}}),[e,n,o,d,h]);const f=e=>{console.log("Selected champion:",e)};return Object(r.jsx)("div",{className:"champion-analysis",children:u?Object(r.jsx)("div",{className:"loading",children:"\ub370\uc774\ud130\ub97c \ubd88\ub7ec\uc624\ub294 \uc911..."}):O?Object(r.jsx)("div",{className:"error",children:O}):Object(r.jsxs)(r.Fragment,{children:[Object(r.jsx)("div",{className:"champion-sidebar",children:Object(r.jsx)("div",{className:"initial-filters",children:["\uc804\uccb4","\u3131","\u3134","\u3137","\u3139","\u3141","\u3142","\u3145","\u3147","\u3148","\u314a","\u314b","\u314c","\u314d","\u314e"].map((e=>Object(r.jsx)("button",{className:"initial-btn ".concat(n===e?"active":""),onClick:()=>i(e),children:e},e)))})}),Object(r.jsxs)("div",{className:"champion-main",children:[Object(r.jsx)("div",{className:"champion-filters",children:Object(r.jsxs)("div",{className:"filter-header",children:[Object(r.jsx)("div",{className:"position-filters",children:["\uc804\uccb4","\ud0d1","\uc815\uae00","\ubbf8\ub4dc","\uc6d0\ub51c","\uc11c\ud3ff"].map((e=>Object(r.jsx)("button",{className:"position-btn ".concat(o===e?"active":""),onClick:()=>l(e),children:"\uc804\uccb4"===e?e:Object(r.jsx)("img",{src:"/images/positions/".concat(e.toLowerCase(),".svg"),alt:e,title:e})},e)))}),Object(r.jsxs)("div",{className:"search-sort",children:[Object(r.jsx)("input",{type:"text",placeholder:"\ucc54\ud53c\uc5b8 \uac80\uc0c9",value:d,onChange:e=>j(e.target.value),className:"champion-search"}),Object(r.jsxs)("select",{value:h,onChange:e=>b(e.target.value),className:"sort-select",children:[Object(r.jsx)("option",{value:"tier",children:"\ud2f0\uc5b4 \uc21c"}),Object(r.jsx)("option",{value:"winRate",children:"\uc2b9\ub960 \uc21c"}),Object(r.jsx)("option",{value:"pickRate",children:"\ud53d\ub960 \uc21c"}),Object(r.jsx)("option",{value:"banRate",children:"\ubc34\ub960 \uc21c"})]})]})]})}),Object(r.jsx)("div",{className:"champion-grid",children:a.map((e=>Object(r.jsx)(g,{champion:e,onClick:f},e.id)))})]})]})})};var w=()=>{const[e,t]=Object(s.useState)([]),[a,c]=Object(s.useState)(""),[n,i]=Object(s.useState)(!1),[o,d]=Object(s.useState)(null),[h,m]=Object(s.useState)(null),[b,v]=Object(s.useState)(null),[g,N]=Object(s.useState)("overview"),w=new u,k=window.location.pathname;return Object(r.jsxs)("div",{className:"app",children:[Object(r.jsx)(l,{onSearch:async e=>{c(e),i(!0),d(null);try{const a=await w.getSummonerByName(e),s=await w.getSummonerRank(a.id),c=await w.getCurrentGame(a.id),n=await w.getMatchHistory(e);m(s),v(c),t(n)}catch(o){d("\uc18c\ud658\uc0ac\ub97c \ucc3e\uc744 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4. \uc18c\ud658\uc0ac\uba85\uc744 \ud655\uc778\ud574\uc8fc\uc138\uc694."),t([])}finally{i(!1)}}}),"/champions"===k?Object(r.jsx)(f,{}):Object(r.jsxs)(r.Fragment,{children:[n&&Object(r.jsx)("div",{className:"loading",children:"\uac80\uc0c9\uc911..."}),o&&Object(r.jsx)("div",{className:"error-message",children:o}),a&&!o&&Object(r.jsxs)("div",{className:"content",children:[Object(r.jsx)(p,{summonerName:a}),Object(r.jsx)(x,{onTabChange:N}),Object(r.jsxs)("div",{className:"main-content",children:[Object(r.jsx)(O,{rank:h,currentGame:b}),"overview"===g&&Object(r.jsx)(j,{matches:e}),"champions"===g&&Object(r.jsx)("div",{children:"\ucc54\ud53c\uc5b8 \uc815\ubcf4"}),"ingame"===g&&Object(r.jsx)("div",{children:"\uc778\uac8c\uc784 \uc815\ubcf4"})]})]})]})]})};i.a.render(Object(r.jsx)(c.a.StrictMode,{children:Object(r.jsx)(w,{})}),document.getElementById("root"))}},[[58,1,2]]]);
//# sourceMappingURL=main.f36d05f3.chunk.js.map