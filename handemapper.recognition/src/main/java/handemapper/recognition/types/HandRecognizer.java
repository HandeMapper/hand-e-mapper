package handemapper.recognition.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import handemapper.recognition.AbstractGesture;
import handemapper.recognition.skin.SkinRecognizer;
import handemapper.recognition.skin.YCrCbSkinDetector;

import handemapper.common.recognition.event.GestureEvent;


/**
 * 
 * @author Chris Hartley
 */
public class HandRecognizer extends AbstractGesture {

	/*private class NullContourException extends Exception {
		public NullContourException() { super(); }
		public NullContourException(String message) { super(message); }
		public NullContourException(String message, Throwable cause) { super(message, cause); }
		public NullContourException(Throwable cause) { super(cause); }
	}*/
	
	
	// Add a initialize function to AbstractGesture to set calibration flag
		// Thus GestureRecognizerWorker will grab skin color if calibration flag isn't set
		// If the calibration flag isn't set, it's assumed that a color space has been defined for skin color
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4630440503905990319L;
	private static final int recentStatesMemCapacity = 5;
	// TODO It'd probably be a good idea to add a treshold + or - button to the GUI
	private static final double movementThreshold = 10.0;
	
	private static final boolean showSkinImg = false;
	
	private Scalar ycrcbPresetMin = new Scalar(0, 131, 80);
	private Scalar ycrcbPresetMax = new Scalar(255, 185, 135);
	private Scalar ycrcbMin = ycrcbPresetMin;
	private Scalar ycrcbMax = ycrcbPresetMax;
	
	private MatOfInt hull = new MatOfInt();
	private ArrayList<Point> filteredHull = new ArrayList<Point>();
	private RotatedRect enclosingRect = new RotatedRect();
	private MatOfInt4 defects = new MatOfInt4();
	ArrayList<Point> cogShapePts = new ArrayList<Point>();
	
	/*
	MatOfInt4 defects structure
	   Integer start; // Index of point of the contour where the defect begins
	   Integer end; // Index of point of the contour where the defect ends
	   Integer depth_point; // Index of the farthest from the convex hull point within the defect
	   Integer depth; // Distance between the farthest point and the convex hull
	*/
	private int[] defectsArray = {};
	MatOfPoint biggestContour = null;

	private enum GestureType {
		GRAB, DROP, DRAG, CLICK, THROW, UNKNOWN
	};
	private GestureType currentGesture = GestureType.UNKNOWN;
	
	private enum HandState {
		OPEN, CLOSED, MOVING_OPEN, MOVING_CLOSED, OTHER
	};
	private HandState currentState = HandState.OTHER;
	private Point currentPos = new Point();
	private double currentArea = 0.0;
	
	
	private class HandInfo {
		private HandState state; // Indicates state of hand (for gestures)
		private Point position; // Indicates position of center of hand (for positioning mouse input)
		private double area; // Indicates size of hand (for identifying throw gesture)
		
		public HandInfo(HandState state, Point position, double area) {
			this.state = state;
			this.position = position;
			this.area = area;
		}
		
		public HandState getState() {
			return this.state;
		}
		
		public Point getPosition() {
			return this.position;
		}
		
		public double getArea() {
			return this.area;
		}
	}
	
	//private SizedStack<HandInfo> pastHandInfo = new SizedStack<HandInfo>(recentStatesMemCapacity); 
	
	public HandRecognizer(String name) {
		super(name);
	}
	
	
	public HandRecognizer(String name, boolean enabled) {
		super(name, enabled);
	}
	
	
	public HandRecognizer(String name, String desc) {
		super(name, desc, true);
	}
	
	
	public HandRecognizer(String name, String desc, boolean enabled) {
		super(name, desc, enabled);
	}

