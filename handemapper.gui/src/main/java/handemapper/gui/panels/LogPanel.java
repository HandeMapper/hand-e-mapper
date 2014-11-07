/**
 * 
 */
package handemapper.gui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;


/**
 * 
 * @author Chris Hartley
 *
 */
public class LogPanel extends JPanel {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -683208083097054461L;
	
	private final HashMap<Level,SimpleAttributeSet> attrs =
			new HashMap<Level,SimpleAttributeSet>(8);
	
	private StyledDocument logDoc = null;
	private JTextPane logTextPane = null;
	
	/**
	 * 
	 * @author Chris Hartley
	 *
	 */
	private class LogPanelAppender extends WriterAppender {

		protected LogPanelAppender() {
			super( new PatternLayout(), new OutputStream() {
				@Override
				public void write(int b) throws IOException { }
			});
		}

		@Override
		public void append(LoggingEvent event) {
			try {
				logDoc.insertString(logDoc.getLength(), event.getMessage() + "\n", attrs.get(event.getLevel()));
				logTextPane.setCaretPosition(logDoc.getLength());
			} catch (BadLocationException ignore) { }
			
			LogPanel.this.repaint();
		}
	};
	
	
	/**
	 * Constructor for a new instance of this log panel.
	 */
	public LogPanel() {
		super( new BorderLayout() );
		
		buildPanel();
		
		// register this as a listener to the Log.
		LogManager.getRootLogger().addAppender( new LogPanelAppender() );
	}
	
	
	/**
	 * 
	 */
	private final void buildPanel() {
		setMinimumSize( new Dimension(250, 400) );
		setMaximumSize( new Dimension(0, 0) );
		setPreferredSize( new Dimension(250, 400) );
		
		logTextPane = new JTextPane();
		logTextPane.setBackground(Color.BLACK);
		logTextPane.setForeground(Color.WHITE);
		logTextPane.setMargin( new Insets(5, 5, 5, 5) );
		logTextPane.setEditable(false);
		add(new JScrollPane(logTextPane), BorderLayout.CENTER);
		
		configureStyles(logTextPane.getStyledDocument());
	}


	/**
	 * 
	 * @param doc
	 */
	private void configureStyles(StyledDocument doc) {
		logDoc = doc;
		
		SimpleAttributeSet baseAttr = new SimpleAttributeSet();
        StyleConstants.setFontFamily(baseAttr, "Monospaced");
        StyleConstants.setFontSize(baseAttr, 12);
        attrs.put(Level.INFO, baseAttr);
        
        SimpleAttributeSet attr = new SimpleAttributeSet(baseAttr);
        StyleConstants.setItalic(attr, true);
        StyleConstants.setForeground(attr, Color.orange.darker());
        attrs.put(Level.WARN, attr);
        
        attr = new SimpleAttributeSet(baseAttr);
        StyleConstants.setForeground(attr, Color.cyan.darker());
        attrs.put(Level.DEBUG, attr);
        
        attr = new SimpleAttributeSet(baseAttr);
        StyleConstants.setForeground(attr, Color.gray);
        attrs.put(Level.TRACE, attr);
        
        attr = new SimpleAttributeSet(baseAttr);
        StyleConstants.setBold(attr, true);
        StyleConstants.setForeground(attr, Color.red);
        attrs.put(Level.ERROR, attr);
        
        attr = new SimpleAttributeSet(baseAttr);
        StyleConstants.setBold(attr, true);
        StyleConstants.setForeground(attr, Color.red.darker());
        attrs.put(Level.FATAL, attr);
	}
	
}
