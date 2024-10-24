import cv2

# Initialize the camera (change to '/dev/video0' or the correct video device if necessary)
camera = cv2.VideoCapture(0)  # Change this to your video device

# Check if the camera opened successfully
if not camera.isOpened():
    print("Error: Could not open video device.")
    exit()

# Set camera resolution (optional)
camera.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
camera.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)

# Loop to continuously capture frames
while True:
    # Capture frame-by-frame
    ret, frame = camera.read()

    # If frame capture was successful
    if ret:
        # Display the resulting frame
        cv2.imshow('Webcam', frame)

    # Press 'q' to quit the window
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

# When done, release the camera and close windows
camera.release()
cv2.destroyAllWindows()
