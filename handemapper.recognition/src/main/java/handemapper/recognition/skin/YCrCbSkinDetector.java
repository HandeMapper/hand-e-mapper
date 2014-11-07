package handemapper.recognition.skin;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class YCrCbSkinDetector {
	
	public static Mat detectSkin(Mat img, Scalar min, Scalar max) {
		Mat ycrcbImg = new Mat();
		
		// Converts the given image to a YCrCb image
		Imgproc.cvtColor(img, ycrcbImg, Imgproc.COLOR_BGR2YCrCb);
		
		// Creates a grayscale image
		Mat skinImg = new Mat(ycrcbImg.width(), ycrcbImg.height(), CvType.CV_8UC1);
		
		// Obtains all the pixels, within a specified range, in ycrcbImg and places them in skinImg
		Core.inRange(ycrcbImg, min, max, skinImg);
		
		// Creates a rectangular-shaped kernel to be used in the following image erosion
		Mat rectKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(6, 6));
		// Erodes the selected pixels, removing small white noise in skinImg
		Imgproc.erode(skinImg, skinImg, rectKernel, new Point(3, 3), 2);
		
		// Creates a rectangular-shaped kernel to be used in the following image dilation
		rectKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
		// Dilates the selected pixels, enlarging the darker-colored objects since erosion previously shrunk them
		Imgproc.dilate(skinImg, skinImg, rectKernel, new Point(1, 1), 2);
		
		// Returns a grayscale image where the white pixels in the image are skin pixels
		return skinImg;
	}
}
