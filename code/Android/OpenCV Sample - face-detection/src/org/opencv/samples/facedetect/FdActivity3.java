package org.opencv.samples.facedetect;

// Code reference for Kalman filter:
// https://github.com/Franciscodesign/Moving-Target-Tracking-with-OpenCV/blob/master/src/sonkd/Kalman.java
// http://www.morethantechnical.com/2011/06/17/simple-kalman-filter-for-tracking-using-opencv-2-2-w-code/
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.video.KalmanFilter;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

public class FdActivity3 extends Activity implements CvCameraViewListener2, OnClickListener {

	private static final String TAG = "OCVSample::Activity";
	private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
	public static final int JAVA_DETECTOR = 0;
	public static final int NATIVE_DETECTOR = 1;

	private MenuItem mItemFace50;
	private MenuItem mItemFace40;
	private MenuItem mItemFace30;
	private MenuItem mItemFace20;
	private MenuItem mItemType;

	private Mat mRgba;
	private Mat mGray;
	private Mat right;
	private File mCascadeFile;
	private CascadeClassifier mJavaDetector;
	private DetectionBasedTracker mNativeDetector;

	private int mDetectorType = JAVA_DETECTOR;
	private String[] mDetectorName;

	private float mRelativeFaceSize = 0.2f;
	private int mAbsoluteFaceSize = 0;
	private int imgnum = 1;
	private KalmanFilter kalman;

	private CameraBridgeViewBase mOpenCvCameraView;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");

				// Load native library after(!) OpenCV initialization
				System.loadLibrary("detection_based_tracker");

				try {
					// load cascade file from application resources
					//InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
					InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_default);
					File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
					//mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
					mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_default.xml");
					FileOutputStream os = new FileOutputStream(mCascadeFile);

					byte[] buffer = new byte[4096];
					int bytesRead;
					while ((bytesRead = is.read(buffer)) != -1) {
						os.write(buffer, 0, bytesRead);
					}
					is.close();
					os.close();

					mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
					if (mJavaDetector.empty()) {
						Log.e(TAG, "Failed to load cascade classifier");
						mJavaDetector = null;
					} else
						Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

					mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

