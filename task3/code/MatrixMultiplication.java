import java.util.Random;
import java.util.Scanner;

public class MatrixMultiplication {

    // classic version, the definition c[i][j] = sum of a[i][k] * b[k][j]
    public static double[][] multiply(double[][] a, double[][] b) {
        if (a[0].length != b.length) {
            throw new IllegalArgumentException("columns of A must be equal to rows of B");
        }
        int m = a.length;
        int n = b.length;
        int p = b[0].length;
        double[][] c = new double[m][p];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < p; j++) {
                double sum = 0;
                for (int k = 0; k < n; k++) {
                    sum += a[i][k] * b[k][j];
                }
                c[i][j] = sum;
            }
        }
        return c;
    }

    // same result, but the j and k loops are swapped so b and c are read row by row
    public static double[][] multiplyRowOrder(double[][] a, double[][] b) {
        if (a[0].length != b.length) {
            throw new IllegalArgumentException("columns of A must be equal to rows of B");
        }
        int m = a.length;
        int n = b.length;
        int p = b[0].length;
        double[][] c = new double[m][p];

        for (int i = 0; i < m; i++) {
            for (int k = 0; k < n; k++) {
                double aik = a[i][k];
                for (int j = 0; j < p; j++) {
                    c[i][j] += aik * b[k][j];
                }
            }
        }
        return c;
    }

    static double[][] randomMatrix(Random random, int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = random.nextDouble();
            }
        }
        return matrix;
    }

    static void printMatrix(double[][] matrix) {
        for (double[] row : matrix) {
            for (double value : row) {
                System.out.printf("%10.3f", value);
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Rows of A: ");
        int m = sc.nextInt();
        System.out.print("Columns of A: ");
        int n = sc.nextInt();
        System.out.print("Columns of B: ");
        int p = sc.nextInt();
        if (m <= 0 || n <= 0 || p <= 0) {
            System.out.println("Sizes must be positive.");
            sc.close();
            return;
        }

        System.out.print("Seed: ");
        Random random = new Random(sc.nextLong());
        double[][] a = randomMatrix(random, m, n);
        double[][] b = randomMatrix(random, n, p);

        System.out.print("How many times to run (for timing): ");
        int runs = sc.nextInt();

        double[][] c = null;
        for (int r = 1; r <= runs; r++) {
            long start = System.nanoTime();
            c = multiply(a, b);
            long classic = System.nanoTime() - start;

            start = System.nanoTime();
            multiplyRowOrder(a, b);
            long rowOrder = System.nanoTime() - start;

            System.out.printf("Run %d: classic %.3f ms, row order %.3f ms%n",
                    r, classic / 1_000_000.0, rowOrder / 1_000_000.0);
        }

        System.out.print("Print the result? (y/n): ");
        if (c != null && sc.next().equals("y")) {
            System.out.println("Result C = A * B:");
            printMatrix(c);
        }

        sc.close();
    }
}
