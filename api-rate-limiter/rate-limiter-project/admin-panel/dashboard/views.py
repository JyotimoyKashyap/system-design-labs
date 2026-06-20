import os
import redis
from django.shortcuts import render

def dashboard(request):
    # Fallback to localhost if not running in Docker
    shards_env = os.getenv("RATE_LIMITER_REDIS_SHARDS", "localhost:6379,localhost:6380,localhost:6381")
    shard_list = shards_env.split(",")
    
    context_shards = []
    
    for shard in shard_list:
        host, port = shard.split(":")
        try:
            r = redis.Redis(host=host, port=int(port), decode_responses=True, socket_timeout=1)
            # Ping to check health
            r.ping()
            
            # Get all active IPs hitting this shard
            keys = r.keys("*")
            active_users = []
            total_requests = 0
            
            for key in keys:
                # The Sliding Window algorithm uses ZSETs
                if r.type(key) == 'zset':
                    count = r.zcard(key)
                    active_users.append({'ip': key, 'requests': count})
                    total_requests += count
            
            context_shards.append({
                'name': shard,
                'status': 'Healthy',
                'active_users': active_users,
                'total_requests': total_requests
            })
        except Exception as e:
            context_shards.append({
                'name': shard,
                'status': 'Down',
                'error': str(e),
                'active_users': [],
                'total_requests': 0
            })
            
    return render(request, 'dashboard/index.html', {'shards': context_shards})
