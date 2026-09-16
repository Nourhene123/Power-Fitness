import { User } from '../models/user.model';

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

/** Backend `/api/auth/me` payload — a User plus a flag the redirect logic needs. */
export interface CurrentUser extends User {
  hasAssessment: boolean;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  user: CurrentUser;
}
