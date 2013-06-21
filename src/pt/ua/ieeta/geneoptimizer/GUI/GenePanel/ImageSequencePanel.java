package pt.ua.ieeta.geneoptimizer.GUI.GenePanel;

import java.awt.Dimension;
import java.awt.Font;
import java.util.Map;
import java.util.Vector;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import pt.ua.ieeta.geneoptimizer.Main.ApplicationSettings;
import pt.ua.ieeta.geneoptimizer.geneDB.BioStructure;

/**
 *
 * @author Paulo Gaspar
 */
public class ImageSequencePanel extends SequencePanel
{
    /* List of all labels of this panel. */
    private Vector<JLabel> labelSequence;

    private BioStructure.Type sequenceType;

    public ImageSequencePanel(BioStructure structure, Map<String, ImageIcon> imageMap)
    {
        assert structure != null;

        sequenceType = structure.getType();

        /* Create layout to place labels correctly. */
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
//        this.setLayout(new GridLayout(1,structure.getLength(), 0, 1));

        /* Create label list. */
        labelSequence = new Vector<JLabel>(structure.getLength());

        /* Create labels, and place them into the JPanel. */
        int sequenceLabelWidthPixel = (Integer) ApplicationSettings.getProperty("sequenceLabelWidthPixel", Integer.class);
        Dimension labelSize = new Dimension(sequenceLabelWidthPixel, 22);
        for (int i=0; i<structure.getLength(); i++)
        {
            //if (!imageMap.containsKey(structure.getWordAt(i))) continue;

            JLabel label = new JLabel("", SwingConstants.CENTER);
            label.setIconTextGap(0);

            /* Tweek to allow make an arrow on strands. */
            if ((i != structure.getLength()-1) && structure.getWordAt(i).equals("E") && !structure.getWordAt(i+1).equals("E"))
                label.setIcon(imageMap.get("Eend"));
            else
                label.setIcon(imageMap.get(structure.getWordAt(i)));

            label.setFont( new Font(Font.MONOSPACED, Font.PLAIN, 14) );
            //label.setBorder( BorderFactory.createLineBorder(Color.LIGHT_GRAY) );

            label.setPreferredSize(labelSize);
            label.setMinimumSize(labelSize);
            label.setMaximumSize(labelSize);

            labelSequence.add( label );
            this.add(label);
        }

        sequenceSize = structure.getSequenceOccupation();
        numberOfPadding = 0;
        validate();

        //assert labelSequence.size() == structure.getLength()-1;
        assert sequenceSize > 0;
    }

    public BioStructure.Type getSequenceType()
    {
        return sequenceType;
    }

}
