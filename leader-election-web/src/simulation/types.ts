export type NodeState = 'FOLLOWER' | 'CANDIDATE' | 'LEADER' | 'DEAD';

export interface BaseMessage {
  fromId: string;
  toId: string;
}

export interface RequestVoteArgs extends BaseMessage {
  type: 'RequestVote';
  term: number;
  candidateId: string;
}

export interface RequestVoteReply extends BaseMessage {
  type: 'RequestVoteReply';
  term: number;
  voteGranted: boolean;
}

export interface AppendEntriesArgs extends BaseMessage {
  type: 'AppendEntries';
  term: number;
  leaderId: string;
}

export interface AppendEntriesReply extends BaseMessage {
  type: 'AppendEntriesReply';
  term: number;
  success: boolean;
}

export type RPCMessage = 
  | RequestVoteArgs 
  | RequestVoteReply 
  | AppendEntriesArgs 
  | AppendEntriesReply;
