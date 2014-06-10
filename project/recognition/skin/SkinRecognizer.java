package project.recognition.skin;

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
	
	public Map<String, Integer> detectSkin(Mat mat);
}
