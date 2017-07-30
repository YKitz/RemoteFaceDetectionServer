/*
 *
 * License
By downloading, copying, installing or using the software you agree to this license. If you do not agree to this license, do not download, install, copy or use the software.

License Agreement
For Open Source Computer Vision Library
(3-clause BSD License)
Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
Neither the names of the copyright holders nor the names of the contributors may be used to endorse or promote products derived from this software without specific prior written permission.
This software is provided by the copyright holders and contributors “as is” and any express or implied warranties, including, but not limited to, the implied warranties of merchantability and fitness 
for a particular purpose are disclaimed. In no event shall copyright holders or contributors be liable for any direct, indirect, incidental, special, exemplary, or consequential damages
 (including, but not limited to, procurement of substitute goods or services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability, 
 whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use of this software, even if advised of the possibility of such damage.
 */

package com.example.yannic.remotefacedetection.agent;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.bytedeco.javacpp.opencv_face.FaceRecognizer;
import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import jadex.bridge.IInternalAccess;

import jadex.bridge.service.annotation.Service;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.future.Tuple2Future;
import jadex.micro.annotation.*;
import myMain.MyFaceRecognizer;

@Description("Agent zur Ausführund der face detection")
@Agent
@Service
@ProvidedServices(@ProvidedService(type = FaceDetectionService.class))
public class FaceDetectionAgent implements FaceDetectionService {

	CascadeClassifier mCascadeClassifier;
	MyFaceRecognizer mFaceRecognizer;

	@AgentBody
	public IFuture<Void> executeBody(IInternalAccess ia) {

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		mCascadeClassifier = new CascadeClassifier(
				FaceDetectionAgent.class.getResource("lbpcascade_frontalface.xml").getPath().substring(1));

		mFaceRecognizer = new MyFaceRecognizer();

		return new Future<Void>();
	}

	int num = 0;

	// string übertragung zum testen der verbindung
	@Override
	public String test() {
		num++;
		System.out.println("String was requested");
		return "String form PC Platform" + num;
	}

	// gibt den Rahmen um die gesichter und ein erkanntes ähnliches gesicht
	// zurück
	@Override
	public Tuple2Future<List<Integer>, byte[]> getFrame(int id, byte[] input) {
		int count = 1;

		long startTime = System.currentTimeMillis();

		Tuple2Future<List<Integer>, byte[]> fut = new Tuple2Future<List<Integer>, byte[]>();

		List<Integer> rectData = new ArrayList<Integer>();
		rectData.add(id);
		byte[] thumbnail = new byte[0];
		BufferedImage bi;
		int label = 0;
		try {

			bi = ImageIO.read(new ByteArrayInputStream(input));

			Mat mat = bufferedImageToMat(bi);

			MatOfRect faces = new MatOfRect();
			if (mCascadeClassifier != null) {
				mCascadeClassifier.detectMultiScale(mat, faces, 1.1, 2, 2, new Size(100, 100), new Size());

			}
			System.out.println("face detection was called id: " + id);

			Rect[] facesArray = faces.toArray();

			for (Rect face : facesArray) {

				// testRect = testRect + "height: " + face.height;
				// testRect = testRect + "width: " + face.width;
				// testRect = testRect + "x: " + face.x;
				// testRect = testRect + "y: " + face.y;
				//
				//
				rectData.add(face.x);
				rectData.add(face.y);
				rectData.add(face.width);
				rectData.add(face.height);

			}

			fut.setFirstResult(rectData);
			long fendTime = System.currentTimeMillis();
			System.out.println("Dauer first Result: " + (fendTime - startTime) + " milliseconds");
			// for(Rect face : facesArray){
			/*
			 * prep for recognition
			 */
			if (facesArray.length > 0) {

				for (int i = 0; i < facesArray.length; i++) {

					Rect face = facesArray[i];
					Mat cropImg = new Mat(mat, face);
					Mat resizedImg = new Mat();
					Size size = new Size(92, 112);
					Imgproc.resize(cropImg, resizedImg, size);
					bi = matToBufferedImage(resizedImg, bi);
					ImageIO.write(bi, "jpg", new File("C:\\inputPictures\\(" + count + ")-test.jpg"));

					label = mFaceRecognizer.recognizeFace("C:\\inputPictures\\(" + count + ")-test.jpg");
					count++;

					// }
					if (label > 0) {

						byte[] tempface = Files
								.readAllBytes(new File("C:\\trainingPictures\\(" + label + ")-test.jpg").toPath());
						byte[] combined = new byte[tempface.length + thumbnail.length];

						for (int x = 0; x < combined.length; x++) {
							combined[x] = x < thumbnail.length ? thumbnail[x] : tempface[x - thumbnail.length];
						}

						thumbnail = combined;
						// System.out.println("bytes: " + thumbnail.length);
					}
				}
			}
			// zeichnet rects ein
			/*
			 * for (int i = 0; i < facesArray.length; i++)
			 * Imgproc.rectangle(mat, facesArray[i].tl(), facesArray[i].br(),
			 * new Scalar(100), 3);
			 * 
			 */

			long endTime = System.currentTimeMillis();
			System.out.println("Dauer second Result: " + (endTime - startTime) + " milliseconds");

			fut.setSecondResult(thumbnail);

			return fut;
		} catch (IOException e) {
			System.out.println("IOException beim umwandeln des byteArray");
		}

		long endTime = System.currentTimeMillis();
		System.out.println("Dauer second Result: " + (endTime - startTime) + " milliseconds");
		return fut;
	}

