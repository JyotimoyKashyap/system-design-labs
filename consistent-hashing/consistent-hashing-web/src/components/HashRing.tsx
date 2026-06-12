import { useEffect } from 'react';
import { motion, useMotionValue, useTransform, animate } from 'framer-motion';
import type { StorageNode } from '../core/ConsistentHashing';

export interface ActiveMessage {
  id: string;
  key: string;
  hash: number;
  targetNodeHash: number;
  createdAt: number;
}

interface HashRingProps {
  ringSize: number;
  nodes: StorageNode[];
  activeMessages: ActiveMessage[];
}

export function HashRing({ ringSize, nodes, activeMessages }: HashRingProps) {
  // SVG Configuration
  const radius = 300;
  const center = radius + 80; // Enough padding for rectangular nodes
  
  // Angle starts at top (12 o'clock => -90 degrees)
  const getAngle = (hash: number) => (hash / ringSize) * 360;
  
  const getCoordinates = (angleDegrees: number, offsetRadius = radius) => {
    const rad = (angleDegrees - 90) * (Math.PI / 180);
    return {
      x: center + offsetRadius * Math.cos(rad),
      y: center + offsetRadius * Math.sin(rad)
    };
  };

  return (
    <div className="relative w-full h-full flex items-center justify-center">
      <svg width={center * 2} height={center * 2} className="overflow-visible">
        {/* The stark brutalist ring */}
        <circle 
          cx={center} 
          cy={center} 
          r={radius} 
          className="fill-none stroke-stone-900 stroke-2" 
        />

        {/* Hash Slots (Ticks & Labels) */}
        {Array.from({ length: ringSize }).map((_, i) => {
          const angle = getAngle(i);
          // Outer point of tick (on the circle line)
          const p1 = getCoordinates(angle, radius);
          // Inner point of tick (slightly inward)
          const p2 = getCoordinates(angle, radius - 10);
          // Label position (further inward)
          const p3 = getCoordinates(angle, radius - 25);
          
          return (
            <g key={`slot-${i}`}>
              <line 
                x1={p1.x} 
                y1={p1.y} 
                x2={p2.x} 
                y2={p2.y} 
                className="stroke-stone-300 stroke-2"
              />
              <text
                x={p3.x}
                y={p3.y}
                textAnchor="middle"
                alignmentBaseline="middle"
                className="font-mono text-[10px] fill-stone-400"
              >
                {i}
              </text>
            </g>
          );
        })}

        {/* Messages */}
        {activeMessages.map(msg => (
          <DataMarker 
            key={msg.id}
            msg={msg}
            getAngle={getAngle}
            center={center}
            radius={radius}
          />
        ))}

        {/* Nodes */}
        {nodes.map((node) => {
          const angle = getAngle(node.hash);
          const { x, y } = getCoordinates(angle);
          
          const rectWidth = 100;
          const rectHeight = 44;

          return (
            <motion.g 
              key={node.ip}
              initial={{ scale: 0, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0, opacity: 0 }}
              transition={{ type: "spring", stiffness: 300, damping: 20 }}
            >
              {/* Rectangular Brutalist Node */}
              <rect 
                x={x - rectWidth / 2} 
                y={y - rectHeight / 2} 
                width={rectWidth} 
                height={rectHeight} 
                className="fill-white stroke-stone-900 stroke-2"
                style={{
                   filter: 'drop-shadow(4px 4px 0px rgba(28,25,23,1))'
                }}
              />
              <text 
                x={x} 
                y={y - 2} 
                textAnchor="middle" 
                alignmentBaseline="middle"
                className="font-mono text-sm font-bold fill-stone-900"
              >
                {node.name}
              </text>
              <text 
                x={x} 
                y={y + 12} 
                textAnchor="middle" 
                className="font-mono text-[10px] fill-stone-500"
              >
                h:{node.hash}
              </text>
            </motion.g>
          );
        })}
      </svg>
    </div>
  );
}


function DataMarker({ 
  msg, 
  getAngle,
  center,
  radius
}: { 
  msg: ActiveMessage;
  getAngle: (hash: number) => number;
  center: number;
  radius: number;
}) {
  const startAngle = getAngle(msg.hash);
  let endAngle = getAngle(msg.targetNodeHash);

  // If the target is numerically less than the start, it means it wrapped around the ring
  if (endAngle < startAngle) {
    endAngle += 360;
  }

  // Use a motion value to drive the exact x,y coordinates along the perimeter.
  // This bypasses ALL CSS transform and transform-origin browser bugs.
  const angle = useMotionValue(startAngle);
  
  const cx = useTransform(angle, a => center + radius * Math.cos((a - 90) * (Math.PI / 180)));
  const cy = useTransform(angle, a => center + radius * Math.sin((a - 90) * (Math.PI / 180)));

  useEffect(() => {
    const controls = animate(angle, endAngle, { 
      duration: 1.2, 
      ease: "easeInOut" 
    });
    return () => controls.stop();
  }, [angle, endAngle]);

  return (
    <motion.circle 
      cx={cx} 
      cy={cy} 
      r={10} 
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      transition={{ opacity: { duration: 0.3 } }}
      className="fill-orange-500 stroke-stone-900 stroke-2"
      style={{
        filter: 'drop-shadow(2px 2px 0px rgba(28,25,23,1))'
      }}
    />
  );
}
