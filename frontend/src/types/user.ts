export interface CurrentUser {
  id: number;
  name: string;
  email: string;
  active: boolean;
}

export interface UpdateCurrentUserRequest {
  name: string;
}
