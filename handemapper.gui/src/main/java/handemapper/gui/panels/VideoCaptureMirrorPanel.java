/**
 * 
 */
package handemapper.gui.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JPanel;


/**
 * 
 * @author Chris Hartley
 * @author Adin Miller
 */
public class VideoCaptureMirrorPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5011685128270293027L;

	
	// Private member data.
	private Object updateLock = new Object();
	private BufferedImage vcOffline = null;
	private Image vcFrame = null;
	
	
	/**
	 * Constructor for a new instance of this video mirror panel.
	 */
	public VideoCaptureMirrorPanel() {
		super(null);
		
		setBackground(Color.black);
		setMinimumSize( new Dimension(160, 120) );
		setMaximumSize( new Dimension(640, 480) );
		setPreferredSize( new Dimension(320, 240) );
		setBounds(0, 0, 320, 240);
		setBorder( BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2),
				BorderFactory.createLineBorder(Color.BLACK, 1) ));
		
		try {
			vcOffline = ImageIO.read(
					getClass().getResourceAsStream("/images/offline.png") );
		}
		catch (IllegalArgumentException | IOException ex) {
			ex.printStackTrace();
		}
	}
	
	
	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		final FontMetrics fm = g2d.getFontMetrics();
		final Insets ins = getInsets();
		int x = ins.left;
		int y = ins.top;
		final int w = getWidth() - x - ins.right;
		final int h = getHeight() - y - ins.bottom;
		
		synchronized (updateLock) {
			if (vcFrame != null) {
				g2d.drawImage(vcFrame, x, y, w, h, this);
				g2d.setColor(getForeground());
				
				x += fm.getHeight();
				y += fm.getHeight();
				g2d.drawString(vcFrame + "", x, y);
				
				y += fm.getHeight();
				g2d.drawString(vcFrame.getWidth(null) + "x" + vcFrame.getHeight(null), x, y);
			}
			else {
				g2d.drawImage(vcOffline, x, y, w, h, this);
			}
		}
	}

	
	/**
	 * Updates the image of the most recent video capture frame and repaints
	 * this panel.
	 * @param img
	 */
	public void updateImage(BufferedImage img) {
		synchronized (updateLock) {
			vcFrame = img;
		}
		
		repaint();
	}
	
}