	/*
	 * face recognition bei lokaler detection
	 */
	public Tuple2Future<byte[], Integer> recognizeFace(int id, byte[] inputFace) {
		int c = 0;
		long startTime = System.currentTimeMillis();

		byte[] recognizedFace = new byte[0];
		System.out.println(id);
		BufferedImage bi;
		try {
			bi = ImageIO.read(new ByteArrayInputStream(inputFace));

			Mat input = bufferedImageToMat(bi);

			Mat resizedImg = new Mat();
			Size size = new Size(92, 112);
			Imgproc.resize(input, resizedImg, size);
			bi = matToBufferedImage(resizedImg, bi);
			ImageIO.write(bi, "jpg", new File("C:\\inputPicturesFace\\" + c + "-test.jpg"));

			int label = mFaceRecognizer.recognizeFace("C:\\inputPicturesFace\\" + c + "-test.jpg");

			c++;
			recognizedFace = Files.readAllBytes(new File("C:\\trainingPictures\\(" + label + ")-test.jpg").toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Tuple2Future fut = new Tuple2Future<byte[], Integer>();
		fut.setFirstResult(recognizedFace);
		fut.setSecondResult(id);
		long endTime = System.currentTimeMillis();
		System.out.println("Dauer Result: " + (endTime - startTime) + " milliseconds");

		return fut;
	}

	public static Mat bufferedImageToMat(BufferedImage bi) {
		Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
		byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
		mat.put(0, 0, data);
		return mat;
	}

	public static BufferedImage matToBufferedImage(Mat matrix, BufferedImage bimg) {
		if (matrix != null) {
			int cols = matrix.cols();
			int rows = matrix.rows();
			int elemSize = (int) matrix.elemSize();
			byte[] data = new byte[cols * rows * elemSize];
			int type;
			matrix.get(0, 0, data);
			switch (matrix.channels()) {
			case 1:
				type = BufferedImage.TYPE_BYTE_GRAY;
				break;
			case 3:
				type = BufferedImage.TYPE_3BYTE_BGR;
				// bgr to rgb
				byte b;
				for (int i = 0; i < data.length; i = i + 3) {
					b = data[i];
					data[i] = data[i + 2];
					data[i + 2] = b;
				}
				break;
			default:
				return null;
			}

			if (bimg == null || bimg.getWidth() != cols || bimg.getHeight() != rows || bimg.getType() != type) {
				bimg = new BufferedImage(cols, rows, type);
			}
			bimg.getRaster().setDataElements(0, 0, cols, rows, data);
		} else {
			bimg = null;
		}
		return bimg;
	}

}

// Für multi threading der Gesichtserkennung
// class MyRunnable implements Runnable{
// Rect[] runnableFacesArray;
// Mat inputPicture;
// BufferedImage bufferedImage;
// MyFaceRecognizer faceRecognizer;
// int count;
// int i;
//
// public void set(Rect[] fa, Mat m, MyFaceRecognizer fr, int c, int faceIndex
// ){
//
//
//
// }
//
// @Override
// public void run() {
// System.out.println("Thread started:::"+Thread.currentThread().getName());
// Rect face = runnableFacesArray[i];
// Mat cropImg = new Mat(inputPicture, face);
// Mat resizedImg = new Mat();
// Size size = new Size(92, 112);
// Imgproc.resize(cropImg, resizedImg, size);
// bufferedImage = FaceDetectionAgent.matToBufferedImage(resizedImg,
// bufferedImage);
// ImageIO.write(bufferedImage, "jpg", new File("C:\\inputPictures\\(" + count +
// ")-test.jpg"));
//
// lableList.add( faceRecognizer.recognizeFace("C:\\inputPictures\\(" + count +
// ")-test.jpg"));
// System.out.println("Thread ended:::"+Thread.currentThread().getName());
// }
//
// }
