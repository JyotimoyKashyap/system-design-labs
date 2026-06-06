import { useEffect, useState } from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import { NodeComponent } from './NodeComponent';
import type { RPCMessage } from '../simulation/types';

interface MessageAnim {
  id: string;
  msg: RPCMessage;
  latency: number;
}

export function ClusterView({ cluster, nodeStates, nodeTerms, nodeTimers, onKill, onRevive }: any) {
  const [messages, setMessages] = useState<MessageAnim[]>([]);

  // Positions for 5 nodes in a circle
  const radius = 230;
  const positions = cluster.nodes.map((_: any, i: number) => {
    const angle = (i * 2 * Math.PI) / cluster.nodes.length - Math.PI / 2;
    return {
      x: 450 + radius * Math.cos(angle),
      y: 325 + radius * Math.sin(angle)
    };
  });

  useEffect(() => {
    cluster.network.onMessageSent = (msg: RPCMessage, latency: number) => {
      const newMsg = { id: Math.random().toString(), msg, latency };
      setMessages(prev => [...prev, newMsg]);
      
      // Remove message from DOM after it finishes animating
      setTimeout(() => {
        setMessages(prev => prev.filter(m => m.id !== newMsg.id));
      }, latency + 100);
    };
  }, [cluster]);

  const getPosition = (id: string) => {
    const index = cluster.nodes.findIndex((n: any) => n.id === id);
    return positions[index] || { x: 0, y: 0 };
  };

  return (
    <div className="relative w-[900px] h-[650px] mx-auto">
      {/* Draw Nodes */}
      {cluster.nodes.map((node: any, i: number) => (
        <NodeComponent
          key={node.id}
          id={node.id}
          state={nodeStates[node.id] || 'FOLLOWER'}
          term={nodeTerms[node.id] || 0}
          timer={nodeTimers[node.id]}
          x={positions[i].x}
          y={positions[i].y}
          onKill={onKill}
          onRevive={onRevive}
        />
      ))}

      {/* Animate Messages */}
      <AnimatePresence>
        {messages.map(({ id, msg, latency }) => {
          const start = getPosition(msg.fromId);
          const end = getPosition(msg.toId);
          
          let color = 'bg-stone-100'; // AppendEntries (Heartbeat)
          if (msg.type === 'RequestVote') color = 'bg-orange-500';
          if (msg.type === 'RequestVoteReply') color = (msg as any).voteGranted ? 'bg-emerald-400' : 'bg-rose-500';

          return (
            <motion.div
              key={id}
              className={`absolute w-4 h-4 rounded-full border-2 border-stone-900 shadow-[2px_2px_0px_0px_rgba(28,25,23,1)] z-10 ${color}`}
              initial={{ x: start.x, y: start.y, opacity: 1, scale: 0.5 }}
              animate={{ x: end.x, y: end.y, opacity: 0, scale: 1.5 }}
              transition={{ duration: latency / 1000, ease: "linear" }}
              style={{ transform: 'translate(-50%, -50%)' }}
            />
          );
        })}
      </AnimatePresence>
    </div>
  );
}
