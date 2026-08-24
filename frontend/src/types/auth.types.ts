export interface LoginRequest {
  username: string;
  password: string;
}

export interface UserProfile {
  id: string | number;
  username: string;
  role: string;
}

export interface LoginResponse {
  message: string;
  user: UserProfile;
}

export interface AuthError {
  message: string;
  status?: number;
  correlationId?: string;
}