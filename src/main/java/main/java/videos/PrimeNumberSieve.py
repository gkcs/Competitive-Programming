def find_all_primes_upto(n):
	touched = [False]*(n+1)
	sqrtOfn = int(n**0.5)
	
	primes = []
	
	for i in range(2, sqrtOfn+1):
		if not touched[i]:
			for j in range(i*i, n+1, i):
				touched[j] = True

	for i in range(2, n+1):
		if not touched[i]:
			primes.append(i)

	return primes

# print(find_all_primes_upto(100))
