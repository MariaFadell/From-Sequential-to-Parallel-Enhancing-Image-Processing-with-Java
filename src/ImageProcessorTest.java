import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Test class to verify that the Sequential and Parallel image processing
 * implementations produce nearly identical output images.
 */
public class ImageProcessorTest {

    // Allowed per-channel RGB difference between Sequential and Parallel output
    private static final int PIXEL_TOLERANCE = 1;

    /**
     * Tests that the output images from Sequential and Parallel processing
     * are identical within a small tolerance for every pixel.
     * 
     * @throws IOException if the test image cannot be loaded
     */
    @Test
    public void testSequentialVsParallelOutput() throws IOException {
        // Load the test image from file system
        BufferedImage input = ImageIO.read(new File("Temp/temp.jpg"));

        // Define a simple averaging kernel (soft blur)
        float[][] kernel = {
            {1f / 9f, 1f / 9f, 1f / 9f},
            {1f / 9f, 1f / 9f, 1f / 9f},
            {1f / 9f, 1f / 9f, 1f / 9f}
        };
        float multiplier = 1.0f;

        // Run sequential processing and get output image
        long elapsedSeq = Sequential.process(input, kernel, multiplier);
        BufferedImage seqOut = Sequential.getOutput();

        // Run parallel processing and get output image
        long elapsedPar = Parallel.process(input, kernel, multiplier);
        BufferedImage parOut = Parallel.getOutput();

        // Defensive: If parallel output is null, run again (should not happen normally)
        if (parOut == null) {
            Parallel.process(input, kernel, multiplier);
            parOut = Parallel.getOutput();
        }

        // Assert image dimensions match exactly
        assertEquals(seqOut.getWidth(), parOut.getWidth(), "Width mismatch");
        assertEquals(seqOut.getHeight(), parOut.getHeight(), "Height mismatch");

        // Compare each pixel's RGB values, allowing a small tolerance
        for (int x = 0; x < seqOut.getWidth(); x++) {
            for (int y = 0; y < seqOut.getHeight(); y++) {
                int seqRGB = seqOut.getRGB(x, y);
                int parRGB = parOut.getRGB(x, y);

                // Extract RGB components from sequential output pixel
                int seqR = (seqRGB >> 16) & 0xFF;
                int seqG = (seqRGB >> 8) & 0xFF;
                int seqB = seqRGB & 0xFF;

                // Extract RGB components from parallel output pixel
                int parR = (parRGB >> 16) & 0xFF;
                int parG = (parRGB >> 8) & 0xFF;
                int parB = parRGB & 0xFF;

                // Fail test if any color channel differs by more than tolerance
                if (Math.abs(seqR - parR) > PIXEL_TOLERANCE ||
                    Math.abs(seqG - parG) > PIXEL_TOLERANCE ||
                    Math.abs(seqB - parB) > PIXEL_TOLERANCE) {
                    fail("Pixel mismatch at (" + x + "," + y + "): Sequential RGB=(" + seqR + "," + seqG + "," + seqB +
                            ") vs Parallel RGB=(" + parR + "," + parG + "," + parB + ")");
                }
            }
        }
    }
}
