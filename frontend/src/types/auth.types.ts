export interface LoginRequest {
  username: string;
  password: string;
  facilityId?: number;
}

export interface UserProfile {
  userId: number;
  username: string;
  role: string;
  facilityId: number;
  facilityName: string;
  isActive?: boolean;
}

export interface LoginResponse {
  userId: number;
  username: string;
  role: string;
  facilityId: number;
  facilityName: string;
  message: string;
}

export interface AuthError {
  message: string;
  status?: number;
  correlationId?: string;
}