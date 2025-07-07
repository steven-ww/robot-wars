import React, { useState, useEffect } from 'react';
import './GreetingComponent.css';

// Define the type for the greeting response
interface Greeting {
  message: string;
}

const GreetingComponent: React.FC = () => {
  const [textGreeting, setTextGreeting] = useState<string>('');
  const [jsonGreeting, setJsonGreeting] = useState<Greeting | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  // Function to fetch the text greeting
  const fetchTextGreeting = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch('/api/greeting');
      if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
      }
      const text = await response.text();
      setTextGreeting(text);
    } catch (err) {
      setError(
        `Failed to fetch text greeting: ${err instanceof Error ? err.message : String(err)}`
      );
      console.error('Error fetching text greeting:', err);
    } finally {
      setLoading(false);
    }
  };

  // Function to fetch the JSON greeting
  const fetchJsonGreeting = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch('/api/greeting/json');
      if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
      }
      const data = await response.json();
      setJsonGreeting(data);
    } catch (err) {
      setError(
        `Failed to fetch JSON greeting: ${err instanceof Error ? err.message : String(err)}`
      );
      console.error('Error fetching JSON greeting:', err);
    } finally {
      setLoading(false);
    }
  };

  // Fetch both greetings when the component mounts
  useEffect(() => {
    fetchTextGreeting();
    fetchJsonGreeting();
  }, []);

  return (
    <div className="greeting-container">
      <h2>REST API Demo</h2>
      <p className="description">
        This component demonstrates communication with the Quarkus backend using
        REST API calls.
      </p>

      <div className="greeting-section">
        <h3>Text Greeting</h3>
        {loading ? (
          <p>Loading...</p>
        ) : error ? (
          <p className="error">{error}</p>
        ) : (
          <div className="greeting-box">
            <p>{textGreeting}</p>
          </div>
        )}
        <button onClick={fetchTextGreeting} disabled={loading}>
          Refresh Text Greeting
        </button>
      </div>

      <div className="greeting-section">
        <h3>JSON Greeting</h3>
        {loading ? (
          <p>Loading...</p>
        ) : error ? (
          <p className="error">{error}</p>
        ) : (
          <div className="greeting-box">
            <p>{jsonGreeting?.message}</p>
            <div className="json-display">
              <pre>{JSON.stringify(jsonGreeting, null, 2)}</pre>
            </div>
          </div>
        )}
        <button onClick={fetchJsonGreeting} disabled={loading}>
          Refresh JSON Greeting
        </button>
      </div>
    </div>
  );
};

export default GreetingComponent;
