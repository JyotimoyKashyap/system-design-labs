import { Network } from './Network';
import type { RPCMessage, NodeState, RequestVoteArgs, AppendEntriesArgs } from './types';

export class Node {
  public id: string;
  private state: NodeState = 'FOLLOWER';
  private currentTerm: number = 0;
  private votedFor: string | null = null;
  private network: Network;

  private electionTimer: number | null = null;
  private heartbeatTimer: number | null = null;

  // Configuration
  private minElectionTimeout = 3000;
  private maxElectionTimeout = 6000;
  private heartbeatInterval = 1000;

  // Election state
  private votesReceived = 0;
  private clusterSize: number;

  public onStateChange?: (id: string, state: NodeState, term: number) => void;
  public onTimerReset?: (id: string, duration: number) => void;

  constructor(id: string, network: Network, clusterSize: number) {
    this.id = id;
    this.network = network;
    this.clusterSize = clusterSize;
  }

  public getState() { return this.state; }
  public getTerm() { return this.currentTerm; }

  public start() {
    this.resetElectionTimer();
    this.notifyState();
  }

  public kill() {
    this.state = 'DEAD';
    this.clearTimers();
    this.notifyState();
  }

  public revive() {
    this.state = 'FOLLOWER';
    this.votedFor = null;
    this.resetElectionTimer();
    this.notifyState();
  }

  private clearTimers() {
    if (this.electionTimer) clearTimeout(this.electionTimer);
    if (this.heartbeatTimer) clearInterval(this.heartbeatTimer);
  }

  private resetElectionTimer() {
    if (this.state === 'DEAD' || this.state === 'LEADER') return;

    if (this.electionTimer) clearTimeout(this.electionTimer);
    const timeout = Math.floor(Math.random() * (this.maxElectionTimeout - this.minElectionTimeout)) + this.minElectionTimeout;
    
    if (this.onTimerReset) this.onTimerReset(this.id, timeout);

    this.electionTimer = window.setTimeout(() => this.becomeCandidate(), timeout);
  }

  private becomeCandidate() {
    if (this.state === 'DEAD') return;

    this.state = 'CANDIDATE';
    this.currentTerm++;
    this.votedFor = this.id;
    this.votesReceived = 1;
    this.notifyState();
    
    this.resetElectionTimer();

    this.network.broadcast(
      (toId) => ({
        type: 'RequestVote',
        fromId: this.id,
        toId,
        term: this.currentTerm,
        candidateId: this.id
      }),
      this.id
    );
  }

  private becomeLeader() {
    if (this.state === 'DEAD') return;

    this.state = 'LEADER';
    this.notifyState();
    this.clearTimers();

    this.sendHeartbeats();
    this.heartbeatTimer = window.setInterval(() => this.sendHeartbeats(), this.heartbeatInterval);
  }

  private stepDown(newTerm: number) {
    this.currentTerm = newTerm;
    this.state = 'FOLLOWER';
    this.votedFor = null;
    this.notifyState();
    this.resetElectionTimer();
  }

  private sendHeartbeats() {
    this.network.broadcast(
      (toId) => ({
        type: 'AppendEntries',
        fromId: this.id,
        toId,
        term: this.currentTerm,
        leaderId: this.id
      }),
      this.id
    );
  }

  public receiveMessage(msg: RPCMessage) {
    if (this.state === 'DEAD') return;

    if (msg.term > this.currentTerm) {
      this.stepDown(msg.term);
    }

    switch (msg.type) {
      case 'RequestVote':
        this.handleRequestVote(msg);
        break;
      case 'RequestVoteReply':
        if (msg.type === 'RequestVoteReply') {
          if (this.state !== 'CANDIDATE' || msg.term !== this.currentTerm) return;
          if (msg.voteGranted) {
            this.votesReceived++;
            if (this.votesReceived > this.clusterSize / 2) {
              this.becomeLeader();
            }
          }
        }
        break;
      case 'AppendEntries':
        this.handleAppendEntries(msg);
        break;
    }
  }

  private handleRequestVote(msg: RequestVoteArgs) {
    let voteGranted = false;

    if (msg.term < this.currentTerm) {
      voteGranted = false;
    } else if (this.votedFor === null || this.votedFor === msg.candidateId) {
      voteGranted = true;
      this.votedFor = msg.candidateId;
      this.resetElectionTimer();
    }

    this.network.send({
      type: 'RequestVoteReply',
      fromId: this.id,
      toId: msg.fromId,
      term: this.currentTerm,
      voteGranted
    });
  }

  private handleAppendEntries(msg: AppendEntriesArgs) {
    if (msg.term < this.currentTerm) {
      this.network.send({
        type: 'AppendEntriesReply',
        fromId: this.id,
        toId: msg.fromId,
        term: this.currentTerm,
        success: false
      });
      return;
    }

    if (this.state === 'CANDIDATE') {
      this.stepDown(msg.term);
    }
    this.resetElectionTimer();
    
    this.network.send({
      type: 'AppendEntriesReply',
      fromId: this.id,
      toId: msg.fromId,
      term: this.currentTerm,
      success: true
    });
  }

  private notifyState() {
    if (this.onStateChange) {
      this.onStateChange(this.id, this.state, this.currentTerm);
    }
  }
}
