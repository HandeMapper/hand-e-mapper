/**
 * 
 */
package handemapper.gui.frames;

import handemapper.gui.GestureApplication;
import handemapper.gui.panels.LogPanel;

import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JFrame;

/**
 * @author Chris
 *
 */
public class LogFrame extends JFrame {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8976856788968845743L;

	
	/**
	 * 
	 * @param app
	 */
	public LogFrame(GestureApplication app) {
		super(app.getTitle() + " - Logs");
		setIconImages(app.getIcons());
		setContentPane( new LogPanel() );
		setAlwaysOnTop(true);
		setVisible(true);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		pack();
		setResizable(true);
		
		Dimension screenDim = app.getToolkit().getScreenSize();
		Insets screenInsets = app.getToolkit().getScreenInsets(
				getGraphicsConfiguration());
		
		setLocation(
				screenDim.width - screenInsets.right - getWidth(), 
				screenDim.height - screenInsets.bottom - getHeight() );
	}
}
