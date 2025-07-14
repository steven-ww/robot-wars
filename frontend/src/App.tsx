import React, { useState } from 'react';
import './App.css';
import GreetingComponent from './components/GreetingComponent';
import ArenaTabComponent from './components/ArenaTabComponent';
import BattleManagement from './components/BattleManagement';

function App() {
  const [activeTab, setActiveTab] = useState<'rest' | 'arena' | 'battles'>(
    'battles'
  );

  return (
    <div className="App">
      <header className="App-header">
        <h1>Robot Wars Frontend</h1>
        <div className="tabs">
          <button
            className={activeTab === 'battles' ? 'active' : ''}
            onClick={() => setActiveTab('battles')}
          >
            Battle Management
          </button>
          <button
            className={activeTab === 'rest' ? 'active' : ''}
            onClick={() => setActiveTab('rest')}
          >
            API Documentation
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
        {activeTab === 'battles' ? (
          <BattleManagement />
        ) : activeTab === 'rest' ? (
          <GreetingComponent />
        ) : (
          <ArenaTabComponent />
        )}
      </main>
      <footer>
        <p>Robot Wars - A Quarkus and React Application</p>
      </footer>
    </div>
  );
}

export default App;
