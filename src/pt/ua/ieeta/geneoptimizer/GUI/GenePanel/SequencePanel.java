package pt.ua.ieeta.geneoptimizer.GUI.GenePanel;

import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import pt.ua.ieeta.geneoptimizer.Main.ApplicationSettings;

/**
 *
 * @author Paulo Gaspar
 */
public abstract class SequencePanel extends JPanel
{
    
    protected int numberOfPadding = 0;
    protected int sequenceSize = 0;
    
    public int getSequenceWidthInChars()
    {
        return this.sequenceSize; // + numberOfPadding;
    }
    
    public void setPaddingLabels(int finalSize)
    {
        assert finalSize >= 0;

        int currentPanelCharWidth = getSequenceWidthInChars() + numberOfPadding;

        /* Final size is smaller than my size? Reduce size... */
        if (finalSize <= currentPanelCharWidth)
        {
            while (finalSize < currentPanelCharWidth)
            {
                this.remove(this.getComponentCount()-1);
                currentPanelCharWidth -= 3;
            }
        }
        else
        {
            int sequenceLabelWidthPixel = (Integer) ApplicationSettings.getProperty("sequenceLabelWidthPixel", Integer.class);
            for (int i=0; i < (finalSize-currentPanelCharWidth)/3; i++)
            {
                JLabel label = new JLabel(" ", SwingConstants.CENTER);
                label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
                Dimension d = new Dimension(sequenceLabelWidthPixel, label.getPreferredSize().height);
                label.setPreferredSize(d);
                label.setMinimumSize(d);
                label.setMaximumSize(d);
                this.add(label);
            }
        }
        
        this.numberOfPadding = (finalSize-getSequenceWidthInChars());
    }
}
