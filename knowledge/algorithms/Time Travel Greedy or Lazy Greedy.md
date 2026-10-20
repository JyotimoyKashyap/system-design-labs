---
created: 2026-10-18
source: https://leetcode.com/problems/furthest-building-you-can-reach/
tags:
  - algorithms
  - greedy
---
# Leetcode 1642 : Furthest Building You Can Reach

In a standard greedy problem, we make an optimal choice which is the local optimal that also turns out to be global optimal. But in some problems it is not possible to make an optimal choice and never look back at the past and retrospectively make an optimal choice or like undo some choice in the past. 

We can get around this by combining an optimistic assumption with a Data Structure that helps us see the past in O(1) usually. Like in this case a priority queue in some cases there might be some other data structures that could be used. 

## Core Intuition 
1. Use your most premium high-value resource first because it guarantees progress
2. Log every premium choice you made into a data structure
3. The moment you run out of your premium resource, we look at our saved log and find the time when you used your premium resource on the least value. 

In this problem, we use ladders first because it lets us progress and then when we run out of ladders, we look back and pick the smallest jump made and use bricks on that. Since bricks cost the difference between building height, we optimise it by using bricks for smallest jump only and using ladders for largest jumps. 



