package project.recognition.skin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

public class ForeheadHistogramRecognizer implements SkinRecognizer {

	private final CascadeClassifier classifier =
			new CascadeClassifier("classifiers/haarcascade_frontalface_alt.xml");
	
	
	private final Rect[] detectFace(Mat mat) {
		Mat grayImg = new Mat();
		MatOfRect objects = new MatOfRect();
		Imgproc.cvtColor(mat, grayImg, Imgproc.COLOR_RGB2GRAY);
		classifier.detectMultiScale(grayImg, objects);

		return objects.toArray();
	}
	
	
   @Override
   public Map<String, Integer> detectSkin(Mat mat) {
      Rect[] faces = detectFace(mat);
      int cr = 0;
      int cb = 0;
      
      // should we really be checking all detected faces? (cnh)
      for (int i = 0; i < faces.length; i++) {
         Mat dest = new Mat();
         Imgproc.cvtColor(mat, dest, Imgproc.COLOR_RGB2YCrCb);
         List<Mat> colorSplits = new ArrayList<Mat>();
         int histSize = 256;
         float range[] = { 0, 256 };
         
         // get forehead from face
         Core.split( new Mat(dest, new Rect((int)faces[i].tl().x, (int)faces[i].tl().y, faces[i].width, faces[i].height / 4)), colorSplits);
         Mat hist2 = new Mat(), hist3 = new Mat();
         
         Imgproc.calcHist(Arrays.asList(colorSplits.get(1)), new MatOfInt(0), new Mat(), hist2, new MatOfInt(histSize), new MatOfFloat(range));
         Imgproc.calcHist(Arrays.asList(colorSplits.get(2)), new MatOfInt(0), new Mat(), hist3, new MatOfInt(histSize), new MatOfFloat(range));
         int total2 = 0, total3 = 0;
         int elementsTotal = 0, elements2Total = 0;
         for(int row = 0; row < hist2.rows(); row++) {
             total2 += (row * hist2.get(row, 0)[0]);
             elementsTotal += hist2.get(row, 0)[0];
         }
         for(int row = 0; row < hist3.rows(); row++) {
            total3 += (row * hist3.get(row, 0)[0]);
            elements2Total += hist3.get(row, 0)[0];
         }
         cr = total2 / elementsTotal;
         cb = total3 / elements2Total;
      }
      
      Map<String, Integer> colorMap = new HashMap<>();
      final int tolerence = 10;
      colorMap.put(CrMIN, cr - tolerence);
      colorMap.put(CrMAX, cr + tolerence);
      colorMap.put(CbMIN, cb - tolerence);
      colorMap.put(CbMAX, cb + tolerence);
      return colorMap;
   }
}

