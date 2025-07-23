import React, { useEffect, useState } from 'react';
import PhaserArenaComponent from './PhaserArenaComponent';
import { buildApiUrl } from '../utils/apiConfig';

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
  winnerId?: string;
  winnerName?: string;
}

const BattleManagement: React.FC = () => {
  const [battles, setBattles] = useState<Battle[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [newBattleName, setNewBattleName] = useState('');
  const [arenaWidth, setArenaWidth] = useState('');
  const [arenaHeight, setArenaHeight] = useState('');
  const [robotMovementTime, setRobotMovementTime] = useState('');
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  const [selectedBattleId, setSelectedBattleId] = useState<string | null>(null);
  const [battleToDelete, setBattleToDelete] = useState<Battle | null>(null);
  const [showDeleteConfirmation, setShowDeleteConfirmation] = useState(false);

  // Fetch battles from the backend API
  const fetchBattles = async () => {
    try {
      const response = await fetch(buildApiUrl('/api/battles'));
      const data = await response.json();
      setBattles(data);
      setError(null);
    } catch (error) {
      setError('Failed to fetch battles.');
    } finally {
      setIsLoading(false);
    }
  };

  // Initial fetch and setup automatic refresh
  useEffect(() => {
    fetchBattles();

    // Set up automatic refresh every 5 seconds
    const refreshInterval = setInterval(() => {
      fetchBattles();
    }, 5000);

    // Cleanup interval on component unmount
    return () => {
      clearInterval(refreshInterval);
    };
  }, []);

  // Handle creating a new battle
  const handleCreateBattle = async () => {
    if (!newBattleName.trim()) {
      setError('Battle name is required.');
      return;
    }

    try {
      const requestBody: any = { name: newBattleName.trim() };

      if (arenaWidth) {
        requestBody.width = parseInt(arenaWidth, 10);
      }
      if (arenaHeight) {
        requestBody.height = parseInt(arenaHeight, 10);
      }
      if (robotMovementTime) {
        requestBody.robotMovementTimeSeconds = parseFloat(robotMovementTime);
      }

      const response = await fetch(buildApiUrl('/api/battles'), {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestBody),
      });

      if (response.ok) {
        const newBattle = await response.json();
        setBattles(prevBattles => [...prevBattles, newBattle]);
        setSuccessMessage(`Battle "${newBattle.name}" created successfully!`);

        // Reset form
        setNewBattleName('');
        setArenaWidth('');
        setArenaHeight('');
        setRobotMovementTime('');
        setShowCreateForm(false);
        setError(null);

        // Clear success message after 3 seconds
        setTimeout(() => setSuccessMessage(''), 3000);
      } else {
        const errorData = await response.json();
        setError(errorData.message || 'Failed to create a new battle.');
      }
    } catch (error) {
      setError('Failed to create a new battle.');
    }
  };

  // Handle viewing arena for a selected battle
  const handleViewArena = (battleId: string) => {
    setSelectedBattleId(battleId);
  };

  // Handle going back to battle list
  const handleBackToBattleList = () => {
    setSelectedBattleId(null);
  };

  // Handle initiating battle deletion
  const handleDeleteBattle = (battle: Battle) => {
    setBattleToDelete(battle);
    setShowDeleteConfirmation(true);
  };

  // Handle confirming battle deletion
  const handleConfirmDelete = async () => {
    if (!battleToDelete) return;

    try {
      const response = await fetch(
        buildApiUrl(`/api/battles/${battleToDelete.id}`),
        {
          method: 'DELETE',
        }
      );

      if (response.ok) {
        // Remove the battle from the list
        setBattles(prevBattles =>
          prevBattles.filter(battle => battle.id !== battleToDelete.id)
        );
        setSuccessMessage(
          `Battle '${battleToDelete.name}' has been successfully deleted`
        );

        // Clear success message after 3 seconds
        setTimeout(() => setSuccessMessage(''), 3000);
      } else {
        setError('Failed to delete battle. Please try again.');
      }
    } catch (error) {
      setError('Failed to delete battle. Please try again.');
    } finally {
      setShowDeleteConfirmation(false);
      setBattleToDelete(null);
    }
  };

  // Handle canceling battle deletion
  const handleCancelDelete = () => {
    setShowDeleteConfirmation(false);
    setBattleToDelete(null);
  };

  // If a battle is selected, show the arena component
  if (selectedBattleId) {
    return (
      <div>
        <button
          onClick={handleBackToBattleList}
          style={{ marginBottom: '20px' }}
        >
          Back to Battle List
        </button>
        <div data-testid="arena-component">
          <PhaserArenaComponent battleId={selectedBattleId} />
        </div>
      </div>
    );
  }

  return (
    <div>
      <h2>Battle Management</h2>

      {successMessage && (
        <div style={{ color: 'green', marginBottom: '10px' }}>
          {successMessage}
        </div>
      )}

      {error && (
        <div style={{ color: 'red', marginBottom: '10px' }}>{error}</div>
      )}

      {isLoading ? (
        <p>Loading battles...</p>
      ) : battles.length === 0 ? (
        <p>No battles found</p>
      ) : (
        <div>
          <h3>Current Battles</h3>
          <div style={{ marginBottom: '20px' }}>
            {battles.map(battle => (
              <div
                key={battle.id}
                style={{
                  border: '1px solid #ccc',
                  padding: '10px',
                  margin: '10px 0',
                }}
              >
                <h4>{battle.name}</h4>
                <p>
                  <strong>Battle ID:</strong> {battle.id}
                </p>
                <p>
                  <strong>Status:</strong> {battle.state}
                </p>
                {battle.state === 'COMPLETED' && battle.winnerName && (
                  <p>
                    <strong>Winner:</strong> {battle.winnerName}
                  </p>
                )}
                <p>
                  <strong>Arena:</strong> {battle.arenaWidth} x{' '}
                  {battle.arenaHeight}
                </p>
                <p>
                  <strong>Robot Movement Time:</strong>{' '}
                  {battle.robotMovementTimeSeconds}s
                </p>
                <p>
                  <strong>Robots ({battle.robotCount}):</strong>
                </p>
                {battle.robots.length > 0 ? (
                  <ul>
                    {battle.robots.map(robot => (
                      <li key={robot.id}>
                        {robot.name} - Status: {robot.status}
                      </li>
                    ))}
                  </ul>
                ) : (
                  <p>No robots registered</p>
                )}
                <div style={{ marginTop: '10px' }}>
                  <button
                    onClick={() => handleViewArena(battle.id)}
                    data-testid={`view-arena-${battle.id}`}
                    style={{
                      marginRight: '10px',
                      padding: '5px 10px',
                      backgroundColor: '#007bff',
                      color: 'white',
                      border: 'none',
                      borderRadius: '3px',
                      cursor: 'pointer',
                    }}
                  >
                    View Arena
                  </button>
                  {battle.state === 'COMPLETED' && (
                    <button
                      onClick={() => handleDeleteBattle(battle)}
                      data-testid={`delete-battle-${battle.id}`}
                      style={{
                        padding: '5px 10px',
                        backgroundColor: '#dc3545',
                        color: 'white',
                        border: 'none',
                        borderRadius: '3px',
                        cursor: 'pointer',
                      }}
                    >
                      Delete
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      <div>
        {!showCreateForm ? (
          <button onClick={() => setShowCreateForm(true)}>Create Battle</button>
        ) : (
          <div
            style={{
              border: '1px solid #ccc',
              padding: '20px',
              marginTop: '20px',
            }}
          >
            <h3>Create a New Battle</h3>

            <div style={{ marginBottom: '10px' }}>
              <label>Battle Name (required):</label>
              <input
                type="text"
                placeholder="Enter battle name"
                value={newBattleName}
                onChange={e => setNewBattleName(e.target.value)}
                style={{ width: '100%', padding: '5px' }}
              />
            </div>

            <div style={{ marginBottom: '10px' }}>
              <label>Arena Width (optional):</label>
              <input
                type="number"
                placeholder="Default arena width"
                value={arenaWidth}
                onChange={e => setArenaWidth(e.target.value)}
                style={{ width: '100%', padding: '5px' }}
                min="10"
                max="1000"
              />
            </div>

            <div style={{ marginBottom: '10px' }}>
              <label>Arena Height (optional):</label>
              <input
                type="number"
                placeholder="Default arena height"
                value={arenaHeight}
                onChange={e => setArenaHeight(e.target.value)}
                style={{ width: '100%', padding: '5px' }}
                min="10"
                max="1000"
              />
            </div>

            <div style={{ marginBottom: '10px' }}>
              <label>Robot Movement Time (seconds, optional):</label>
              <input
                type="number"
                step="0.1"
                placeholder="Default movement time"
                value={robotMovementTime}
                onChange={e => setRobotMovementTime(e.target.value)}
                style={{ width: '100%', padding: '5px' }}
                min="0.1"
              />
            </div>

            <div>
              <button
                onClick={handleCreateBattle}
                style={{ marginRight: '10px' }}
              >
                Create
              </button>
              <button
                onClick={() => {
                  setShowCreateForm(false);
                  setNewBattleName('');
                  setArenaWidth('');
                  setArenaHeight('');
                  setRobotMovementTime('');
                  setError(null);
                }}
              >
                Cancel
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Delete Confirmation Dialog */}
      {showDeleteConfirmation && battleToDelete && (
        <div
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.5)',
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            zIndex: 1000,
          }}
        >
          <div
            style={{
              backgroundColor: 'white',
              padding: '20px',
              borderRadius: '5px',
              maxWidth: '400px',
              width: '90%',
            }}
          >
            <h3>Confirm Deletion</h3>
            <p>
              Are you sure you want to delete the battle '{battleToDelete.name}
              '? This action cannot be undone.
            </p>
            <div style={{ marginTop: '20px', textAlign: 'right' }}>
              <button
                onClick={handleCancelDelete}
                style={{
                  marginRight: '10px',
                  padding: '8px 16px',
                  backgroundColor: '#6c757d',
                  color: 'white',
                  border: 'none',
                  borderRadius: '3px',
                  cursor: 'pointer',
                }}
              >
                Cancel
              </button>
              <button
                onClick={handleConfirmDelete}
                style={{
                  padding: '8px 16px',
                  backgroundColor: '#dc3545',
                  color: 'white',
                  border: 'none',
                  borderRadius: '3px',
                  cursor: 'pointer',
                }}
              >
                Confirm
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default BattleManagement;
