package handemapper.recognition.skin;

import java.util.HashMap;
import java.util.Map;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

public class PointsOfInterestRecognizer implements SkinRecognizer {

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
      double[] mf, lc, rc;
      Mat dest = new Mat();
      Imgproc.cvtColor(mat, dest, Imgproc.COLOR_RGB2YCrCb);
      
      // Should we really be checking all detected faces? (cnh)
      for (int i = 0; i < faces.length; i++) {
         //middle of forehead
         mf = dest.get(
        		 (int)faces[i].tl().x + (faces[i].width / 2),
        		 (int)faces[i].tl().y - (faces[i].height / 8)
         );
         
         //left cheekbone
         lc = dest.get(
        		 (int)faces[i].tl().x + (faces[i].width / 4),
        		 (int)faces[i].tl().y - (faces[i].height / 2)
         );
         
         //right cheekbone
         rc = dest.get(
        		 (int)faces[i].tl().x + (faces[i].width * 3 / 4),
        		 (int)faces[i].tl().y - (faces[i].height / 2)
         );
         
         cr = (int) ((mf[1] + lc[1] + rc[1]) / 3);
         
         cb = (int) ((mf[2] + lc[2] + rc[2]) / 3);
      }
      
      Map<String, Integer> colorMap = new HashMap<>();
      final int tolerence = 20;
      colorMap.put(CrMIN, cr - tolerence);
      colorMap.put(CrMAX, cr + tolerence);
      colorMap.put(CbMIN, cb - tolerence);
      colorMap.put(CbMAX, cb + tolerence);
      return colorMap;
   }

}
