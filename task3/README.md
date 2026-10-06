# Task 3 | Technical Report

For this task I wrote matrix multiplication in Java and in Python with numpy, wrote unit tests for the Java version, and then compared code size and execution time.

## How to run

```bash
cd task3/code
javac *.java
java MatrixMultiplicationTest # unit tests
java MatrixMultiplication # java version
python3 matrix_multiplication.py # numpy version (needs pip install numpy)
```

Both programs ask for everything as input: the sizes of A (m x n) and B (n x p), a seed for the random values, how many times to run for timing, and whether to print the result.

## Java version

I started with the definition from linear algebra: `c[i][j]` is the sum of `a[i][k] * b[k][j]` for all k. So it's three loops:

```java
for (int i = 0; i < m; i++) {
    for (int j = 0; j < p; j++) {
        double sum = 0;
        for (int k = 0; k < n; k++) {
            sum += a[i][k] * b[k][j];
        }
        c[i][j] = sum;
    }
}
```

Before the loops I check that the number of columns of A equals the number of rows of B. If not, I throw an `IllegalArgumentException`, since the multiplication is not defined in that case.

While doing this I remembered from the lecture that most languages store 2D arrays in row major order. In Java a `double[][]` is actually an array of row arrays. In the inner loop above, `b[k][j]` jumps to a different row on every step of k, which is bad for the cache. So I wrote a second version, `multiplyRowOrder`, where I just swapped the j and k loops:

```java
for (int i = 0; i < m; i++) {
    for (int k = 0; k < n; k++) {
        double aik = a[i][k];
        for (int j = 0; j < p; j++) {
            c[i][j] += aik * b[k][j];
        }
    }
}
```

It does the same additions, only in a different order. But now the inner loop goes along one row of `b` and one row of `c`, which are next to each other in memory. The program runs both versions and prints the time of each.

## numpy version

In numpy the multiplication itself is only one line:

```python
c = a @ b
```

The rest of `matrix_multiplication.py` is just reading the input and timing. numpy also checks the sizes by itself and raises an error if they do not match.

## Unit tests

For the tests I did not use JUnit, because I wanted it to run with plain `javac` and `java` without downloading anything. I wrote a small `check` method that prints PASS or FAIL and counts them. The tests are:

- 2x3 times 3x2 with a result I calculated by hand (for both versions)
- wrong sizes (2x3 times 2x3) throw the exception
- both versions give the same result for 20 random sizes (so non-square ones are tested too)

Because doubles can have tiny rounding differences, I compare with a tolerance of 1e-9 instead of `==`. Output:

```
PASS 2x3 * 3x2 known result
PASS 2x3 * 3x2 known result (row order)
PASS 2x3 * 2x3 throws an exception
PASS classic and row order give the same result on 20 random sizes
4 passed, 0 failed
```

## Code size

```
MatrixMultiplication.java      113 lines
MatrixMultiplicationTest.java   74 lines
matrix_multiplication.py        19 lines
```

The multiplication part alone is about 20 lines in Java (sizes check, creating the result, three loops) against 1 line in numpy. Most of the Java file is not the math: it reads input, fills random matrices, prints, and has two versions of the multiplication. The Python file is shorter mostly because numpy already has the multiplication, the size check and the printing.

## Execution time

I ran both programs with square matrices (n x n), random values, 5 runs each. On my MacBook M3 Pro, Java 25, Python 3.14.8.

One thing I noticed is that in Java the first run was always slower. For n = 128 the first run took 6.98 ms and the next ones about 1.7 ms. I think this is because of the JIT compiler. JVM first interprets the code and only compiles it to machine code after it runs for a while. So for the table I took the fastest of the 5 runs:

```
n       Java classic     Java row order     numpy
128         1.55 ms          0.54 ms        0.10 ms
256        14.36 ms          3.49 ms        0.12 ms
512       136.02 ms         28.14 ms        0.95 ms
1024     1415.47 ms        236.36 ms        7.44 ms
2048    30141.50 ms       4430.14 ms       64.63 ms
```

What I see from this:

1. When n doubles, the time grows about 8 times (for example row order: 28 -> 236 -> 4430 ms). That fits the three nested loops, which make the work n^3.
2. Just swapping two loops made Java about 3 times faster at n = 128 and almost 7 times faster at n = 2048, even though the math is exactly the same. At n = 2048 it went from 30 seconds to 4.4 seconds. The gap gets bigger with bigger matrices, because then the matrix no longer fits in the cache, and jumping between rows costs more.
3. numpy was still much faster. Around 30 times faster than my best Java version at n = 512 and n = 1024, and almost 70 times faster at n = 2048. This surprised me at first, because Python is slower than Java. But `a @ b` doesn't run as a Python loop. numpy calls a BLAS library written in low level code (on my machine `np.show_config()` shows it uses Apple's Accelerate). From what I know, these libraries split the matrices into blocks that fit in the cache, use vector instructions and multiple cores. My Java code is a simple single threaded loop.

So in the end numpy wins in both code size and speed. Java gives more control, but to get close to numpy I would need a lot more code. This matches the trade-off from the lecture between writability and cost of execution.

## ChatGPT

Link to the conversation: https://chatgpt.com/share/6ac51dd6-1f54-83ed-a140-0fff6e89db06

First I pasted the task text as it is. ChatGPT chose C++ and gave a numpy version, a C++ `Matrix` class, Catch2 unit tests, a benchmark and a short analysis. So my second prompt was "use java pls", and it rewrote everything in Java: a `Matrix` class with `double[][]`, a `multiply` method with a size check, and four JUnit 5 tests (2x2, 2x3 times 3x2, identity, wrong sizes). It used the i-k-j loop order right away, which is the same idea as my `multiplyRowOrder`.

But it did not fully follow the task. All the matrices and sizes were hard-coded, and the tests needed JUnit, which has to be downloaded first. So my third prompt was: "sizes and values should come from user input. not hard coded, and the tests should run without JUnit".

This time the program asked for the sizes and values, and the tests used Java's built-in `assert` with a tolerance of 1e-9. The problem is that Java assertions are off by default, so without `java -ea` the tests print PASSED even if the result is wrong.

For the analysis it only gave the theory (O(n^3)), estimated line counts and advice on how to measure (several sizes, several runs, JIT warm-up), but no real numbers, because it cannot run the code on my machine.
