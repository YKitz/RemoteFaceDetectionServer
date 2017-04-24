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
@ProvidedServices(@ProvidedService(type=FaceDetectionService.class))
public class FaceDetectionAgent implements FaceDetectionService{

	
	CascadeClassifier mCascadeClassifier;
	MyFaceRecognizer mFaceRecognizer;
	@AgentBody
	public IFuture<Void> executeBody(IInternalAccess ia){
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		mCascadeClassifier = new CascadeClassifier(FaceDetectionAgent.class.getResource("lbpcascade_frontalface.xml").getPath().substring(1));
		
		mFaceRecognizer = new MyFaceRecognizer();
		
		
		return new Future<Void>();
	}
	
	int num =0;


	//string übertragung zum testen der verbindung
	@Override
	public String test(){
		num++;
		System.out.println("String was requested");
		return "String form PC Platform" + num;
	}
	
	//gibt den Rahmen um die gesichter zurück
	@Override
	public Tuple2Future<List<Integer>, byte[]> getFrame(int id, byte[] input) {


		long startTime = System.currentTimeMillis();
		
		Tuple2Future<List<Integer>, byte[]> fut = new Tuple2Future<List<Integer>, byte[]>();
		int count = 1;
		List<Integer> rectData = new ArrayList<Integer>() ;
		rectData.add(id);
		byte[] thumbnail = new byte[0];
		BufferedImage bi;
		int label = 0;
		try {
			bi = ImageIO.read(new ByteArrayInputStream(input));
			//ImageIO.write(bi, "jpg", new File("C:\\test\\test.jpg"));
			

			
			Mat mat = bufferedImageToMat(bi);
	     	
	    	MatOfRect faces = new MatOfRect();
			if (mCascadeClassifier != null){
				mCascadeClassifier.detectMultiScale(mat, faces, 1.1, 2, 2, new Size(100,100), new Size());
				
			}
			System.out.println("face detection was called id: "+ id);
			
			
			
			Rect[] facesArray = faces.toArray();
			

	            
	    		for(Rect face : facesArray){
	    			
//	    			testRect = testRect + "height: " + face.height;
//	    			testRect = testRect + "width: " + face.width;
//	    			testRect = testRect + "x: " + face.x;
//	    			testRect = testRect + "y: " + face.y;
//	    		
//	    			
	    			rectData.add(face.x);
	    			rectData.add(face.y);
	    			rectData.add(face.width);
	    			rectData.add(face.height);
	 
	    			}
	    	
	    		fut.setFirstResult(rectData);
	    		long fendTime = System.currentTimeMillis();
	    		System.out.println("Dauer first Result: " + (fendTime-startTime) + " milliseconds");	
	    		//for(Rect face : facesArray){
	    		/*
    			 * testen was wieviel zeit kostet
    			 */
	    		if(facesArray.length>0){
	    		Rect face = facesArray[0];
    			Mat cropImg = new Mat(mat, face);
    			Mat resizedImg = new Mat();
    			Size size = new Size(500,500);
    			Imgproc.resize(cropImg, resizedImg, size);
    			bi = matToBufferedImage(resizedImg, bi);
    			ImageIO.write(bi, "jpg", new File("C:\\inputPictures\\" + count +"-test.jpg"));
    			
    			label = mFaceRecognizer.recognizeFace("C:\\inputPictures\\"+ count +"-test.jpg");
    			count++;
	    		}
	    		//}
	    		if(label>0){
	    		thumbnail = Files.readAllBytes(new File("C:\\trainingPictures\\" + label +"-test.jpg").toPath());
    			//System.out.println("bytes: " + thumbnail.length);
	    		}
    		
			
	    		//zeichnet rects ein
	    		/*
            for (int i = 0; i < facesArray.length; i++)
                Imgproc.rectangle(mat, facesArray[i].tl(),
                        facesArray[i].br(), new Scalar(100), 3);
            
	    		 	*/
               
            
	     	
	     
			
		} catch (IOException e) {
			System.out.println("IOException beim umwandeln des byteArray");
		}	
		
		fut.setSecondResult(thumbnail);
		long endTime = System.currentTimeMillis();
		System.out.println("Dauer second Result: " + (endTime-startTime) + " milliseconds");
		return fut;
	}
	
	public BufferedImage matToBufferedImage(Mat matrix, BufferedImage bimg)
	{
	    if ( matrix != null ) { 
	        int cols = matrix.cols();  
	        int rows = matrix.rows();  
	        int elemSize = (int)matrix.elemSize();  
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
	            for(int i=0; i<data.length; i=i+3) {  
	                b = data[i];  
	                data[i] = data[i+2];  
	                data[i+2] = b;  
	            }  
	            break;  
	        default:  
	            return null;  
	        }  

	        // Reuse existing BufferedImage if possible
	        if (bimg == null || bimg.getWidth() != cols || bimg.getHeight() != rows || bimg.getType() != type) {
	            bimg = new BufferedImage(cols, rows, type);
	        }        
	        bimg.getRaster().setDataElements(0, 0, cols, rows, data);
	    } else { // mat was null
	        bimg = null;
	    }
	    return bimg;  
	}   
	
	
	
	public static Mat bufferedImageToMat(BufferedImage bi) {
		  Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
		  byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
		  mat.put(0, 0, data);
		  return mat;
		}

}
