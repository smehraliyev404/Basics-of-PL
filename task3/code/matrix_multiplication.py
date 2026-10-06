import time
import numpy as np

m = int(input("Rows of A: "))
n = int(input("Columns of A: "))
p = int(input("Columns of B: "))

rng = np.random.default_rng(int(input("Seed: ")))
a = rng.random((m, n))
b = rng.random((n, p))

runs = int(input("How many times to run (for timing): "))
for r in range(1, runs + 1):
    start = time.perf_counter()
    c = a @ b
    print(f"Run {r}: {(time.perf_counter() - start) * 1000:.3f} ms")

if input("Print the result? (y/n): ") == "y":
    print("Result: ", c)