					cascadeDir.delete();

				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
				}

				mOpenCvCameraView.enableView();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	public FdActivity3() {
		mDetectorName = new String[2];
		mDetectorName[JAVA_DETECTOR] = "Java";
		mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.face_detect_surface_view);

		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
		mOpenCvCameraView.setCameraIndex(1);
		mOpenCvCameraView.setCvCameraViewListener(this);
		Button click = (Button) findViewById(R.id.button1);
		click.setOnClickListener(this);
		click.setVisibility(View.GONE);

	}

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!OpenCVLoader.initDebug()) {
			Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
			OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
		} else {
			Log.d(TAG, "OpenCV library found inside package. Using it!");
			mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
		}
	}

	public void onDestroy() {
		super.onDestroy();
		mOpenCvCameraView.disableView();
	}

	public void onCameraViewStarted(int width, int height) {
		mGray = new Mat();
		mRgba = new Mat();
		right = new Mat();
		//Log.i(TAG, "Jai width is " + width + "x" + height);
		kalman = new KalmanFilter(4, 2, 0, CvType.CV_32F);
		Mat transitionMatrix = new Mat(4, 4, CvType.CV_32F, new Scalar(0));
		float[] tM = { 1, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1, 0, 0, 0, 0, 1 };
		transitionMatrix.put(0, 0, tM);
		kalman.set_transitionMatrix(transitionMatrix);
		Mat statePre = new Mat(4, 1, CvType.CV_32F, new Scalar(0));
		statePre.put(0, 0, width / 2);
		statePre.put(1, 0, height / 2);
		kalman.set_statePre(statePre);

		kalman.set_measurementMatrix(Mat.eye(2, 4, CvType.CV_32F));

		Mat processNoiseCov = Mat.eye(4, 4, CvType.CV_32F);
		processNoiseCov = processNoiseCov.mul(processNoiseCov, 1e-4);
		kalman.set_processNoiseCov(processNoiseCov);

		Mat id1 = Mat.eye(2, 2, CvType.CV_32F);
		id1 = id1.mul(id1, 1e-1);
		kalman.set_measurementNoiseCov(id1);

		Mat id2 = Mat.eye(4, 4, CvType.CV_32F);
		//id2 = id2.mul(id2, 0.1);
		kalman.set_errorCovPost(id2);
	}

	public void onCameraViewStopped() {
		mGray.release();
		mRgba.release();
		right.release();
	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		mRgba = inputFrame.rgba();
		mGray = inputFrame.gray();
		double W = mGray.cols();
		double H = mGray.rows();
		mRgba.setTo(new Scalar(0,0,0,0));
		mRgba.copyTo(right);
		/*
		 * Core.flip(mRgba,mRgba,0); Core.flip(mGray,mGray,0);
		 */

		if (mAbsoluteFaceSize == 0) {
			int height = mGray.rows();
			if (Math.round(height * mRelativeFaceSize) > 0) {
				mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
			}
			mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
		}

		MatOfRect faces = new MatOfRect();

		if (mDetectorType == JAVA_DETECTOR) {
			if (mJavaDetector != null)
				mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO:
																		// objdetect.CV_HAAR_SCALE_IMAGE
						new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
		} else if (mDetectorType == NATIVE_DETECTOR) {
			if (mNativeDetector != null)
				mNativeDetector.detect(mGray, faces);
		} else {
			Log.e(TAG, "Detection method is not selected!");
		}

		double f = 1449.3290;
		double thetaX = 0, thetaY = 0, maskX = 0.25 * W, maskY = 0.25 * H;
		Rect[] facesArray = faces.toArray();
		double faceX = mRgba.cols()/2, faceY = mRgba.rows()/2;
		//Log.i(TAG, "Jai width is " + faceX + "x" + faceY);
		int len = facesArray.length > 1 ? 1 : facesArray.length;
		for (int i = 0; i < len; i++) {
			faceX = facesArray[i].x + facesArray[i].width / 2;
			faceY = facesArray[i].y + facesArray[i].height / 2;
			//Imgproc.circle(mRgba, new Point(faceX, faceY), 5, new Scalar(255, 255, 0, 255));
			//Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
		}
		// Kalman filter update
		Mat prediction = kalman.predict();
		Mat measurement = new Mat(2, 1, CvType.CV_32F, new Scalar(0));
		measurement.put(0, 0, faceX);
		measurement.put(1, 0, faceY);
		
		Mat estimated = kalman.correct(measurement);
		double faceXKF = estimated.get(0, 0)[0];
		double faceYKF = estimated.get(1, 0)[0];
		// kalman filter done
		//Imgproc.circle(mRgba, new Point(faceXKF, faceYKF), 5, new Scalar(255, 0, 0, 255));
		Log.i(TAG, "Jai Predicted values are :"+ faceXKF+"x"+faceYKF);
		thetaX = Math.atan((faceXKF - mRgba.cols() / 2) / f) * 180 / Math.PI;
		thetaY = Math.atan((faceYKF - mRgba.rows() / 2) / f) * 180 / Math.PI;
		maskX = 0.25 * W - 0.5 * f * Math.tan(thetaX * Math.PI / 180);
		maskY = 0.25 * H + 0.5 * f * Math.tan(thetaY * Math.PI / 180);

		if (maskX >= 0 && maskX <= W && maskY >= 0 && maskY <= H) {
			Rect ROI = new Rect((int) (maskX), (int) (maskY), (int) (W / 2), (int) (H / 2));
			Scalar col = new Scalar(255, 255, 255, 255);
			mRgba.submat(ROI).setTo(col);
			int[][] source = { { 0, 0 }, { 0, (int) H }, { (int) W, 0 }, { (int) W, (int) H } };
			int[][] dest = { { (int) maskX, (int) maskY }, { (int) maskX, (int) (maskY + 0.5 * H) },
					{ (int) (maskX + 0.5 * W), (int) maskY }, { (int) (maskX + +0.5 * W), (int) (maskY + 0.5 * H) } };
			// Log.i("TAG", "JAI Length is " + source.length);
			for (int i = 0; i < source.length; i++) {
				Imgproc.line(mRgba, new Point(source[i][0], source[i][1]), new Point(dest[i][0], dest[i][1]), col, 4);
			}
		}
		
		//Mat dst = new Mat(); Core.addWeighted(mRgba, 0.25, cube, 0.75, 0, dst);
		return mRgba;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i(TAG, "called onCreateOptionsMenu");
		mItemFace50 = menu.add("Face size 50%");
		mItemFace40 = menu.add("Face size 40%");
		mItemFace30 = menu.add("Face size 30%");
		mItemFace20 = menu.add("Face size 20%");
		mItemType = menu.add(mDetectorName[mDetectorType]);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
		if (item == mItemFace50)
			setMinFaceSize(0.5f);
		else if (item == mItemFace40)
			setMinFaceSize(0.4f);
		else if (item == mItemFace30)
			setMinFaceSize(0.3f);
		else if (item == mItemFace20)
			setMinFaceSize(0.2f);
		else if (item == mItemType) {
			int tmpDetectorType = (mDetectorType + 1) % mDetectorName.length;
			item.setTitle(mDetectorName[tmpDetectorType]);
			setDetectorType(tmpDetectorType);
		}
		return true;
	}

	private void setMinFaceSize(float faceSize) {
		mRelativeFaceSize = faceSize;
		mAbsoluteFaceSize = 0;
	}

	private void setDetectorType(int type) {
		if (mDetectorType != type) {
			mDetectorType = type;

			if (type == NATIVE_DETECTOR) {
				Log.i(TAG, "Detection Based Tracker enabled");
				mNativeDetector.start();
			} else {
				Log.i(TAG, "Cascade detector enabled");
				mNativeDetector.stop();
			}
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.button1:
			saveImage(mRgba);
			break;
		}

	}

	private void saveImage(Mat img) {
		// TODO Auto-generated method stub
		Mat rgb = new Mat();
		Imgproc.cvtColor(img, rgb, Imgproc.COLOR_BGR2RGB);
		File path = new File(Environment.getExternalStorageDirectory() + "/opencv/");
		path.mkdirs();
		File file = new File(path, "image" + imgnum + ".png");
		imgnum++;
		String filename = file.toString();
		Boolean bool = Imgcodecs.imwrite(filename, rgb);
		Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		Uri contentUri = Uri.fromFile(file);
		mediaScanIntent.setData(contentUri);
		this.sendBroadcast(mediaScanIntent);

		if (bool)
			Log.i(TAG, "SUCCESS writing image to external storage");
		else
			Log.i(TAG, "Fail writing image to external storage");
	}
}
