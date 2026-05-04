import { Injectable, NgZone, OnDestroy, inject } from '@angular/core';
import { Subject } from 'rxjs';

export interface CollabMessage {
  type: 'user_joined' | 'user_left' | 'active_users' | 'cursor_move' | 'spec_change' | 'node_selected';
  user_id?: string;
  user?: any;
  users?: any[];
  x?: number;
  y?: number;
  spec?: any;
  node_id?: string;
}

@Injectable({
  providedIn: 'root'
})
export class CollaborationService implements OnDestroy {
  private ws: WebSocket | null = null;
  public messages$ = new Subject<CollabMessage>();

  private zone = inject(NgZone);


  connect(workflowId: string, token?: string): void {
    this.disconnect();
    
    // Determine protocol based on current location
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    // For local dev, api is on 8000, or proxy. Let's assume proxy routes /api/v1/workflow to the backend.
    // In dev mode, window.location.host is localhost:4200, which proxies /api to localhost:8000.
    // WebSockets should also be proxied if configured, or we connect directly.
    const host = window.location.host;
    let wsUrl = `${protocol}//${host}/api/v1/workflow/${workflowId}/sync`;
    
    if (token) {
      wsUrl += `?token=${encodeURIComponent(token)}`;
    }

    this.ws = new WebSocket(wsUrl);

    this.ws.onmessage = (event) => {
      this.zone.run(() => {
        try {
          const message = JSON.parse(event.data);
          this.messages$.next(message);
        } catch (e) {
          console.warn('[CollabService] Invalid message:', event.data);
        }
      });
    };

    this.ws.onclose = () => {
      console.log('[CollabService] Disconnected');
      this.ws = null;
    };
    
    this.ws.onerror = (err) => {
      console.error('[CollabService] WebSocket error:', err);
    };
  }

  disconnect(): void {
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }

  sendMessage(message: CollabMessage): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message));
    }
  }

  ngOnDestroy(): void {
    this.disconnect();
  }
}
