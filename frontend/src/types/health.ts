export type ComponentStatus = 'UP' | 'DOWN';

export interface HealthResponse {
  status: ComponentStatus;
  database: ComponentStatus;
  service: string;
  timestamp: string;
}
