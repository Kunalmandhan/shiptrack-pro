/**
 * STOMP WebSocket Client Service for ShipTrack Pro.
 * Handles WebSocket connection lifecycle, STOMP v1.2 frame encoding/decoding,
 * and topic subscriptions (/topic/tracking/{shipmentId}).
 */

class StompWebSocketClient {
  constructor() {
    this.socket = null;
    this.isConnected = false;
    this.subscriptions = new Map(); // subId -> callback
    this.listeners = new Set(); // connection status listeners
    this.subCounter = 0;
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectTimer = null;
    this.wsUrl = import.meta.env.VITE_WS_URL || 'ws://localhost:8084/ws';
  }

  connect() {
    if (this.socket && (this.socket.readyState === WebSocket.CONNECTING || this.socket.readyState === WebSocket.OPEN)) {
      return;
    }

    try {
      this.socket = new WebSocket(this.wsUrl);

      this.socket.onopen = () => {
        this.sendFrame('CONNECT', { 'accept-version': '1.2', host: 'localhost' });
      };

      this.socket.onmessage = (event) => {
        this.handleFrame(event.data);
      };

      this.socket.onerror = () => {
        this.handleDisconnect();
      };

      this.socket.onclose = () => {
        this.handleDisconnect();
      };
    } catch {
      this.handleDisconnect();
    }
  }

  disconnect() {
    if (this.socket) {
      if (this.isConnected) {
        this.sendFrame('DISCONNECT', {});
      }
      this.socket.close();
    }
    this.isConnected = false;
    this.subscriptions.clear();
    this.notifyListeners(false);
  }

  subscribe(destination, callback) {
    this.subCounter++;
    const subId = `sub-${this.subCounter}`;
    this.subscriptions.set(subId, { destination, callback });

    if (this.isConnected) {
      this.sendFrame('SUBSCRIBE', { id: subId, destination });
    } else {
      this.connect();
    }

    return () => this.unsubscribe(subId);
  }

  unsubscribe(subId) {
    if (this.subscriptions.has(subId)) {
      if (this.isConnected) {
        this.sendFrame('UNSUBSCRIBE', { id: subId });
      }
      this.subscriptions.delete(subId);
    }
  }

  subscribeShipment(shipmentId, callback) {
    return this.subscribe(`/topic/tracking/${shipmentId}`, callback);
  }

  subscribeDriver(driverId, callback) {
    return this.subscribe(`/topic/driver/${driverId}`, callback);
  }

  sendFrame(command, headers = {}, body = '') {
    if (!this.socket || this.socket.readyState !== WebSocket.OPEN) return;

    let frame = `${command}\n`;
    for (const [key, value] of Object.entries(headers)) {
      frame += `${key}:${value}\n`;
    }
    frame += `\n${body}\0`;

    this.socket.send(frame);
  }

  handleFrame(rawData) {
    if (!rawData) return;
    const commandEnd = rawData.indexOf('\n');
    if (commandEnd === -1) return;

    const command = rawData.substring(0, commandEnd).trim();
    const rest = rawData.substring(commandEnd + 1);

    const bodyStart = rest.indexOf('\n\n');
    const headerLines = (bodyStart !== -1 ? rest.substring(0, bodyStart) : rest).split('\n');

    const headers = {};
    for (const line of headerLines) {
      const kv = line.split(':');
      if (kv.length >= 2) {
        headers[kv[0].trim()] = kv.slice(1).join(':').trim();
      }
    }

    let body = '';
    if (bodyStart !== -1) {
      body = rest.substring(bodyStart + 2).replace(/\0$/, '');
    }

    if (command === 'CONNECTED') {
      this.isConnected = true;
      this.reconnectAttempts = 0;
      this.notifyListeners(true);

      // Resubscribe to active subscriptions after connect
      for (const [subId, sub] of this.subscriptions.entries()) {
        this.sendFrame('SUBSCRIBE', { id: subId, destination: sub.destination });
      }
    } else if (command === 'MESSAGE') {
      const subId = headers['subscription'];
      if (subId && this.subscriptions.has(subId)) {
        try {
          const payload = JSON.parse(body);
          this.subscriptions.get(subId).callback(payload);
        } catch {
          this.subscriptions.get(subId).callback(body);
        }
      }
    }
  }

  handleDisconnect() {
    this.isConnected = false;
    this.notifyListeners(false);

    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      const delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts), 10000);
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = setTimeout(() => this.connect(), delay);
    }
  }

  addStatusListener(listener) {
    this.listeners.add(listener);
    listener(this.isConnected);
    return () => this.listeners.delete(listener);
  }

  notifyListeners(status) {
    this.listeners.forEach((listener) => listener(status));
  }
}

export const websocketService = new StompWebSocketClient();
export default websocketService;
