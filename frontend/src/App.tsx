import React, { useState, useEffect } from 'react';
import './App.css';
import GreetingComponent from './components/GreetingComponent';
import ChatComponent from './components/ChatComponent';
import ArenaComponent from './components/ArenaComponent';

function App() {
  const [activeTab, setActiveTab] = useState<'rest' | 'websocket' | 'arena'>('rest');

  return (
    <div className="App">
      <header className="App-header">
        <h1>Robot Wars Frontend</h1>
        <div className="tabs">
          <button 
            className={activeTab === 'rest' ? 'active' : ''} 
            onClick={() => setActiveTab('rest')}
          >
            REST API Demo
          </button>
          <button 
            className={activeTab === 'websocket' ? 'active' : ''} 
            onClick={() => setActiveTab('websocket')}
          >
            WebSocket Demo
          </button>
          <button 
            className={activeTab === 'arena' ? 'active' : ''} 
            onClick={() => setActiveTab('arena')}
          >
            Battle Arena
          </button>
        </div>
      </header>
      <main>
        {activeTab === 'rest' ? (
          <GreetingComponent />
        ) : activeTab === 'websocket' ? (
          <ChatComponent />
        ) : (
          <ArenaComponent battleId="test-battle-id" />
        )}
      </main>
      <footer>
        <p>Robot Wars - A Quarkus and React Application</p>
      </footer>
    </div>
  );
}

export default App;
