package handemapper.recognition.skin;

import java.util.Map;

import org.opencv.core.Mat;

public interface SkinRecognizer {

	/*
	 * Added for easy of implementation. (cnh)
	 */
	public static final String CrMIN = "CrMin";
	public static final String CrMAX = "CrMax";
	public static final String CbMIN = "CbMin";
	public static final String CbMAX = "CbMax";
	
	/**
	 * Returns a map of the detected skin regions from the specified {@code Mat}
	 * parameter for this particular implementation.
	 * @param mat  the {@code Mat} containing the graphic data to analyze for
	 *             skin detection.
	 * @return
	 */
	public Map<String, Integer> detectSkin(Mat mat);
}
