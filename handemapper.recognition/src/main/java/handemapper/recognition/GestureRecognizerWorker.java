/**
 * 
 */
package handemapper.recognition;

import handemapper.common.recognition.Gesture;
import handemapper.recognition.utils.Updater;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.SwingWorker;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;


/**
 * 
 * @author Chris Hartley
 *
 * @see javax.swing.SwingWorker<A,B>
 */
public class GestureRecognizerWorker extends SwingWorker<Void, Long> {

	/** Static log4j logger instance for this class. */
	private static final Logger logger = LogManager.getLogger(GestureRecognizerWorker.class);
	
	
	/**
	 * The publish format for the image buffer.
	 */
	private static final String publishImageFormat = ".jpg";
	
	
	/**
	 * The maximum number of re-connect attempts before error'ing out.
	 */
	private static final int maxReconnectAttempts = 5;
	
	
	/**
	 * The delay in milliseconds between re-connect attempts on the video
	 * device.
	 */
	private static final int delayInMillis = 2000;
	
	
	// Private member fields.
	private final int frameAvg = 8;
	private final Updater<BufferedImage> imgUpdater;
	private final Map<String,Gesture> gestures;
	private final MatOfByte matrixBuffer = new MatOfByte();
	private final AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
	
	// Private member data.
	private double fps = 0d;
	private long fpsLimit = 30;
	private long fpsLimitTime = 1000 / fpsLimit;
	private VideoCapture camera;
	private AffineTransformOp op = null;

	
	/**
	 * Constructor for a new instance with the specified video capture device,
	 * image holder to publish the received frames, and the map containing the
	 * registered gestures that are currently loaded.
	 * 
	 * @param camera
	 * @param imgIcon
	 * @param gestures2
	 */
	public GestureRecognizerWorker(Updater<BufferedImage> imgUpdater,
			VideoCapture camera, Map<String,Gesture> gestures)
	{
		super();
		
		this.camera = camera;
		this.imgUpdater = imgUpdater;
		this.gestures = gestures;
		
		logger.debug("Initializing " + getClass().getSimpleName());
	}
	
	
	/**
	 * Verifies that the camera device is available and retrieve the next frame
	 * from the video camera to store into the specified {@link Mat} buffer.
	 * 
	 * @param buffer	The buffer to store the next frame to.
	 * @return			{@code true} if the device is available and successfully
	 * 					retrieved an image for the next frame; otherwise,
	 * 					returns {@code false}.
	 */
	private final synchronized boolean readNextVideoFrame(Mat buffer) {
		int reconnectAttempt = 1;
		
		if (camera == null) {
			logger.error("No camera device connected.");
			return false;
		}
		
		// To retrieve various properties of the camera device.
		//camera.get(propId);
		if (!camera.isOpened())
			logger.warn("Camera device is NOT opened at this time.");
		
		while (!camera.read(buffer) || buffer.empty()) {
			if (reconnectAttempt > maxReconnectAttempts) {
				logger.error("After " + reconnectAttempt + " attempts to "
						+ "re-connect to the device, it was unable to retireve "
						+ "an image. Please verify the web camera is connected "
						+ "and functioning properly.");
				return false;
			}
			
			logger.warn("Couldn't retrive image from video. "
					+ "Re-attempting in " + (delayInMillis / 1000.0)
					+ " seconds...");
			
			try {
				Thread.sleep(delayInMillis);
			} catch (Exception ignore) { }
			
			reconnectAttempt++;
			if (!camera.isOpened())
				camera.open(0);
		}
		return true;
	}
	
	
	/**
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Void doInBackground() throws Exception {
		logger.debug("Worker " + getClass().getSimpleName() + " running...");
		Mat img = new Mat();
		
		int frameCount = 0;
		long startTime = 0l, lapseTime, fpsDiff;
		
		try {
			while (!isCancelled() && readNextVideoFrame(img)) {
				lapseTime = System.currentTimeMillis();
				if (++frameCount == frameAvg) {
					publish(lapseTime - startTime);
					frameCount = 0;
					startTime = lapseTime;
				}
				synchronized(gestures) {
					for (Gesture gesture : gestures.values()) {
						if (gesture != null && gesture.isEnabled())
							gesture.detect(img);
					}
				}
				publishImage(img);
				
				lapseTime = System.currentTimeMillis() - lapseTime;
				fpsDiff = fpsLimitTime - lapseTime;
				if (fpsDiff > 0) {
					try {
						Thread.sleep(fpsDiff);
					} catch (Exception ignore) { }
				}
			}
			
			logger.debug("Worker " + getClass().getSimpleName() + " canceled!");
		}
		catch (Exception ex) {
			logger.error("Exception caught in " + getClass().getSimpleName() + ": " + ex);
			ex.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * 
	 * @param in
	 */
	private final synchronized void publishImage(Mat img) {
		InputStream in = null;
		
		Highgui.imencode(publishImageFormat, img, matrixBuffer);
		
		try {
			in = new ByteArrayInputStream(matrixBuffer.toArray());
		
			BufferedImage bufferImg = ImageIO.read(in);
			
			if (op == null) {
				tx.translate(-bufferImg.getWidth(null), 0);
				op = new AffineTransformOp(tx, 
						AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			}
			
			imgUpdater.update(op.filter(bufferImg, null));
		}
		catch(IOException ex) {
			if (!isCancelled())
				cancel(true);
		}
	}
	
	
	@Override
	protected void process(List<Long> times) {
		for (long milli : times) {
			fps = frameAvg * 1000.0 / milli;
		}
	}

	
	@Override
	protected void done() {
		logger.warn(getClass().getSimpleName() + " has completed!");

		try {
			camera.release();
			logger.debug("Video device released!");
		} catch(Exception ignore) { }
	}


	/**
	 * returns the current frames-per-second that this worker is processing at.
	 * 
	 * @return	the current frames-per-second (FPS).
	 */
	public double getFPS() {
		return fps;
	}

}
