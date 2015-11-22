Fall 2015 16811 Project Outline

Introduction
The aim of this project is to explore how to calculate the orientation of a smart phone with respect to a user's face, provided that there is only one user, the user is stationary, and that user is in the frame and looking at the camera.  Cases such as multiple users and no users being in the camera's frame will not be considered.  We will also assume that lighting conditions will be constant and that there is not excessive vibration being applied to the phone, such as when riding in a car.

Our hypothesis is that the camera will give the best estimate of the camera's 3D location, but will not be able to account fully for its orientation.  We further hypothesize that IMU sensor data will be prone to drift, bias, and in general, error.

Motivation
-Dynamic perspective
-Other use cases where knowing the orientation between user and phone is beneficial

Background
-Degrees of freedom of camera and IMU 

Related Work
-Cite papers in folder, discuss them a bit

Methods
-Quaternions-
-IMU background
-Trig for desired output-
-Face detection algorithm (Viola-Jones?)

Results
Camera experiments, these are assuming a canonical phone orientation:
-arm arcs, face stationary (x and y axis)
-arm arcs, face moving with camera (x and y axis)
-stationary phone, stationary face (control, we should observe almost no change)
-stationary phone, moving face (this should produce the same result as the arm arcs in experiment 1)
-Improvemnents on Raw Data

IMU experiments, these are assuming a canonical face:
-stationary phone (control x,y,z axis)
-arm arc (x and y axis)
-arm flex (x and y axis, should produce similar results to experiment 3)
-Improvemnents on Raw Data
  -Kalman filter (rerun experiments)

Error metrics: In the stationary cases (controls), the L1, L2 errors will be computed and we will discuss which is a better metric to use.  We will qualitatively asses the performance of the other experiments

Conclusion
We will compare how the camera and the IMU performed and propose how to fuse the data for future work (in the context of a much larger project).
