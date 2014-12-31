/**
 * 
 */
package handemapper.gui.tray;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import handemapper.common.recognition.Gesture;
import handemapper.common.recognition.GestureRecognizer;
import handemapper.gui.GestureApplication;
import handemapper.gui.frames.LogFrame;
import handemapper.gui.frames.VideoCapturePreviewFrame;
import handemapper.gui.translation.HandGestureRobot;
import handemapper.gui.util.Gestures;


/**
 * Provides the system tray application for the gesture detection graphical 
 * user interface (GUI). This class implements the {@link Runnable} interface 
 * to register the configured gestures to process.
 * 
 * @author Chris Hartley
 * @author Adin Miller
 */
public class GestureTrayApp extends GestureApplication {

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
	
	/**
	 * The current system's system tray.
	 * @see SystemTray#getSystemTray()
	 */
	private static final SystemTray tray = SystemTray.getSystemTray();
	
	// Private final member data.
	private final TrayIcon trayIcon;
	
	// Private member data.
	private JFrame logFrame = null;
	private GestureRecognizer gr = null;
	private HandGestureRobot robot = null;
	private VideoCapturePreviewFrame vcPreview = null;
	
	
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
		super(title);
		
		// Build the pop-up menu for the tray application...
		final JPopupMenu jmenu = new GestureTrayPopupMenu();
		
		// Load the image icon for the tray icon...
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
		trayIcon.displayMessage(getTitle(),
				"Gesture Recognizer initializing...",
                TrayIcon.MessageType.INFO);
		
		robot = new HandGestureRobot();
		logger.debug("Loaded hand gesture robot: " + robot);
		
		gr = new handemapper.recognition.GestureRecognizerImpl();
//		vcMirror = new VideoCaptureMirrorPanel(gr.getVideoCaptureImageIcon());
		
		/* TODO incorporate the gestures.xml resource to dynamically load the
		 *      configured gestures to use in this application.
		 */
		for (Gesture g : Gestures.load()) {// returns List<Gesture>
			g.addGestureListener(robot);
			g.addGestureListener(vcPreview);
			gr.register(g);
		}
		
		trayIcon.displayMessage(getTitle(),
				"Gesture Recognizer loaded and running!",
                TrayIcon.MessageType.INFO);
	}
	
	
	@Override
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
	 * @return
	 */
	private final Image getTrayIcon() {
		Dimension iconDim = tray.getTrayIconSize();
		int size = Math.min(iconDim.height, iconDim.width);
		
		return getIcon(size);
	}
	
	
	/**
	 * 
	 */
	private final synchronized void showVideoCapturePreviewFrame() {
		if (vcPreview == null) {
			vcPreview = new VideoCapturePreviewFrame(this, gr);
		}
		vcPreview.setVisible(true);
		logger.debug("Showing video capture frame...");
	}
	
	
	/**
	 * Request the enabled gesture recognition object(s) to re-initialize.
	 * @see Gesture#initialize()
	 * @see Gesture#isEnabled()
	 */
	private final synchronized void reinitialize() {
		final Gesture[] gestures = gr.getGestures();
		
		for (int i = 0; i < gestures.length; i++)
			if (gestures[i] != null && gestures[i].isEnabled())
				gestures[i].initialize();
	}
	
	
	/**
	 * 
	 */
	private final synchronized void showVideoCaptureStatusFrame() {
		//TODO: fill in code to show frame of video capture status...
	}
	
	
	/**
	 * Shows the LogFrame
	 */
	private final synchronized void showLogFrame() {
		if (logFrame == null) {
			logFrame = new LogFrame(this);
		}
		logFrame.setVisible(true);
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
					new JCheckBoxMenuItem("Allow gestures to control mouse", false)) )
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
