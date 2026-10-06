import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class ImageSlicing {

    // negative index counts from the end, out of range is clipped
    static int fixIndex(int index, int length, int step) {
        if (index < 0) {
            index += length;
            if (index < 0) {
                index = (step > 0) ? 0 : -1;
            }
        } else if (index >= length) {
            index = (step > 0) ? length : length - 1;
        }
        return index;
    }

    // turns "start:stop:step" into the list of indices it selects
    static int[] sliceIndices(String text, int length) {
        String[] parts = text.split(":", -1);
        if (parts.length < 2 || parts.length > 3) {
            throw new IllegalArgumentException("slice must look like start:stop or start:stop:step");
        }

        int step = 1;
        if (parts.length == 3 && !parts[2].isBlank()) {
            step = Integer.parseInt(parts[2].trim());
        }
        if (step == 0) {
            throw new IllegalArgumentException("step cannot be zero");
        }

        int start;
        if (parts[0].isBlank()) {
            start = (step > 0) ? 0 : length - 1;
        } else {
            start = fixIndex(Integer.parseInt(parts[0].trim()), length, step);
        }

        int stop;
        if (parts[1].isBlank()) {
            stop = (step > 0) ? length : -1;
        } else {
            stop = fixIndex(Integer.parseInt(parts[1].trim()), length, step);
        }

        ArrayList<Integer> indices = new ArrayList<>();
        for (int i = start; (step > 0) ? i < stop : i > stop; i += step) {
            indices.add(i);
        }

        int[] result = new int[indices.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = indices.get(i);
        }
        return result;
    }

    static int[][] slice(int[][] matrix, String rowSlice, String colSlice) {
        int[] rows = sliceIndices(rowSlice, matrix.length);
        int[] cols = sliceIndices(colSlice, matrix[0].length);
        int[][] result = new int[rows.length][cols.length];
        for (int i = 0; i < rows.length; i++) {
            for (int j = 0; j < cols.length; j++) {
                result[i][j] = matrix[rows[i]][cols[j]];
            }
        }
        return result;
    }

    static int[][] imageToMatrix(BufferedImage image) {
        int[][] pixels = new int[image.getHeight()][image.getWidth()];
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                pixels[y][x] = image.getRGB(x, y);
            }
        }
        return pixels;
    }

    static BufferedImage matrixToImage(int[][] pixels) {
        BufferedImage image = new BufferedImage(pixels[0].length, pixels.length, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < pixels.length; y++) {
            for (int x = 0; x < pixels[0].length; x++) {
                image.setRGB(x, y, pixels[y][x]);
            }
        }
        return image;
    }

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        System.out.print("Input image: ");
        String inputPath = sc.nextLine();
        System.out.print("Row slice (start:stop:step): ");
        String rowSlice = sc.nextLine();
        System.out.print("Column slice (start:stop:step): ");
        String colSlice = sc.nextLine();
        System.out.print("Output image (.png): ");
        String outputPath = sc.nextLine();

        BufferedImage image = ImageIO.read(new File(inputPath));
        if (image == null) {
            System.out.println("Could not read the image.");
            sc.close();
            return;
        }
        int[][] pixels = imageToMatrix(image);
        int[][] result = slice(pixels, rowSlice, colSlice);
        System.out.println("Original size: " + pixels.length + " x " + pixels[0].length);
        if (result.length == 0 || result[0].length == 0) {
            System.out.println("The slice is empty, nothing to save.");
            sc.close();
            return;
        }
        System.out.println("Sliced size: " + result.length + " x " + result[0].length);

        ImageIO.write(matrixToImage(result), "png", new File(outputPath));
        System.out.println("Saved " + outputPath);
        sc.close();
    }
}
