import cv2
import time
import os
import serial

# Function to get Raspberry Pi CPU temperature
def get_cpu_temperature():
    temp = os.popen("vcgencmd measure_temp").readline()
    return temp.replace("temp=", "").strip()

# Initialize the serial connection (e.g., for a sensor connected to /dev/ttyUSB0)
ser = serial.Serial('COM3', 9600, timeout=1)  # Correct the serial device path
ser.flush()

# Initialize camera (e.g., /dev/video0 for the USB camera)
cap = cv2.VideoCapture('/dev/video1')  # Make sure the correct video device is used

# Set width and height of the camera frame
cap.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)

# Frame per second calculation
prev_frame_time = 0
new_frame_time = 0

while True:
    # Capture frame-by-frame
    ret, frame = cap.read()

    if not ret:
        print("Failed to grab frame")
        break

    # Calculate FPS
    new_frame_time = time.time()
    fps = 1 / (new_frame_time - prev_frame_time)
    prev_frame_time = new_frame_time
    fps_text = f"FPS: {int(fps)}"

    # Get CPU temperature
    cpu_temp = get_cpu_temperature()
    temp_text = f"Temp: {cpu_temp}"

    # Get data from the serial device
    if ser.in_waiting > 0:
        sensor_data = ser.readline().decode('utf-8').strip()
        sensor_text = f"Sensor: {sensor_data}"
    else:
        sensor_text = "Sensor: No data"

    # Display FPS, CPU temperature, and sensor data on the frame
    cv2.putText(frame, fps_text, (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 2)
    cv2.putText(frame, temp_text, (10, 70), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 2)
    cv2.putText(frame, sensor_text, (10, 110), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 2)

    # Display the resulting frame
    cv2.imshow('Camera', frame)

    # Press 'q' to quit the window
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

# Release the camera and close all windows
cap.release()
ser.close()
cv2.destroyAllWindows()