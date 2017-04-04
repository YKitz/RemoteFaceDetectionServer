
package myMain;



import java.io.File;
import java.io.FilenameFilter;
import java.nio.IntBuffer;

import static org.bytedeco.javacpp.opencv_core.CV_32SC1;

import static org.bytedeco.javacpp.opencv_face.createFisherFaceRecognizer;
// import static org.bytedeco.javacpp.opencv_face.createEigenFaceRecognizer;
// import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;


import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.opencv_face.FaceRecognizer;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;

public class MyFaceRecognizer {
   
	FaceRecognizer faceRecognizer;
	
	public MyFaceRecognizer() {
        String trainingDir = "C:\\trainingPictures";
        
        File root = new File(trainingDir);

        FilenameFilter imgFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
            }
        };

        File[] imageFiles = root.listFiles(imgFilter);

       
        MatVector images = new MatVector(imageFiles.length);

        Mat labels = new Mat(imageFiles.length, 1, CV_32SC1);
        IntBuffer labelsBuf = labels.createBuffer();

        int counter = 0;

        for (File image : imageFiles) {
            Mat img = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
            
            int label = Integer.parseInt(image.getName().split("\\-")[0]);
            System.out.println("Label: " + label);
            images.put(counter, img);

            labelsBuf.put(counter, label);

            counter++;
        }

        faceRecognizer = createFisherFaceRecognizer();
        // FaceRecognizer faceRecognizer = createEigenFaceRecognizer();
        // FaceRecognizer faceRecognizer = createLBPHFaceRecognizer()
        System.out.println("start training");
        faceRecognizer.train(images, labels);
        System.out.println("done training");

        
    }
	
	
	public int recognizeFace(String path){

		System.out.println("recognizeFace called");
        IntPointer label = new IntPointer(1);
        DoublePointer confidence = new DoublePointer(1);
		Mat testImage = imread(path, CV_LOAD_IMAGE_GRAYSCALE);
        faceRecognizer.predict(testImage, label, confidence);
        int predictedLabel = label.get(0);

        System.out.println("Predicted label: " + predictedLabel + " confidence: " + confidence.get());

		
		return predictedLabel;
	}
}