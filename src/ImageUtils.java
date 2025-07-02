import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Utility class for image output operations and file extension handling.
 * Contains methods to save processed images and extract file extensions.
 */
public class ImageUtils {

    /**
     * Saves the processed image to a fixed temporary location,
     * updates static fields in the UI class,
     * and updates the status label with timing information.
     *
     * @param visualInstance Reference to the GUI instance to update UI components
     * @param fileLocation   Original input file path (used to determine output extension)
     * @param outputImg      The processed BufferedImage to save
     * @param timeToPrepare  Time in milliseconds spent preparing the image before processing
     * @param timeElapsed    Time in milliseconds spent processing the image
     * @throws IOException   If saving the image fails
     */
    public static void formalFinish(ImageFilterUI visualInstance, String fileLocation, BufferedImage outputImg, long timeToPrepare, long timeElapsed) throws IOException {
        // Fixed output path in Temp directory
        String fileOutputPath = "Temp/temp";

        // Determine output extension from original filename, default to JPG
        String extOutput = getExtension(fileLocation);
        if (extOutput.isEmpty()) {
            extOutput = "jpg";
        }

        // Save the processed image to disk with the chosen extension
        ImageIO.write(outputImg, extOutput, new File(fileOutputPath + "." + extOutput.toLowerCase()));

        // Update static variables in the UI class to keep track of the processed image and format
        ImageFilterUI.imageFormat = extOutput.toUpperCase();
        ImageFilterUI.processedImage = outputImg;

        // Prepare mode description based on current processing mode
        String modeStr;
        switch (ImageFilterUI.currentMode) {
            case 1 -> modeStr = "SEQUENTIAL mode:";
            case 2 -> modeStr = "PARALLEL mode:";
            default -> modeStr = "UNKNOWN mode:";
        }

        // Update the GUI status label with a formatted string including timings and mode
        visualInstance.statusLabel.setText(String.format(
            "<html>%s<br>Prepar.: %d ms<br>Image proc.: %d ms<br>Total: %d ms<br></html>",
            modeStr, timeToPrepare, timeElapsed, timeToPrepare + timeElapsed));
    }

    /**
     * Extracts the file extension from a file path or filename string.
     * Recognizes common image extensions and returns a standardized uppercase form.
     * Defaults to "PNG" if the extension is unrecognized.
     *
     * @param fileLocation File path or name string to analyze
     * @return Uppercase extension string without the leading dot (e.g., "JPG", "PNG")
     */
    public static String getExtension(String fileLocation) {
        String lower = fileLocation.toLowerCase();

        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "JPG";
        } else if (lower.endsWith(".png")) {
            return "PNG";
        }

        // Default extension if none recognized
        return "PNG";
    }
}
