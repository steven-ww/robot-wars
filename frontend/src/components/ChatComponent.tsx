import React, { useState, useEffect, useRef } from 'react';
import './ChatComponent.css';

const ChatComponent: React.FC = () => {
  const [messages, setMessages] = useState<string[]>([]);
  const [inputMessage, setInputMessage] = useState<string>('');
  const [username, setUsername] = useState<string>('');
  const [isConnected, setIsConnected] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  
  // Reference to the WebSocket connection
  const webSocketRef = useRef<WebSocket | null>(null);
  
  // Function to connect to the WebSocket
  const connectWebSocket = () => {
    if (!username.trim()) {
      setError('Please enter a username');
      return;
    }
    
    setError(null);
    
    try {
      // Close existing connection if any
      if (webSocketRef.current) {
        webSocketRef.current.close();
      }
      
      // Create a new WebSocket connection
      const ws = new WebSocket(`ws://${window.location.host}/chat/${username}`);
      
      // Set up event handlers
      ws.onopen = () => {
        setIsConnected(true);
        setMessages(prev => [...prev, 'Connected to chat server']);
      };
      
      ws.onmessage = (event) => {
        setMessages(prev => [...prev, event.data]);
      };
      
      ws.onerror = (event) => {
        console.error('WebSocket error:', event);
        setError('WebSocket connection error');
        setIsConnected(false);
      };
      
      ws.onclose = () => {
        setIsConnected(false);
        setMessages(prev => [...prev, 'Disconnected from chat server']);
      };
      
      // Store the WebSocket reference
      webSocketRef.current = ws;
    } catch (err) {
      setError(`Failed to connect: ${err instanceof Error ? err.message : String(err)}`);
      console.error('Error connecting to WebSocket:', err);
    }
  };
  
  // Function to disconnect from the WebSocket
  const disconnectWebSocket = () => {
    if (webSocketRef.current) {
      webSocketRef.current.close();
      webSocketRef.current = null;
    }
  };
  
  // Function to send a message
  const sendMessage = () => {
    if (!inputMessage.trim() || !isConnected || !webSocketRef.current) {
      return;
    }
    
    webSocketRef.current.send(inputMessage);
    setInputMessage('');
  };
  
  // Clean up WebSocket connection when component unmounts
  useEffect(() => {
    return () => {
      disconnectWebSocket();
    };
  }, []);
  
  // Handle Enter key press in the message input
  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      sendMessage();
    }
  };
  
  return (
    <div className="chat-container">
      <h2>WebSocket Chat Demo</h2>
      <p className="description">
        This component demonstrates real-time communication with the Quarkus backend using WebSockets.
      </p>
      
      {!isConnected ? (
        <div className="connection-form">
          <h3>Connect to Chat</h3>
          {error && <p className="error">{error}</p>}
          <div className="input-group">
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Enter your username"
              className="username-input"
            />
            <button onClick={connectWebSocket}>Connect</button>
          </div>
        </div>
      ) : (
        <div className="chat-interface">
          <div className="chat-header">
            <h3>Chat as {username}</h3>
            <button onClick={disconnectWebSocket} className="disconnect-button">
              Disconnect
            </button>
          </div>
          
          <div className="messages-container">
            {messages.map((msg, index) => (
              <div key={index} className="message">
                {msg}
              </div>
            ))}
          </div>
          
          <div className="message-input-container">
            <input
              type="text"
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
              onKeyPress={handleKeyPress}
              placeholder="Type a message..."
              className="message-input"
            />
            <button onClick={sendMessage} disabled={!inputMessage.trim()}>
              Send
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default ChatComponent;