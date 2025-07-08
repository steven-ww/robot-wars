import React, { useEffect, useState } from 'react';
import ArenaComponent from './ArenaComponent';

// Define interfaces for battles and robots
interface Robot {
  id: string;
  name: string;
  status: string;
}

interface Battle {
  id: string;
  name: string;
  arenaWidth: number;
  arenaHeight: number;
  robotMovementTimeSeconds: number;
  state: string;
  robotCount: number;
  robots: Robot[];
}

const ArenaTabComponent: React.FC = () => {
  const [battles, setBattles] = useState<Battle[]>([]);
  const [selectedBattleId, setSelectedBattleId] = useState<string | null>(null);
  const [selectedBattle, setSelectedBattle] = useState<Battle | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showBattleSelection, setShowBattleSelection] = useState(false);
  const [battleNotFound, setBattleNotFound] = useState(false);

  // Load battles and determine which battle to show
  useEffect(() => {
    const fetchBattles = async () => {
      try {
        setIsLoading(true);
        const response = await fetch('/api/battles');
        const data = await response.json();
        setBattles(data);

        // Get previously selected battle ID from localStorage
        const savedBattleId = localStorage.getItem('selectedBattleId');

        if (savedBattleId) {
          // Check if the saved battle still exists
          const savedBattle = data.find(
            (battle: Battle) => battle.id === savedBattleId
          );
          if (savedBattle) {
            setSelectedBattleId(savedBattleId);
            setSelectedBattle(savedBattle);
            setBattleNotFound(false);
          } else {
            // Previously selected battle no longer exists
            setBattleNotFound(true);
            setSelectedBattleId(null);
            setSelectedBattle(null);
            localStorage.removeItem('selectedBattleId');
          }
        } else if (data.length === 1) {
          // If there's exactly one battle and no previous selection, use it as default
          const defaultBattle = data[0];
          setSelectedBattleId(defaultBattle.id);
          setSelectedBattle(defaultBattle);
          setBattleNotFound(false);
        } else {
          // No previous selection and multiple or no battles available
          setSelectedBattleId(null);
          setSelectedBattle(null);
          setBattleNotFound(false);
        }
      } catch (error) {
        setError('Failed to fetch battles.');
      } finally {
        setIsLoading(false);
      }
    };

    fetchBattles();
  }, []);

  // Handle battle selection
  const handleSelectBattle = (battleId: string) => {
    const battle = battles.find(b => b.id === battleId);
    if (battle) {
      setSelectedBattleId(battleId);
      setSelectedBattle(battle);
      setShowBattleSelection(false);
      setBattleNotFound(false);
      localStorage.setItem('selectedBattleId', battleId);
    }
  };

  // Handle showing battle selection
  const handleShowBattleSelection = () => {
    setShowBattleSelection(true);
  };

  // Handle canceling battle selection
  const handleCancelBattleSelection = () => {
    setShowBattleSelection(false);
  };

  if (isLoading) {
    return (
      <div>
        <h2>Battle Arena</h2>
        <p>Loading battles...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div>
        <h2>Battle Arena</h2>
        <div style={{ color: 'red', marginBottom: '10px' }}>{error}</div>
      </div>
    );
  }

  // Show battle selection interface
  if (showBattleSelection) {
    return (
      <div>
        <h2>Battle Arena - Select Battle</h2>
        <p>Choose a battle to view its arena:</p>

        {battles.length === 0 ? (
          <p>No battles available.</p>
        ) : (
          <div style={{ marginBottom: '20px' }}>
            {battles.map(battle => (
              <div
                key={battle.id}
                style={{
                  border: '1px solid #ccc',
                  padding: '10px',
                  margin: '10px 0',
                  cursor: 'pointer',
                  backgroundColor: '#f9f9f9',
                }}
                onClick={() => handleSelectBattle(battle.id)}
              >
                <h4>{battle.name}</h4>
                <p>
                  <strong>Status:</strong> {battle.state}
                </p>
                <p>
                  <strong>Arena:</strong> {battle.arenaWidth} x{' '}
                  {battle.arenaHeight}
                </p>
                <p>
                  <strong>Robots:</strong> {battle.robotCount}
                </p>
                <button
                  data-testid={`select-battle-${battle.id}`}
                  onClick={e => {
                    e.stopPropagation();
                    handleSelectBattle(battle.id);
                  }}
                  style={{
                    padding: '5px 10px',
                    backgroundColor: '#007bff',
                    color: 'white',
                    border: 'none',
                    borderRadius: '3px',
                    cursor: 'pointer',
                  }}
                >
                  Select This Battle
                </button>
              </div>
            ))}
          </div>
        )}

        <button
          onClick={handleCancelBattleSelection}
          style={{
            padding: '10px 20px',
            backgroundColor: '#6c757d',
            color: 'white',
            border: 'none',
            borderRadius: '3px',
            cursor: 'pointer',
          }}
        >
          Cancel
        </button>
      </div>
    );
  }

  // Show arena for selected battle
  if (selectedBattle && selectedBattleId) {
    return (
      <div>
        <div style={{ marginBottom: '20px' }}>
          <h2>Battle Arena - {selectedBattle.name}</h2>
          <button
            onClick={handleShowBattleSelection}
            style={{
              padding: '10px 20px',
              backgroundColor: '#28a745',
              color: 'white',
              border: 'none',
              borderRadius: '3px',
              cursor: 'pointer',
              marginBottom: '10px',
            }}
          >
            Change Battle
          </button>
        </div>
        <div data-testid="arena-component">
          <ArenaComponent battleId={selectedBattleId} />
        </div>
      </div>
    );
  }

  // Show appropriate message when no battle is selected
  return (
    <div>
      <h2>Battle Arena</h2>

      {battleNotFound ? (
        <div>
          <p style={{ color: 'orange', marginBottom: '10px' }}>
            The previously selected battle is no longer available.
          </p>
          <button
            onClick={handleShowBattleSelection}
            style={{
              padding: '10px 20px',
              backgroundColor: '#007bff',
              color: 'white',
              border: 'none',
              borderRadius: '3px',
              cursor: 'pointer',
            }}
          >
            Select Battle
          </button>
        </div>
      ) : battles.length === 0 ? (
        <p>No battles available for arena view.</p>
      ) : (
        <div>
          <p>No battle selected for arena view.</p>
          <button
            onClick={handleShowBattleSelection}
            style={{
              padding: '10px 20px',
              backgroundColor: '#007bff',
              color: 'white',
              border: 'none',
              borderRadius: '3px',
              cursor: 'pointer',
            }}
          >
            Select Battle
          </button>
        </div>
      )}
    </div>
  );
};

export default ArenaTabComponent;
