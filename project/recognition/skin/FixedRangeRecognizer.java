package project.recognition.skin;

import java.util.HashMap;
import java.util.Map;

import org.opencv.core.Mat;

public class FixedRangeRecognizer implements SkinRecognizer {

	private static final int CrMiddle = 158;	// 185-131=54
												//originals=131//144//105//155
	private static final int CbMiddle = 107;	// 135-80=55
												//originals=80//123//122//120
	
	private static final int tolerence = 27;
	
	private static final Map<String,Integer> colorMap =
			new HashMap<String,Integer>(8);
	
	static {
		colorMap.put(CrMIN, CrMiddle - tolerence);
		colorMap.put(CrMAX, CrMiddle + tolerence);
		colorMap.put(CbMIN, CbMiddle - tolerence);
		colorMap.put(CbMAX, CbMiddle + tolerence);
	}
	
	
	@Override
	public Map<String, Integer> detectSkin(Mat mat) {
		return colorMap;
	}

}
