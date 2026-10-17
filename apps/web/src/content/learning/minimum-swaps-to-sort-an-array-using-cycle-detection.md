---
title: "Minimum Swaps to Sort an Array using Cycle Detection"
description: "How to calculate the minimum number of swaps required to sort an array."
publishedAt: "2026-10-17"
category: "Algorithm"
tags: ["algorithms"]
readTimeMinutes: 2
featured: false
---

# Problem Statement
How to calculate the minimum number of swaps required to sort an array. 

# Intuition
The core intuition is that if we have an array `nums` and then we sort it then the elements' indexes will be changed. 

From GeeksforGeeks
>  This approach uses cycle detection method to find out the minimum number of swaps required to sort the array.
>  
>  If an element is not in its correct position, it indicates that it is a part of a cycle with one or more other elements that also need to be moved.
>  
>  For example, if element A is in the position of element B, and element B is in the position of element C, and so on, until it comes back to A, it forms a cycle. And to sort the elements in the cycle, we need cycleSize - 1 swaps, as each swap places one element in its correct position, and the last element will automatically be in its correct place.

![Pasted image 20261017092926.png](/assets/knowledge/Pasted image 20261017092926.png)

Here, 2 swaps 4, 4 swaps 1 and 1 swaps 2. So there are 3 cycles but then if 2 swaps 4 and 4 swaps 1 then 3rd swap is bound to happen, because otherwise 2's place will be empty.
Hence using this intuition we can derive 

`No. of swaps = Cycle Size - 1`

# How did I encounter this 
I found this concept to be used here 
https://leetcode.com/problems/minimum-swaps-to-sort-by-digit-sum/description/

And I learnt the concept from here 
https://www.geeksforgeeks.org/dsa/minimum-number-swaps-required-sort-array/