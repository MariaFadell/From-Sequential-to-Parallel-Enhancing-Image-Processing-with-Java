import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Sequential image processing class that applies a convolution filter
 * to an image in a single-threaded manner.
 */
public class Sequential {

    // Stores the output image after processing
    private static BufferedImage outputImage;

    /**
     * Applies the given convolution kernel to the input image sequentially.
     * 
     * @param inputImg   The input BufferedImage to be processed.
     * @param kernel     The convolution kernel matrix (square).
     * @param multiplier The factor to multiply the kernel result by (filter intensity).
     * @return The time taken in milliseconds to complete the processing.
     */
    public static long process(BufferedImage inputImg, float[][] kernel, float multiplier) {
        // Record start time for performance measurement
        long start = System.currentTimeMillis();

        int width = inputImg.getWidth();
        int height = inputImg.getHeight();
        int kernelLen = kernel.length;  // Kernel size (e.g., 3 for 3x3)

        // Create output image with same dimensions and type as input
        BufferedImage outputImg = new BufferedImage(width, height, inputImg.getType());

        // Iterate over every pixel in the image
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Accumulators for the RGB channels after applying kernel
                float redAcc = 0, greenAcc = 0, blueAcc = 0;

                // Apply convolution kernel over neighborhood pixels
                for (int i = 0; i < kernelLen; i++) {
                    for (int j = 0; j < kernelLen; j++) {
                        // Calculate wrapped coordinates for edge pixels (toroidal wrap)
                        int xCoord = (x - kernelLen / 2 + i + width) % width;
                        int yCoord = (y - kernelLen / 2 + j + height) % height;

                        // Get RGB color of neighbor pixel
                        int rgb = inputImg.getRGB(xCoord, yCoord);

                        // Extract and accumulate red, green, blue components multiplied by kernel value
                        redAcc += ((rgb >> 16) & 0xFF) * kernel[i][j];
                        greenAcc += ((rgb >> 8) & 0xFF) * kernel[i][j];
                        blueAcc += (rgb & 0xFF) * kernel[i][j];
                    }
                }

                // Multiply accumulators by multiplier and clamp values to [0, 255]
                int r = Math.min(Math.max((int) (redAcc * multiplier), 0), 255);
                int g = Math.min(Math.max((int) (greenAcc * multiplier), 0), 255);
                int b = Math.min(Math.max((int) (blueAcc * multiplier), 0), 255);

                // Set the pixel in output image with new RGB value
                outputImg.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }

        // Store the processed image for later retrieval
        outputImage = outputImg;

        // Return elapsed time in milliseconds
        return System.currentTimeMillis() - start;
    }

    /**
     * Returns the last processed output image.
     * 
     * @return Processed BufferedImage after applying filter.
     */
    public static BufferedImage getOutput() {
        return outputImage;
    }
}
