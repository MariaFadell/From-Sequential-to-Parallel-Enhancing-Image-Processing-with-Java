
# Java Image Filtering Application

This is a desktop-based Java application for applying various **convolution-based image filters** (such as **Edge Detection**, **Sharpen**, **Blur**, etc.) to **sample images** using **Sequential** and **Parallel** (multi-threaded) processing. The GUI allows users to select filters, control intensity, and compare results side-by-side.

---

## 📦 Project Description

This application uses:
- **Swing** for GUI
- **BufferedImage** for image manipulation
- **ForkJoin Framework** for parallel processing
- **JUnit 5** for unit testing

---

## 🗂️ Project Structure and File Explanation

| File                      | Description                                                                                                                                                                                   |
| ------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `ImageFilterUI.java`      | **Main GUI class.** Entry point of the application. Allows users to choose sample images, select filters, adjust intensity, process them using sequential or parallel mode, and save results. |
| `Sequential.java`         | Implements **sequential image filtering** using 2D convolution on each pixel. Used for baseline comparisons and benchmarking.                                                                 |
| `Parallel.java`           | Implements **parallel image filtering** using Java's `ForkJoinPool`. It splits image columns and processes them concurrently for better performance on multi-core systems.                    |
| `ImageUtils.java`         | Contains **helper utility functions** for working with images. Can be used for shared image operations, though it's minimal in this project.                                                  |
| `ImageProcessorTest.java` | **JUnit 5 test class** that validates processing logic by comparing output image dimensions and types for both sequential and parallel methods.                                               |
| `Samples/`                | Directory containing **10 preloaded images** used for testing the filtering functionality.                                                                                                    |

---

## 🚀 How to Run the Application

### ✅ Prerequisites
- **Java 17+**
- **JUnit 5** library added to your project (for testing)
- A supported IDE like **IntelliJ IDEA**, **Eclipse**, or compile via terminal

### 🧑‍💻 Run Steps

1. **Clone or download** this repository.

2. **Add JUnit 5 Library** to your classpath:
   - If using IntelliJ: File > Project Structure > Libraries > Add > JUnit 5.
   - If using command-line: Download JUnit 5 JARs and add to `javac` and `java` classpath.

3. **Compile and Run the application from `ImageFilterUI.java`**.

---

## 📸 Features

- Choose from **10 sample images**
- Apply 5 types of filters: Edge Detection, Sharpen, Blur, Gaussian, Emboss
- Toggle between **Sequential** and **Parallel** modes
- Adjust **intensity** using slider
- View side-by-side comparison (Original vs Processed)
- Save the filtered image locally

---

## 🛠 Technologies Used

- Java 17
- Java Swing
- ForkJoinPool (Parallelism)
- JUnit 5 (Testing)

---

