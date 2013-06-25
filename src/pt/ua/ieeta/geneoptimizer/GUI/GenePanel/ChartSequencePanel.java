package pt.ua.ieeta.geneoptimizer.GUI.GenePanel;

import javax.swing.BoxLayout;

/**
 *
 * @author Paulo Gaspar
 */
public class ChartSequencePanel extends SequencePanel
{

    public ChartSequencePanel()
    {
         /* Create layout to place labels correctly. */
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    }

    @Override
    public int getSequenceWidthInChars()
    {
        return 100;
    }

    @Override
    public void setPaddingLabels(int finalSize)
    {
        
    }

}