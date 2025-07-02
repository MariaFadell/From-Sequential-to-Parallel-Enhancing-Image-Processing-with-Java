import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Parallel image processing using Java's ForkJoin framework.
 * Applies a convolution filter to an image by dividing the work
 * across multiple threads for better performance.
 */
public class Parallel {

    private static BufferedImage processedImage;
    private static final int THRESHOLD = 100;

    private static class ConvolutionTask extends RecursiveAction {
        private final int startCol, endCol;
        private final BufferedImage inputImg, outputImg;
        private final float[][] kernel;
        private final float multiplier;
        private final int kernelLen, width, height;

        ConvolutionTask(int startCol, int endCol,
                        BufferedImage inputImg, BufferedImage outputImg,
                        float[][] kernel, float multiplier,
                        int kernelLen, int width, int height) {
            this.startCol = startCol;
            this.endCol = endCol;
            this.inputImg = inputImg;
            this.outputImg = outputImg;
            this.kernel = kernel;
            this.multiplier = multiplier;
            this.kernelLen = kernelLen;
            this.width = width;
            this.height = height;
        }

        @Override
        protected void compute() {
            int range = endCol - startCol + 1;
            if (range <= THRESHOLD) {
                processChunk();
            } else {
                int mid = startCol + range / 2;
                invokeAll(
                    new ConvolutionTask(startCol, mid - 1, inputImg, outputImg, kernel, multiplier, kernelLen, width, height),
                    new ConvolutionTask(mid, endCol, inputImg, outputImg, kernel, multiplier, kernelLen, width, height)
                );
            }
        }

        private void processChunk() {
            for (int x = startCol; x <= endCol; x++) {
                for (int y = 0; y < height; y++) {
                    float redAcc = 0, greenAcc = 0, blueAcc = 0;

                    for (int i = 0; i < kernelLen; i++) {
                        for (int j = 0; j < kernelLen; j++) {
                            int xCoord = (x - kernelLen / 2 + i + width) % width;
                            int yCoord = (y - kernelLen / 2 + j + height) % height;

                            int rgb = inputImg.getRGB(xCoord, yCoord);

                            redAcc += ((rgb >> 16) & 0xFF) * kernel[i][j];
                            greenAcc += ((rgb >> 8) & 0xFF) * kernel[i][j];
                            blueAcc += (rgb & 0xFF) * kernel[i][j];
                        }
                    }

                    int r = Math.min(Math.max((int) (redAcc * multiplier), 0), 255);
                    int g = Math.min(Math.max((int) (greenAcc * multiplier), 0), 255);
                    int b = Math.min(Math.max((int) (blueAcc * multiplier), 0), 255);

                    outputImg.setRGB(x, y, new Color(r, g, b).getRGB());
                }
            }
        }
    }

    /**
     * Process using default commonPool (all available processors)
     */
    public static long process(BufferedImage inputImg, float[][] kernel, float multiplier) {
        return process(inputImg, kernel, multiplier, Runtime.getRuntime().availableProcessors());
    }

    /**
     * Process using specified number of threads.
     */
    public static long process(BufferedImage inputImg, float[][] kernel, float multiplier, int numThreads) {
        long start = System.currentTimeMillis();

        int width = inputImg.getWidth();
        int height = inputImg.getHeight();
        int kernelLen = kernel.length;

        BufferedImage outputImg = new BufferedImage(width, height, inputImg.getType());

        ForkJoinPool pool = new ForkJoinPool(numThreads);
        ConvolutionTask mainTask = new ConvolutionTask(0, width - 1, inputImg, outputImg, kernel, multiplier, kernelLen, width, height);
        pool.invoke(mainTask);
        pool.shutdown();

        processedImage = outputImg;
        return System.currentTimeMillis() - start;
    }

    public static BufferedImage getOutput() {
        return processedImage;
    }
}
