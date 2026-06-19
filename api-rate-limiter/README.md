# API Rate Limiter
This directory will contain two things : 

1. api-rate-limiter visualizer - which will be made via frontend tech
2. A Java SpringBoot project for designing rate limiter. 
3. There are 7 parts to it : 
    1. The Proxy server that connects to the clients 
    2. Redis Cache divided into shards
    3. Single Configuration file hosted in some sort of single centralized place like S3
    4. API server that has some API that the client actually needs to call
    5. Consistent Hashing algorithm for assigning an IP address to a particular shard such that for that IP address we can then rate limit
    6. The entire rate limit algorithm as well as the reading the single configuration file hosted in some osrt of single centralized place like S3 can be a part of a library 
    7. Admin panel for the checking how rate limiting is working and manipulate the shards of Redis

## Messy Thoughts
1. Now I would want to have the single configuration file, such that whenever we spin up a new redis shard that configuration should update automatically. 
2. If that has to automatically update that means we need to fire some event or some API call that would update the configuration with the new IP address of the shard and also make sure the consistent hashing algorithm is also updated during runtime of the proxy server
3. But I think that level of complexity would be a way too high, although worth exploring sometime later. 
4. To keep it simple, we will simply hardcode it and let the single configuration file will reside in the proxy server itself. 
5. The redis cache spins up then the configuration can be updated. 
6. In what way, we have to spin up the redis cache first and then the proxy server so that the IP addresses are assigned first and then we can create the configuration file in the proxy server

