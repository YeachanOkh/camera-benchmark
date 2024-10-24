import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.Duration;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class CameraApp {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // Load OpenCV library
    }

    // Function to get CPU temperature from Raspberry Pi
    public static String getCpuTemperature() {
        String temp = "N/A";
        try {
            Process process = Runtime.getRuntime().exec("vcgencmd measure_temp");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            temp = reader.readLine().replace("temp=", "").trim();
        } catch (Exception e) {
            System.out.println("Error getting CPU temperature: " + e.getMessage());
        }
        return temp;
    }

    // Convert Mat object (OpenCV) to BufferedImage (Java AWT)
    public static BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        DataBufferByte buffer = (DataBufferByte) image.getRaster().getDataBuffer();
        byte[] targetPixels = buffer.getData();
        mat.get(0, 0, targetPixels);
        return image;
    }

    public static void main(String[] args) {

        // Initialize the camera
        VideoCapture camera = new VideoCapture(0); // Use camera 0 (default)
        if (!camera.isOpened()) {
            System.out.println("Error: Camera not opened");
            return;
        }

        JFrame frame = new JFrame("Camera App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel imageLabel = new JLabel();
        frame.add(imageLabel);
        frame.setVisible(true);

        Mat mat = new Mat(); // OpenCV Mat to hold frame data
        long prevTime = Instant.now().toEpochMilli(); // To calculate FPS

        while (true) {
            if (camera.read(mat)) {
                // Calculate FPS
                long currentTime = Instant.now().toEpochMilli();
                long elapsedTime = currentTime - prevTime;
                double fps = 1000.0 / elapsedTime;
                prevTime = currentTime;

                // Get CPU temperature
                String cpuTemp = getCpuTemperature();

                // Overlay FPS and temperature on the image
                Imgproc.putText(mat, String.format("FPS: %.2f", fps), new Point(10, 30),
                        Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 255, 0), 2);
                Imgproc.putText(mat, "Temp: " + cpuTemp, new Point(10, 70),
                        Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 255, 0), 2);

                // Convert Mat to BufferedImage and display
                BufferedImage image = matToBufferedImage(mat);
                ImageIcon imageIcon = new ImageIcon(image);
                imageLabel.setIcon(imageIcon);
                frame.pack();

                // Delay for a short time to control frame rate
                try {
                    Thread.sleep(33); // ~30 FPS
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } else {
                System.out.println("Error: Could not read frame");
                break;
            }

            // Close the application when the window is closed
            if (!frame.isVisible()) {
                break;
            }
        }

        camera.release(); // Release the camera resource
    }
}
