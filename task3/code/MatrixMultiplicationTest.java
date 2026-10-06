import java.util.Random;

public class MatrixMultiplicationTest {

    static int passed = 0;
    static int failed = 0;

    static void check(boolean condition, String testName) {
        if (condition) {
            passed++;
            System.out.println("PASS " + testName);
        } else {
            failed++;
            System.out.println("FAIL " + testName);
        }
    }

    static boolean equal(double[][] x, double[][] y) {
        if (x.length != y.length || x[0].length != y[0].length) {
            return false;
        }
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[0].length; j++) {
                if (Math.abs(x[i][j] - y[i][j]) > 1e-9) {
                    return false;
                }
            }
        }
        return true;
    }

    static void testKnownResult() {
        double[][] a = {{1, 2, 3}, {4, 5, 6}};
        double[][] b = {{7, 8}, {9, 10}, {11, 12}};
        double[][] expected = {{58, 64}, {139, 154}};
        check(equal(MatrixMultiplication.multiply(a, b), expected), "2x3 * 3x2 known result");
        check(equal(MatrixMultiplication.multiplyRowOrder(a, b), expected), "2x3 * 3x2 known result (row order)");
    }

    static void testWrongSizes() {
        double[][] a = new double[2][3];
        double[][] b = new double[2][3];
        boolean thrown = false;
        try {
            MatrixMultiplication.multiply(a, b);
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        check(thrown, "2x3 * 2x3 throws an exception");
    }

    static void testBothVersionsAgree() {
        Random random = new Random(42);
        boolean allSame = true;
        for (int t = 0; t < 20; t++) {
            int m = 1 + random.nextInt(30);
            int n = 1 + random.nextInt(30);
            int p = 1 + random.nextInt(30);
            double[][] a = MatrixMultiplication.randomMatrix(random, m, n);
            double[][] b = MatrixMultiplication.randomMatrix(random, n, p);
            if (!equal(MatrixMultiplication.multiply(a, b), MatrixMultiplication.multiplyRowOrder(a, b))) {
                allSame = false;
            }
        }
        check(allSame, "classic and row order give the same result on 20 random sizes");
    }

    public static void main(String[] args) {
        testKnownResult();
        testWrongSizes();
        testBothVersionsAgree();
        System.out.println(passed + " passed, " + failed + " failed");
    }
}
