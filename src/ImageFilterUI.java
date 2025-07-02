import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;

public class ImageFilterUI extends JFrame {

    public static final int MODE_SEQUENTIAL = 1;
    public static final int MODE_PARALLEL = 2;

    public static int currentMode = MODE_SEQUENTIAL;

    public static BufferedImage processedImage;
    private String currentImagePath;
    public static String imageFormat = "jpg";

    private JLabel originalImageLabel = new JLabel();
    private JLabel processedImageLabel = new JLabel();
    private JButton applyFilterButton = new JButton("Apply Filter");
    private JButton saveImageButton = new JButton("Save Image");
    private JButton chooseSampleImageButton = new JButton("Choose Sample Image");
    public JLabel statusLabel = new JLabel(" ");

    private JList<String> filterList;
    private JSlider intensitySlider;
    private JProgressBar progressBar;

    // NEW: Thread count selector
    private JComboBox<Integer> threadCountComboBox;

    private final String[] sampleImageNames = {
            "Clouds", "Sea", "Flag", "Bike", "Sunset",
            "Mountain View", "Walking Man", "Cathedral", "Walrus", "Friends"
    };

    private final String[] sampleImagePaths = {
            "Samples/s1-300x300.jpg", "Samples/s2-689x689.jpg", "Samples/s3-1036x1036.jpg",
            "Samples/s4-1792x1792.jpg", "Samples/s5-2192x2192.jpg", "Samples/s6-3218x4291.jpg",
            "Samples/s7-2500x3333.jpg", "Samples/s8-3750x2500.jpg", "Samples/s9-4800x3200.jpg",
            "Samples/s10-5000x2000.jpg"
    };

    public ImageFilterUI() {
        super("Image Filtering Application");
        setupUI();
        loadSampleImage(sampleImagePaths[0]);
    }

