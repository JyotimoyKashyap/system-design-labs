---
tags:
  - algorithms
created: 2026-10-20
source: https://leetcode.com/problems/number-of-subsequences-that-satisfy-the-given-sum-condition/description/
---
Precomputing has always been a way to basically say that we can cache results early and then use them later. In programming usually `Math.pow()` or `pow()` generally takes a lot of time and in [Leetcode 1498](https://leetcode.com/problems/number-of-subsequences-that-satisfy-the-given-sum-condition/description/) , gives you TLE if you use `Math.pow()` in Java or `pow()` in C++. 

So how to solve this ? 

In this question, we can actually precompute the powers. Because we know the maximum and minimum range for the values of power here which is from `0 -> n-1` , we can simply create an array and store the power calculation

```Java
int n = nums.length;
// precompute pow
// max diff = nums.length - 1
int[] pow = new int[n];
pow[0] = 1;
for (int i=1; i<n; i++) {
	pow[i] = (pow[i-1] * 2) % MOD;
}
```

Note that here, we are using `MOD` because $2^{100000}$ far larger than normal integer hence `MOD` is important.  

This is a very common trick in competitive programming.

Happy Coding !! 