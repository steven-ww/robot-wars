import React, { useEffect, useState } from 'react';

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

  // Fetch battles from the backend API
  useEffect(() => {
    const fetchBattles = async () => {
      try {
        const response = await fetch('/api/battles');
        const data = await response.json();
        setBattles(data);
      } catch (error) {
        setError('Failed to fetch battles.');
      } finally {
        setIsLoading(false);
      }
    };

    fetchBattles();
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

      const response = await fetch('/api/battles', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestBody),
      });
      
      if (response.ok) {
        const newBattle = await response.json();
        setBattles((prevBattles) => [...prevBattles, newBattle]);
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

  return (
    <div>
      <h2>Battle Management</h2>

      {successMessage && (
        <div style={{ color: 'green', marginBottom: '10px' }}>
          {successMessage}
        </div>
      )}

      {error && (
        <div style={{ color: 'red', marginBottom: '10px' }}>
          {error}
        </div>
      )}

      {isLoading ? (
        <p>Loading battles...</p>
      ) : battles.length === 0 ? (
        <p>No battles available.</p>
      ) : (
        <div>
          <h3>Current Battles</h3>
          <div style={{ marginBottom: '20px' }}>
            {battles.map((battle) => (
              <div key={battle.id} style={{ border: '1px solid #ccc', padding: '10px', margin: '10px 0' }}>
                <h4>{battle.name}</h4>
                <p><strong>Status:</strong> {battle.state}</p>
                <p><strong>Arena:</strong> {battle.arenaWidth} x {battle.arenaHeight}</p>
                <p><strong>Robot Movement Time:</strong> {battle.robotMovementTimeSeconds}s</p>
                <p><strong>Robots ({battle.robotCount}):</strong></p>
                {battle.robots.length > 0 ? (
                  <ul>
                    {battle.robots.map((robot) => (
                      <li key={robot.id}>
                        {robot.name} - Status: {robot.status}
                      </li>
                    ))}
                  </ul>
                ) : (
                  <p>No robots registered</p>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      <div>
        {!showCreateForm ? (
          <button onClick={() => setShowCreateForm(true)}>Create Battle</button>
        ) : (
          <div style={{ border: '1px solid #ccc', padding: '20px', marginTop: '20px' }}>
            <h3>Create a New Battle</h3>
            
            <div style={{ marginBottom: '10px' }}>
              <label>Battle Name (required):</label>
              <input
                type="text"
                placeholder="Enter battle name"
                value={newBattleName}
                onChange={(e) => setNewBattleName(e.target.value)}
                style={{ width: '100%', padding: '5px' }}
              />
            </div>
            
            <div style={{ marginBottom: '10px' }}>
              <label>Arena Width (optional):</label>
              <input
                type="number"
                placeholder="Default arena width"
                value={arenaWidth}
                onChange={(e) => setArenaWidth(e.target.value)}
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
                onChange={(e) => setArenaHeight(e.target.value)}
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
                onChange={(e) => setRobotMovementTime(e.target.value)}
                style={{ width: '100%', padding: '5px' }}
                min="0.1"
              />
            </div>
            
            <div>
              <button onClick={handleCreateBattle} style={{ marginRight: '10px' }}>
                Create
              </button>
              <button onClick={() => {
                setShowCreateForm(false);
                setNewBattleName('');
                setArenaWidth('');
                setArenaHeight('');
                setRobotMovementTime('');
                setError(null);
              }}>
                Cancel
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default BattleManagement;

