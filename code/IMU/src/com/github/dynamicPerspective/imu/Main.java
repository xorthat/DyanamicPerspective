package com.github.dynamicPerspective.imu;

import org.opencv.core.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;


public class Main {

    public static void main(String[] args) {
        int rate = 10;
        int seconds = 10;
        ArrayList<PhoneData> data = new ArrayList<PhoneData>();
        // Load the native library.
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        // Create a socket to listen on the port.
        try {
            Socket clientSocket = new Socket(InetAddress.getByName("10.0.0.11"), 54721);
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            for(int i = 0; i < rate*seconds; i++) {
                data.add(new PhoneData(inFromServer.readLine()));
            }
            clientSocket.close();
        }
        catch(UnknownHostException e)
        {
            System.out.println("Cannot connect to sensor data");
        }
        catch(SocketException e)
        {
            System.out.println("Cannot connect to socket");
        }
        catch(IOException e) {
            System.out.println("Error encountered while reading data");
        }
    }
}

//class DetectFaceDemo {
//    public void run() {
//        System.out.println("\nRunning com.jelake.application.DetectFaceDemo");
//
//        // Create a face detector from the cascade file in the resources
//        // directory.
//        CascadeClassifier faceDetector = new CascadeClassifier("//Users//Picard//Documents//ComputerVisionProject//src//com//jelake//application//lbpcascade_frontalface.xml");
//        Mat image = Highgui.imread("//Users//Picard//Documents//ComputerVisionProject//src//com//jelake//application//lena.png");
//
//        // Detect faces in the image.
//        // MatOfRect is a special container class for Rect.
//        MatOfRect faceDetections = new MatOfRect();
//        faceDetector.detectMultiScale(image, faceDetections);
//
//        System.out.println(String.format("Detected %s faces", faceDetections.toArray().length));
//
//        // Draw a bounding box around each face.
//        for (Rect rect : faceDetections.toArray()) {
//            Core.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
//        }
//
//        // Save the visualized detection.
//        String filename = "faceDetection.png";
//        System.out.println(String.format("Writing %s", filename));
//        Highgui.imwrite(filename, image);
//    }
//}