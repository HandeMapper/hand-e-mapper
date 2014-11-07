/**
 * 
 */
package handemapper.gui.tray;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import handemapper.common.recognition.Gesture;
import handemapper.common.recognition.GestureRecognizer;
import handemapper.common.recognition.event.GestureEvent;
import handemapper.common.recognition.event.GestureListener;
import handemapper.gui.panels.LogPanel;
import handemapper.gui.panels.VideoCaptureMirrorPanel;
import handemapper.gui.translation.HandGestureRobot;

//import project.MainEntry;
//import project.gui.common.VideoCaptureMirrorPanel;
//import project.recognition.types.HaarClassifierGesture;
//import project.util.HandGestureRobot;


/**
 * 
 * 
 * @author Chris Hartley
 * @author Adin Miller
 */
public class GestureTrayApp implements Runnable {
	
	/** Static log4j logger instance for this class. */
	private static final Logger logger = LogManager.getLogger(GestureTrayApp.class);
	

	private static final String ACTION_CMD_EXIT =
			"app-popup-menu-exit";
	
	private static final String ACTION_CMD_ALLOW_MOUSE_OVERRIDE =
			"app-popup-menu-allowMouseOverride";
	
	private static final String ACTION_CMD_VIDEO_CAPTURE_PREVIEW =
			"app-popup-menu-vcPreview";
	
	private static final String ACTION_CMD_REINITIALIZE =
			"app-popup-menu-reinitialize";
	
	//private static final String ACTION_CMD_CLASSIFIERS_DETAILS =
	//		"app-popup-menu-cdFrame";
	
	private static final String ACTION_CMD_VIDEO_CAPTURE_STATUS =
			"app-popup-menu-vcStatus";
	
	private static final String ACTION_CMD_VIEW_LOGS =
			"app-popup-menu-logFrame";
	
	private final String iconFolder = "/icons/";
	
	
	// Private final member data.
	private final SystemTray tray = SystemTray.getSystemTray();
	private final Toolkit toolkit = Toolkit.getDefaultToolkit();
	private final TrayIcon trayIcon;
	private final String title;
	