	private final SkinRecognizer skinRec =
			//new handemapper.recognition.skin.ForeheadHistogramRecognizer();
			new handemapper.recognition.skin.FixedRangeRecognizer();
			//new handemapper.recognition.skin.PointsOfInterestRecognizer();
	
	
	@Override
	public void detect(Mat matrix) {
		boolean contourFound = false;
		
		if (needsInitializing()) {
			Map<String, Integer> skinRng = skinRec.detectSkin(matrix);
			ycrcbMin = new Scalar(0, skinRng.get(SkinRecognizer.CrMIN), skinRng.get(SkinRecognizer.CbMIN));
			ycrcbMax = new Scalar(255, skinRng.get(SkinRecognizer.CrMAX), skinRng.get(SkinRecognizer.CbMAX));
			initialized();
			System.out.println("initialized!");
		}
		
		//System.out.println("mid=[" + Arrays.toString(matrix.get(matrix.width() >> 1, matrix.height() >> 1)) + "]");
			
		// Detects skin pixels in the given Mat (image)
		Mat skinImg = YCrCbSkinDetector.detectSkin(matrix, ycrcbMin, ycrcbMax);
		
		cogShapePts.clear();
		
		if(showSkinImg)
			skinImg.copyTo(matrix);
		else {
			contourFound = extractContourAndHull(matrix, skinImg);
			
			if(contourFound) {
				detectAndCountFingers(matrix);
				//this.pastHandInfo.push(new HandInfo(this.currentState, this.currentPos, this.currentArea));
				
				/*if(pastHandInfo.toArray().length >= 2)
					identifyGesture(pastHandInfo);*/
			}
			else {
				this.initialize();
			}
			
		}
		
	}
	
	
	/**
	 * Obtains the contour and contour hull of the given image
	 * 
	 * @param origImg RGB-colored frame from camera feed
	 * @param skinImg Grayscale image of object
	 * @throws NullContourException 
	 * @return Boolean indicating if contour was found or not
	 */
	private boolean extractContourAndHull(Mat origImg, Mat skinImg) {
		List<MatOfPoint> contours = new ArrayList<>();
		MatOfPoint2f currentContour = new MatOfPoint2f();
		//Point[] vertices = {};
		
		// Obtains the contours in the binary skin image
		Imgproc.findContours(skinImg, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		
		// Obtains the biggest contour from the found contours
		double biggestArea = 0;
		for(int i = 0; i < contours.size(); i++) {
			double curArea = Imgproc.contourArea(contours.get(i));
			
			if(curArea > biggestArea) {
				biggestArea = curArea;
				biggestContour = contours.get(i);
			}
		}
		
		// If a biggest contour wasn't found
		if(biggestContour == null) {
			return false;
		}
		
		this.currentArea = biggestArea;
		
		MatOfPoint2f contourCpy = new MatOfPoint2f(biggestContour.toArray());
		
		// Approximates the biggest contour
		Imgproc.approxPolyDP(contourCpy, currentContour, Imgproc.arcLength(contourCpy, true) * 0.0025, true);
		
		// Draws the approximated contour using green-colored lines
		List<MatOfPoint> tempContourList = new ArrayList<>();
		tempContourList.add(new MatOfPoint(currentContour.toArray()));
		Imgproc.drawContours(origImg, tempContourList, -1, new Scalar(0, 255, 0));
		biggestContour = new MatOfPoint(currentContour.toArray());
		
		
		// Obtains the smallest convex set that contains the biggest counter
		Imgproc.convexHull(biggestContour, hull, true);
		
		// Obtains the rectangle with the smallest area that encloses the biggest contour
		enclosingRect = Imgproc.minAreaRect(new MatOfPoint2f(biggestContour.toArray()));
		
		// Obtains the vertices of the enclosing rectangle
		//enclosingRect.points(vertices);
		
		// Converts the hull points from integers to Points
		ArrayList<Point> hullPtsList = new ArrayList<Point>();
		int[] indices = hull.toArray();
		Point[] contourPts = biggestContour.toArray();
		for(int i = 0/*, j = 0*/; i < indices.length/* - 1*/; i++/*, j+=2*/) {
			hullPtsList.add(contourPts[indices[i]]);
			//hullPts[i] = new Point(indices[j], indices[j+1]);
		}
		
		Point[] hullPts = new Point[hullPtsList.size()];
		for(int i = 0; i < hullPtsList.size(); i++) {
			hullPts[i] = hullPtsList.get(i);
		}
		
		ArrayList<MatOfPoint> hullContour = new ArrayList<MatOfPoint>();
		hullContour.add(new MatOfPoint(hullPts));
		
		//cogShapePts = findLowestPoints(new ArrayList<Point>(Arrays.asList(hullPts)));
		
		// Draws a blue line outlining the convex set
		Imgproc.drawContours(origImg, hullContour, -1, new Scalar(200, 125, 75));
		/*for(int i = 0; i < hullPts.length - 1; i++) {
			Core.line(origImg, hullPts[i], hullPts[i+1], new Scalar(200, 125, 75), 2);
		}*/
		
		this.currentPos = new Point(enclosingRect.center.x, enclosingRect.center.y);
		
		// Draws a blue circle at the center of the enclosing rectangle
		Core.circle(origImg, this.currentPos, 3, new Scalar(200, 125, 75), 2);
		
		filteredHull.clear();
		
		// For each point in the smallest convex set that contains the biggest contour
		for(int i = 0; i < hullPts.length - 1; i++) {
			//If the Euclidean distance between the current convex set point and the next one is > than the width/10 of the enclosing rectangle?
			if (Math.pow(hullPts[i].x - hullPts[i + 1].x, 2) + Math.pow(hullPts[i].y - hullPts[i + 1].y, 2) > enclosingRect.size.width / 3)
            {
				// Add the convex set point to new list 
				filteredHull.add(hullPts[i]);
            }
		}
		
		// Obtains the convexity defects of the contour
		//MatOfInt temp = new MatOfInt();
		//temp.fromList(filteredHull);
		Imgproc.convexityDefects(biggestContour, hull, defects);
		defectsArray = defects.toArray();
		
		return true;
	}

	
	/**
	 * Detects and counts the number of fingers the user is holding up
	 * 
	 * @param origImg RGB-colored frame from camera feed
	 */
	private void detectAndCountFingers(Mat origImg) {
		int fingerNum = 0;
		Point[] contourPts = biggestContour.toArray();
		
		
		// For each convexity defect
		for(int i = 0; i < defectsArray.length; i+=4) {
			// Obtain the point where the defect begins
			Point startPoint = contourPts[defectsArray[i]];
			
			//Point endPoint = contourPts[defectsArray[i+1]];
			
			// Obtain the point of the deepest part of the defect
			Point depthPoint = contourPts[defectsArray[i+2]];
			//cogShapePts.add(depthPoint);
			
			// Counts the number of fingers in the image
            // Author's note: Custom heuristic based on some experiment, double check it before use
            if (/*(startPoint.y < enclosingRect.center.y || depthPoint.y < enclosingRect.center.y) && */(startPoint.y < depthPoint.y) && (Math.sqrt(Math.pow(startPoint.x - depthPoint.x, 2) + Math.pow(startPoint.y - depthPoint.y, 2)) > enclosingRect.size.height / 6.5))
            //if((startPoint.y < depthPoint.y))
            {
            	cogShapePts.add(depthPoint);
            	
            	// Increment finger count
                fingerNum++;
                
                // Draws a line from the tip of the finger to the "valley" between the two fingers
                Core.line(origImg, startPoint, depthPoint, new Scalar(0, 255, 0));
                
                // Draws a blue circle at the tip of the finger
                Core.circle(origImg, startPoint, 5, new Scalar(255, 0, 0));
                // Draws a green circle at the "valley" between the two fingers
                Core.circle(origImg, depthPoint, 5, new Scalar(255, 255, 0));
            }
		}
		
		Point center = new Point();
		float radius[] = {0};
		MatOfPoint ptMap = new MatOfPoint();
		
		// Obtains the leftmost and right most depth points used to count fingers
		Point farLeft = new Point(-1, -1), farRight = new Point(-1, -1);
		ArrayList<Point> newPts = new ArrayList<Point>();
		for(Point pt : cogShapePts) {
			if(farLeft.x == -1 || pt.x < farLeft.x)
				farLeft = pt.clone();
			if(farRight.x == -1 || pt.x > farRight.x)
				farRight = pt.clone();
		}
		
		newPts.add(farLeft);
		newPts.add(farRight);
		
		//cogShapePts = findCirclePoints(cogShapePts);
		ptMap.fromArray(newPts/*cogShapePts*/.toArray(new Point[1]));
		
		Imgproc.minEnclosingCircle(new MatOfPoint2f(ptMap.toArray()), center, radius); // Pass radius to fireGestureDetected()
		Core.circle(origImg, center, (int)radius[0], new Scalar(255, 255, 0));
		Core.circle(origImg, center, 4, new Scalar(0, 0, 255));
		
		this.currentPos = center;
		
		//HandInfo prevInfo = pastHandInfo.peek();
		//HandState prevState = prevInfo.getState();
		//Point prevPos = prevInfo.getPosition();
		//double dist = Math.sqrt(Math.pow(this.currentPos.x - prevPos.x, 2) + Math.pow(this.currentPos.y - prevPos.y, 2));
		
		// If 5, 4, or 3 fingers are detected, the hand is open
		if(fingerNum >= 3 && fingerNum <= 5) {
			/*if((prevState == HandState.OPEN || prevState == HandState.MOVING_OPEN)
					&& dist >= movementThreshold)
				this.currentState = HandState.MOVING_OPEN;
			else
				this.currentState = HandState.OPEN;*/
			this.fireGestureDetected(GestureEvent.OPENED_HAND_DETECTED, new Double(this.currentPos.x).intValue(), new Double(this.currentPos.y).intValue());
		}
		// If 2, 1, or no fingers are detected, the hand is closed
		else if(fingerNum >= 0  && fingerNum <= 2) {
			/*if((prevState == HandState.CLOSED || prevState == HandState.MOVING_CLOSED)
					&& dist >= movementThreshold)
				this.currentState = HandState.MOVING_CLOSED;
			else
				this.currentState = HandState.CLOSED;*/
			this.fireGestureDetected(GestureEvent.CLOSED_HAND_DETECTED, new Double(this.currentPos.x).intValue(), new Double(this.currentPos.y).intValue());
		}
		//else
			//this.currentState = HandState.OTHER;
		
		// Draws the finger count on the screen
		//Core.putText(origImg, recentStates.peek().toString(), new Point(15, 75), Core.FONT_HERSHEY_TRIPLEX, 2, new Scalar(255, 255, 255));
	}
	
	
	public Scalar getYCrCbMin() {
		return this.ycrcbMin;
	}
	
	public Scalar getYCrCbMax() {
		return this.ycrcbMax;
	}
	
	public void setYCrCbMin(Scalar ycrcbMin) {
		this.ycrcbMin = ycrcbMin;
	}
	
	public void setYCrCbMax(Scalar ycrcbMax) {
		this.ycrcbMax = ycrcbMax;
	}
	
	/*
	public void identifyGesture(SizedStack<HandInfo> pastHandInfo) {
		HandInfo recentHI = pastHandInfo.peek(); // The most recent HandInfo
		HandInfo prevHI = pastHandInfo.get(1); // The previous, 2nd-most recent HandInfo
		
		/**
		 * Problems:
		 * - How to distinguish between click and grab?
		 * 
		 *
		
		switch(prevHI.getState()) {
			case OPEN:
				switch(recentHI.getState()) {
					case CLOSED:
						this.currentGesture = GestureType.CLICK;
						break;
					case MOVING_OPEN:
						// Move mouse case
						break;
					default:
						break;
				}
				
				break;
			case CLOSED:
				switch(recentHI.getState()) {
					case OPEN:
						/*if(pastHandInfo.get(2).getState() == HandState.OPEN)
							
						else*
							this.currentGesture = GestureType.DROP;
						break;
					case CLOSED:
						this.currentGesture = GestureType.GRAB;
						break;
					case MOVING_CLOSED:
						this.currentGesture = GestureType.DRAG;
						break;
					default:
						break;
				}
				
				break;
			case MOVING_OPEN:
				switch(recentHI.getState()) {
					case MOVING_OPEN:
						// Still move mouse case
						break;
					/*case MOVING_CLOSED:
						this.currentGesture = GestureType.DRAG;
						break;*
					default:
						break;
				}
			
				break;
			case MOVING_CLOSED:
				switch(recentHI.getState()) {
					case MOVING_OPEN:
						this.currentGesture = GestureType.THROW;
						break;
					case MOVING_CLOSED:
						this.currentGesture = GestureType.DRAG;
						break;
					default:
						break;
				}
			
				break;
			default:
				break;
		}
	}*/
	
	
	public ArrayList<Point> findCirclePoints(ArrayList<Point> pts) {
		ArrayList<Point> lowPts = new ArrayList<Point>();
		Point lowPt = null, leftPt = null, rightPt = null;
		
		System.out.println(pts.toString());
		
		for(Point pt : pts) {
			if(lowPt == null || pt.y > lowPt.y)
				lowPt = pt.clone();
			if(leftPt == null || pt.x < leftPt.x)
				leftPt = pt.clone();
			if(rightPt == null || pt.x > rightPt.x)
				rightPt = pt.clone();
		}
		
		lowPts.add(lowPt);
		lowPts.add(leftPt);
		lowPts.add(rightPt);
		
		System.out.println(lowPts.toString());
		
		return lowPts;
	}
	
	
	public ArrayList<Point> findLowestPoints(ArrayList<Point> pts) {
		ArrayList<Point> lowPts = new ArrayList<Point>();
		Point lowPt1 = null;//, lowPt2 = null;
		
		//System.out.println(pts.toString());
		
		for(Point pt : pts) {
			if(lowPt1 == null || pt.y > lowPt1.y)
				lowPt1 = pt.clone();
			//else if(lowPt2 == null || pt.y > lowPt2.y)
				//lowPt2 = pt.clone();
		}
		
		lowPts.add(lowPt1);
		//lowPts.add(lowPt2);
		
		//System.out.println(lowPts.toString());
		
		return lowPts;
	}
	
	
	/**
	 * Obtains the current String value of the hand state enumerator, hState
	 * 
	 * @return String value of hState
	 *
	public String getCurrentHandStateValue() {
		String output = "";
		
		if(!pastHandInfo.isEmpty())
			output = pastHandInfo.peek().toString();
		else
			output = "Stack is empty";
		
		return output;
	}//*/
	
	
	/**
	 * Obtains a String listing all the state currently in the HandState FixedStack, recentStates
	 * 
	 * @return String list of HandStates in recentStates
	 *
	public String getRecentHandStatesList() {
		String output = "";
		
		if(!pastHandInfo.isEmpty())
			output = pastHandInfo.toString();
		else
			output = "Stack is empty";
		
		return output;
	}/*/
}