    private void setupUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 450);
        setLayout(new BorderLayout(10, 10));

        String[] filters = {"Edge Detection", "Sharpen", "Soft Blur", "Gaussian Blur", "Emboss"};
        filterList = new JList<>(filters);
        filterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        filterList.setSelectedIndex(0);

        JPanel filtersPanel = new JPanel(new BorderLayout());
        filtersPanel.setBorder(BorderFactory.createTitledBorder("Choose Filter"));
        filtersPanel.add(new JScrollPane(filterList), BorderLayout.CENTER);

        intensitySlider = new JSlider(1, 10, 5);
        intensitySlider.setMajorTickSpacing(1);
        intensitySlider.setPaintTicks(true);
        intensitySlider.setPaintLabels(true);
        JPanel intensityPanel = new JPanel(new BorderLayout());
        intensityPanel.setBorder(BorderFactory.createTitledBorder("Filter Intensity"));
        intensityPanel.add(intensitySlider, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        applyFilterButton.setEnabled(true);
        saveImageButton.setEnabled(false);
        buttonsPanel.add(chooseSampleImageButton);
        buttonsPanel.add(applyFilterButton);
        buttonsPanel.add(saveImageButton);

        // NEW: Setup thread count combo box
        Integer[] threadOptions = {1, 2, 4, 8, 12};
        threadCountComboBox = new JComboBox<>(threadOptions);
        threadCountComboBox.setSelectedItem(Runtime.getRuntime().availableProcessors());
        JPanel threadPanel = new JPanel(new BorderLayout());
        threadPanel.setBorder(BorderFactory.createTitledBorder("Number of Threads"));
        threadPanel.add(threadCountComboBox, BorderLayout.CENTER);

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.add(filtersPanel);
        leftPanel.add(intensityPanel);
        leftPanel.add(buttonsPanel);
        leftPanel.add(threadPanel);  // Add the thread count panel here
        leftPanel.setPreferredSize(new Dimension(220, 0));

        JPanel originalPanel = new JPanel(new BorderLayout());
        originalPanel.setBorder(BorderFactory.createTitledBorder("Original Image"));
        originalImageLabel.setHorizontalAlignment(JLabel.CENTER);
        originalImageLabel.setPreferredSize(new Dimension(350, 350));
        originalPanel.add(originalImageLabel, BorderLayout.CENTER);

        JPanel processedPanel = new JPanel(new BorderLayout());
        processedPanel.setBorder(BorderFactory.createTitledBorder("Processed Image"));
        processedImageLabel.setHorizontalAlignment(JLabel.CENTER);
        processedImageLabel.setPreferredSize(new Dimension(350, 350));
        processedPanel.add(processedImageLabel, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel();
        centerPanel.add(originalPanel);
        centerPanel.add(processedPanel);

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(statusLabel, BorderLayout.CENTER);
        bottomPanel.add(progressBar, BorderLayout.EAST);
        bottomPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JMenuBar menuBar = new JMenuBar();
        JMenu modeMenu = new JMenu("Processing Mode");
        JRadioButtonMenuItem seqMode = new JRadioButtonMenuItem("Sequential", true);
        JRadioButtonMenuItem parMode = new JRadioButtonMenuItem("Parallel");

        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(seqMode);
        modeGroup.add(parMode);
        modeMenu.add(seqMode);
        modeMenu.add(parMode);
        menuBar.add(modeMenu);
        setJMenuBar(menuBar);

        seqMode.addActionListener(e -> {
            currentMode = MODE_SEQUENTIAL;
            statusLabel.setText("Mode set to Sequential");
        });
        parMode.addActionListener(e -> {
            currentMode = MODE_PARALLEL;
            statusLabel.setText("Mode set to Parallel");
        });

        applyFilterButton.addActionListener(e -> applyFilter());
        saveImageButton.addActionListener(e -> saveProcessedImage());
        chooseSampleImageButton.addActionListener(e -> openSampleImageDialog());

        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
    }

    private void openSampleImageDialog() {
        String selected = (String) JOptionPane.showInputDialog(
                this,
                "Select an image:",
                "Choose Sample Image",
                JOptionPane.PLAIN_MESSAGE,
                null,
                sampleImageNames,
                sampleImageNames[0]
        );

        if (selected != null) {
            for (int i = 0; i < sampleImageNames.length; i++) {
                if (sampleImageNames[i].equals(selected)) {
                    loadSampleImage(sampleImagePaths[i]);
                    break;
                }
            }
        }
    }

    private void loadSampleImage(String imagePath) {
        try {
            BufferedImage img = ImageIO.read(new File(imagePath));
            if (img == null) throw new IOException("Unsupported image format");
            setImageToLabel(img, originalImageLabel);
            currentImagePath = imagePath;
            imageFormat = getFileExtension(imagePath);
            processedImageLabel.setIcon(null);
            statusLabel.setText("Loaded image: " + new File(imagePath).getName());
            saveImageButton.setEnabled(false);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not load image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyFilter() {
        if (currentImagePath == null) {
            JOptionPane.showMessageDialog(this, "No image loaded. Please load an image first.");
            return;
        }
        String selectedFilter = filterList.getSelectedValue();
        if (selectedFilter == null) {
            JOptionPane.showMessageDialog(this, "Please select a filter.");
            return;
        }

        float[][] kernel;
        float baseFactor;
        switch (selectedFilter) {
            case "Edge Detection" -> {
                kernel = new float[][]{{-1, -1, -1}, {-1, 8, -1}, {-1, -1, -1}};
                baseFactor = 1f;
            }
            case "Sharpen" -> {
                kernel = new float[][]{{0, -1, 0}, {-1, 5, -1}, {0, -1, 0}};
                baseFactor = 1f;
            }
            case "Soft Blur" -> {
                kernel = new float[][]{{1, 1, 1}, {1, 1, 1}, {1, 1, 1}};
                baseFactor = 1f / 9f;
            }
            case "Gaussian Blur" -> {
                kernel = new float[][]{{1, 2, 1}, {2, 4, 2}, {1, 2, 1}};
                baseFactor = 1f / 16f;
            }
            case "Emboss" -> {
                kernel = new float[][]{{-2, -1, 0}, {-1, 1, 1}, {0, 1, 2}};
                baseFactor = 1f;
            }
            default -> {
                JOptionPane.showMessageDialog(this, "Unknown filter selected.");
                return;
            }
        }

        float factor = baseFactor * intensitySlider.getValue();

        try {
            BufferedImage inputImg = ImageIO.read(new File(currentImagePath));
            if (inputImg == null) throw new IOException("Could not read image");

            progressBar.setVisible(true);
            progressBar.setIndeterminate(true);
            statusLabel.setText("Processing with " + selectedFilter + " (" + (currentMode == MODE_SEQUENTIAL ? "Sequential" : "Parallel") + ")...");

            long elapsed;
            if (currentMode == MODE_SEQUENTIAL) {
                elapsed = Sequential.process(inputImg, kernel, factor);
                processedImage = Sequential.getOutput();
            } else {
                int threads = (Integer) threadCountComboBox.getSelectedItem();
                elapsed = Parallel.process(inputImg, kernel, factor, threads);
                processedImage = Parallel.getOutput();
            }

            setImageToLabel(processedImage, processedImageLabel);

            statusLabel.setText(String.format("Filter applied in %d ms [%s mode%s]",
                    elapsed,
                    currentMode == MODE_SEQUENTIAL ? "Sequential" : "Parallel",
                    currentMode == MODE_PARALLEL ? ", Threads: " + threadCountComboBox.getSelectedItem() : ""));

            saveImageButton.setEnabled(true);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error during processing: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            progressBar.setIndeterminate(false);
            progressBar.setVisible(false);
        }
    }

    private void saveProcessedImage() {
        if (processedImage == null) {
            JOptionPane.showMessageDialog(this, "No processed image to save.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                ImageIO.write(processedImage, imageFormat, file);
                JOptionPane.showMessageDialog(this, "Image saved successfully!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Failed to save image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void setImageToLabel(BufferedImage image, JLabel label) {
        int width = label.getWidth() > 0 ? label.getWidth() : 350;
        int height = label.getHeight() > 0 ? label.getHeight() : 350;
        Image scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        label.setIcon(new ImageIcon(scaled));
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1).toLowerCase();
        }
        return "jpg";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ImageFilterUI app = new ImageFilterUI();
            app.setVisible(true);
        });
    }
}