	// Private member data.
	private GestureRecognizer gr = null;
	private VideoCaptureMirrorPanel vcMirror;
	private VideoCapturePreviewFrame vcPreview = null;
	//private JFrame cdFrame = null;
	private JFrame logFrame = null;
	private List<BufferedImage> icons;
	
	
	/**
	 * Constructor for a new instance of this gesture tray application with the
	 * specified program title.
	 * 
	 * @param title  the {@link String} title name of this application.
	 * 
	 * @throws UnsupportedOperationException
	 * @throws HeadlessException
	 * @throws SecurityException
	 * @throws AWTException
	 */
	public GestureTrayApp(String title) throws UnsupportedOperationException,
	                                           HeadlessException,
	                                           SecurityException,
	                                           AWTException
	{
		this.title = title;
		
		// Build the pop-up menu for the tray application...
		final JPopupMenu jmenu = new GestureTrayPopupMenu();
		
		// Load the image icon for the tray icon...
		loadIcons();
		Image image = getTrayIcon();
		
		// Load this application as a tray icon into the system tray...
		trayIcon = new TrayIcon(image, title, null);
		trayIcon.addMouseListener( new MouseAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent me) {
				if (me != null && me.getClickCount() > 1)
					GestureTrayApp.this.showVideoCapturePreviewFrame();
			}
			
			@Override
			public void mouseReleased(MouseEvent me) {
				if (me.isPopupTrigger()) {
					jmenu.setLocation(me.getX(), me.getY());
					jmenu.setInvoker(jmenu);
					jmenu.setVisible(true);
					jmenu.requestFocus(true);
				}
			}
			
		} );
		tray.add(trayIcon);
	}


	@Override
	public void run() {
		trayIcon.displayMessage(title,
				"Gesture Recognizer loaded and running...",
                TrayIcon.MessageType.INFO);
		
		HandGestureRobot robot = new HandGestureRobot();
		logger.debug("Loaded hand gesture robot: " + robot);
		
		gr = new handemapper.recognition.GestureRecognizerImpl();
		vcMirror = new VideoCaptureMirrorPanel(gr.getVideoCaptureImageIcon());
		
		String clsRoot = "/classifiers/";// /Gesture Detection/classifiers
		
		Gesture ag = new handemapper.recognition.types.HandRecognizer("Hand Convexity/Contour Detection");
		ag.addGestureListener(robot);
		ag.addGestureListener(vcPreview);
		gr.register(ag);
		
		handemapper.recognition.types.HaarClassifierGesture hc = 
				new handemapper.recognition.types.HaarClassifierGesture("Frontal Face", "Face detection to capture skin tone from.", false);
		hc.setClassifier(clsRoot + "haarcascade_frontalface_default.xml");
		hc.setHighLightColor(Color.RED);
		gr.register(hc);
	}
	
	
	/**
	 * 
	 */
	public final void close() {
		if (gr != null)
			gr.stop();
		
		if (vcPreview != null) {
			vcPreview.setVisible(false);
			vcPreview.dispose();
		}
		
		tray.remove(trayIcon);
		System.exit(java.awt.Frame.NORMAL);
	}
	
	
	/**
	 * 
	 */
	private final void loadIcons() {
		icons = new LinkedList<BufferedImage>();
		int[] sizes = { 16, 24, 36, 48 };
		
		for (int size : sizes) {
			try {
				String imgPath = iconFolder + "trayIcon_"
						+ size + "x" + size + ".png";
				icons.add( ImageIO.read( getClass().getResourceAsStream(imgPath) ) );
			} catch (Exception ignore) { }
		}
	}
	
	
	/**
	 * 
	 * @return
	 */
	private final Image getTrayIcon() {
		Dimension iconDim = tray.getTrayIconSize();
		String imgPath = iconFolder + "trayIcon_16x16.png";
		Image image = null;
		
		int base = Math.min(iconDim.height, iconDim.width);
		if (base > 42)
			imgPath.replace("16", "48");
		else if (base > 31)
			imgPath.replace("16", "36");
		else if (base > 20)
			imgPath.replace("16", "24");

		try {
			image = ImageIO.read( getClass().getResourceAsStream(imgPath) );
		}
		catch (IllegalArgumentException | IOException ex) {
			logger.error("Unable to load tray icon!");
			ex.printStackTrace();
		}
		return image;
	}
	
	
	/**
	 * 
	 */
	private final synchronized void showVideoCapturePreviewFrame() {
		if (vcPreview == null)
			vcPreview = new VideoCapturePreviewFrame();
		
		vcPreview.setVisible(true);
	}
	
	
	/**
	 * 
	 * @author Chris Hartley
	 *
	 */
	private final class VideoCapturePreviewFrame extends JFrame implements GestureListener {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 6881309079375182591L;

		
		// Member data.
		private Timer refreshTimer = null;
		private final JLabel leftLbl = new JLabel("");
		private final JLabel rightLbl = new JLabel("FPS=?");
		
		
		/**
		 * Constructor for a new instance of the video capture preview frame.
		 */
		public VideoCapturePreviewFrame() {
			super(title + " - Preview");
			setIconImages(icons);
			setAlwaysOnTop(true);
			setVisible(true);
			setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			build();
			pack();
		}
		
		
		/**
		 * 
		 */
		private final void build() {
			JPanel main = new JPanel( new BorderLayout() );
			main.add(vcMirror, BorderLayout.CENTER);
			
			JPanel statusBar = new JPanel();
			BoxLayout box = new BoxLayout(statusBar, BoxLayout.X_AXIS);
			statusBar.setLayout(box);
			statusBar.add(leftLbl);
			statusBar.add(Box.createHorizontalGlue());
			statusBar.add(rightLbl);
			rightLbl.setHorizontalAlignment(JLabel.RIGHT);
			main.add(statusBar, BorderLayout.SOUTH);
			
			setContentPane(main);
		}
		
		
		@Override
		public void setVisible(boolean visible) {
			super.setVisible(visible);
			
			if (visible) {
				refreshTimer = new Timer("vcRefreshTimer");
				refreshTimer.scheduleAtFixedRate(getTask(), 1000, 1000);
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
	
	
	/**
	 * Request the in use gesture recognition object to re-initialize.
	 */
	private final synchronized void reinitialize() {
		final Gesture[] gestures = gr.getGestures();
		
		for (int i = 0; i < gestures.length; i++)
			if (gestures[i] != null && gestures[i].isEnabled())
				gestures[i].initialize();
	}
	
	/*private final synchronized void showClassifierDetailsFrame() {
		if (cdFrame != null) {
			cdFrame.setVisible(true);
		}
		else {
			cdFrame = new JFrame(title + " - Classifier(s)");
			cdFrame.setIconImages(icons);
			cdFrame.setContentPane( new ClassifierDetailsPanel(gr) );
			cdFrame.setAlwaysOnTop(true);
			cdFrame.setVisible(true);
			cdFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			cdFrame.pack();
			cdFrame.setResizable(true);
			cdFrame.setLocationRelativeTo(null);
			cdFrame.setVisible(true);
		}
	}*/
	
	
	/**
	 * 
	 */
	private final synchronized void showVideoCaptureStatusFrame() {
		//TODO: fill in code to show frame of video capture status...
	}
	
	
	/**
	 * 
	 */
	private final synchronized void showLogFrame() {
		if (logFrame != null) {
			logFrame.setVisible(true);
		}
		else {
			logFrame = new JFrame(title + " - Logs");
			logFrame.setIconImages(icons);
			logFrame.setContentPane( new LogPanel() );
			logFrame.setAlwaysOnTop(true);
			logFrame.setVisible(true);
			logFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			logFrame.pack();
			logFrame.setResizable(true);
			
			Dimension screenDim = toolkit.getScreenSize();
			Insets screenInsets = toolkit.getScreenInsets(
					logFrame.getGraphicsConfiguration());
			
			logFrame.setLocation(
					screenDim.width - screenInsets.right - logFrame.getWidth(), 
					screenDim.height - screenInsets.bottom - logFrame.getHeight() );
			logFrame.setVisible(true);
		}
		logger.debug("Showing log frame...");
	}
	

	
	/**
	 * 
	 * @author Chris Hartley
	 */
	private final class GestureTrayPopupMenu extends JPopupMenu
			implements ActionListener, MouseListener
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = -9158476153662188001L;
		
		
		/**
		 * Maintains a hash of all menu items registered into this pop-up menu.
		 */
		public final HashMap<String,JMenuItem> menuItems =
				new HashMap<String,JMenuItem>();
		
		
		/**
		 * Constructor for a new instance of the tray pop-up menu.
		 */
		public GestureTrayPopupMenu() {
			super();
			
			buildPopupMenu();
			
			setFocusable(true);
			addMouseListener(this);
		}
		
		
		/**
		 * 
		 * @param cmd
		 * @param text
		 * @return
		 */
		private final JMenuItem registerPopupMenuItem(String cmd, String text) {
			return registerPopupMenuItem(cmd, new JMenuItem(text));
		}
		
		
		/**
		 * 
		 * @param cmd
		 * @param item
		 * @return
		 */
		private final <T extends JMenuItem> T registerPopupMenuItem(String cmd, T item) {
			item.setActionCommand(cmd);
			item.addActionListener(this);
			menuItems.put(cmd, item);
			return item;
		}
		
		
		/**
		 * 
		 */
		private final void buildPopupMenu() {
			add( registerPopupMenuItem(ACTION_CMD_VIDEO_CAPTURE_PREVIEW,
					"Video Capture preview...") ).setFont(
					getFont().deriveFont(Font.BOLD) );
			
			addSeparator();
			
			add( registerPopupMenuItem(ACTION_CMD_REINITIALIZE,
					"Re-initialize...") );
			
			addSeparator();
			
			add( registerPopupMenuItem(ACTION_CMD_ALLOW_MOUSE_OVERRIDE,
					new JCheckBoxMenuItem("Allow gestures to control mouse")) )
					.setEnabled(false);
			
			addSeparator();
			
			add( registerPopupMenuItem(ACTION_CMD_VIEW_LOGS,
					"Show/hide logs...") );
			
			//add( registerPopupMenuItem(ACTION_CMD_CLASSIFIERS_DETAILS,
			//		"View Classifiers...") );
			
			add( registerPopupMenuItem(ACTION_CMD_VIDEO_CAPTURE_STATUS,
					"Video Capture status...") )
					.setEnabled(false);
			
			addSeparator();
			
			add( registerPopupMenuItem(ACTION_CMD_EXIT, "Exit") );
		}
		
		
		@Override
		public void actionPerformed(ActionEvent ae) {
			String cmd = ae.getActionCommand();

			if (ACTION_CMD_EXIT.equals(cmd))
				close();
			else if (ACTION_CMD_VIDEO_CAPTURE_PREVIEW.equals(cmd))
				showVideoCapturePreviewFrame();
			else if (ACTION_CMD_REINITIALIZE.equals(cmd))
				reinitialize();
			//else if (ACTION_CMD_CLASSIFIERS_DETAILS.equals(cmd))
			//	showClassifierDetailsFrame();
			else if (ACTION_CMD_VIDEO_CAPTURE_STATUS.equals(cmd))
				showVideoCaptureStatusFrame();
			else if (ACTION_CMD_VIEW_LOGS.equals(cmd))
				showLogFrame();
			else
				logger.warn("Unknown action preformed: " + ae);
		}


		@Override
		public void mouseClicked(MouseEvent me) { }


		@Override
		public void mouseEntered(MouseEvent me) { }


		@Override
		public void mouseExited(MouseEvent me) {
			if (!contains(me.getPoint()))
				setVisible(false);
		}


		@Override
		public void mousePressed(MouseEvent me) { }


		@Override
		public void mouseReleased(MouseEvent me) { }
		
	}
	
}
