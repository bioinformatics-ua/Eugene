
package pt.ua.ieeta.geneoptimizer.GUI.GenePanel;

import java.awt.Dimension;
import java.awt.Font;
import java.util.Vector;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import pt.ua.ieeta.geneoptimizer.Main.ApplicationSettings;

/**
 *
 * @author Paulo Gaspar
 */
public class NumberingSequencePanel extends SequencePanel // implements MouseListener, MouseMotionListener
{
    /* List of all labels of this panel. */
    private Vector<JLabel> labelSequence;
    
    private BoxLayout layoutBox;

    /* Constructor to create a text sequence panel with numbers to identify the number of each codon. */
    public NumberingSequencePanel(SingleGenePanel container, int size)
    {
//        this.container = container;

        this.layoutBox = new BoxLayout(this, BoxLayout.X_AXIS);
        this.setLayout(layoutBox);

        /* Create label list. */
        labelSequence = new Vector<JLabel>(size);

        int sequenceLabelWidthPixel = (Integer) ApplicationSettings.getProperty("sequenceLabelWidthPixel", Integer.class);
        Dimension labelSize = new Dimension(sequenceLabelWidthPixel, 22);
        for (int i=1; i<=size; i++)
        {
            JLabel label;
            if (((i%5) != 0) && (i != 1))
                label = new JLabel(" ", SwingConstants.CENTER);
            else
                label = new JLabel(Integer.toString(i), SwingConstants.CENTER);

            /* Set monospaced font and line boarder. */
            if (i >= 10000)
                label.setFont( new Font(Font.MONOSPACED, Font.PLAIN, 8) );
            else
                label.setFont( new Font(Font.MONOSPACED, Font.PLAIN, 10) );
            //label.setBorder( BorderFactory.createLineBorder(Color.LIGHT_GRAY) );

            label.setPreferredSize( labelSize );
            label.setMinimumSize( labelSize );
            label.setMaximumSize( labelSize );

            /* Hack to record in the label what is its index. */
            label.setIconTextGap(i-1);

            labelSequence.add( label );
            this.add(label);
        }

        sequenceSize = size * 3;
        numberOfPadding = 0;

        assert sequenceSize > 0;
    }
    
}
