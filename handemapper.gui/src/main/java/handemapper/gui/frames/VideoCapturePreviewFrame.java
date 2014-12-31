/**
 * 
 */
package handemapper.gui.frames;

import java.awt.BorderLayout;
import java.util.Timer;
import java.util.TimerTask;

import handemapper.common.recognition.GestureRecognizer;
import handemapper.common.recognition.event.GestureEvent;
import handemapper.common.recognition.event.GestureListener;
import handemapper.gui.GestureApplication;
import handemapper.gui.panels.VideoCaptureMirrorPanel;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author Chris
 *
 */
public class VideoCapturePreviewFrame extends JFrame implements GestureListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2460799843280056225L;

	
	// Member data.
	private final JLabel leftLbl = new JLabel("");
	private final JLabel rightLbl = new JLabel("FPS=?");
	
	private int refreshRequestFPS = 1000;
	private Timer refreshTimer = null;
	private GestureRecognizer gr = null;
	private VideoCaptureMirrorPanel vcMirror = null;

	
	/**
	 * Constructor for a new instance of the video capture preview frame.
	 */
	public VideoCapturePreviewFrame(GestureApplication app, GestureRecognizer gr) {
		super(app.getTitle() + " - Preview");
		setIconImages(app.getIcons());
		setAlwaysOnTop(true);
		setVisible(true);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		
		this.gr = gr;
		
		build();
		pack();
	}
	
	
	/**
	 * 
	 */
	private final void build() {
		JPanel main = new JPanel( new BorderLayout() );
		//TODO JLayeredPane panes = new JLayeredPane();
		
		vcMirror = new VideoCaptureMirrorPanel(gr.getVideoCaptureImageIcon());
		main.add(vcMirror, BorderLayout.CENTER);
		//panes.setLayer(vcMirror, JLayeredPane.FRAME_CONTENT_LAYER);
		
		JPanel statusBar = new JPanel();
		BoxLayout box = new BoxLayout(statusBar, BoxLayout.X_AXIS);
		statusBar.setLayout(box);
		statusBar.add(leftLbl);
		statusBar.add(Box.createHorizontalGlue());
		statusBar.add(rightLbl);
		rightLbl.setHorizontalAlignment(JLabel.RIGHT);
		main.add(statusBar, BorderLayout.SOUTH);
		//panes.setLayer(vcMirror, JLayeredPane.PALETTE_LAYER);
		
		setContentPane(main);
		//setContentPane(panes);
	}
	
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		
		if (visible) {
			refreshTimer = new Timer("vcRefreshTimer");
			refreshTimer.scheduleAtFixedRate(getTask(), refreshRequestFPS, refreshRequestFPS);
		}
		else if (refreshTimer != null){
			refreshTimer.cancel();
		}
	}
	
	
	/**
	 * 
	 * @return
	 */
	private final TimerTask getTask() {
		return new TimerTask() {

			private final String frmt = "FPS=%.02f";
			
			@Override
			public void run() {
				rightLbl.setText(String.format(frmt, gr.getFPS()));
			}
			
		};
	}
	
	
	/**
	 * 
	 * @param obj
	 */
	public final void setStatus(Object obj) {
		leftLbl.setText(obj != null ? obj.toString() : "");
	}

	
	/* (non-Javadoc)
	 * @see handemapper.common.recognition.event.GestureListener#gestureDetected(handemapper.common.recognition.event.GestureEvent)
	 */
	@Override
	public void gestureDetected(GestureEvent ge) {
		switch (ge.getID()) {
		case GestureEvent.CLOSED_HAND_DETECTED:
			setStatus("Closed Hand");
			break;
		case GestureEvent.OPENED_HAND_DETECTED:
			setStatus("Open Hand");
			break;
		default:
			setStatus("nothing detected");
		}
	}

}
