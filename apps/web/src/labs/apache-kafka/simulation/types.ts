export interface Message {
  id: string;
  partition: number;
  offset: number;
  payload: string;
}
