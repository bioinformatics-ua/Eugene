
package pt.ua.ieeta.geneoptimizer.GUI;

import com.l2fprod.common.swing.JTaskPane;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import pt.ua.ieeta.geneoptimizer.Main.ApplicationSettings;
import pt.ua.ieeta.geneoptimizer.Main.Project;

/**
 *
 * @author Paulo Gaspar
 */
public class ContainerPanel extends JPanel
{
    /* Project for which this container panel is responsible. */
    private Project project;

    /* List of panels in this container. */
    private List<ContentPanel> contentPanels;

    /* Panel to take all the content panels inside. */
    private JTaskPane contentPanel;
    private JScrollPane contentScrollPane;
    
    /* Scroll bar to control content panels. */
    private JScrollBar globalScrollBar = null;
    static protected int biggestSize = 0;

    /* Constructor which defines the layout as a grid with one column. */
    public ContainerPanel(Project project)
    {        
        /* Box layout to display content panels in a page axis (vertically). */
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        //add thread safe list
        contentPanels = Collections.synchronizedList(new ArrayList());
        
        /* Create content panel to take all the contentPanels. */
        contentPanel = new JTaskPane();
        contentScrollPane = new JScrollPane(contentPanel);
        contentScrollPane.setBorder(null);
        this.add(contentScrollPane);
        
        /* Create global scroll bar, just in case it is needed. */
        globalScrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
        this.add(globalScrollBar, 0);
        showHideScrollbar(false);
        
        /* adjust the scroll increments */
        Integer codonSize = (Integer) ApplicationSettings.getProperty("sequenceLabelWidthPixel", Integer.class);
        globalScrollBar.setUnitIncrement(2*codonSize);
        globalScrollBar.setBlockIncrement(20*codonSize);
        
        this.project = project;
    }

    /* Add a content panel to this panel. */
    public synchronized void addContentPanel(ContentPanel panel)
    {
        assert panel != null;
        assert contentPanels != null;
        
        /* Add the new panel to the begining of the contentPanel. */
        contentPanel.add(panel.getContentPanel(), 0);
       
        /* Add panel to list of panels. */
        contentPanels.add(panel);

        /* If the new panel needs a scroll bar, show the global scroll bar. */
        if (panel.needsScrollbar())
        {   
            updatePanelsPadding();
            if (!globalScrollBar.isVisible())
                showHideScrollbar(true);
        }
        
        /* Update graphics to show new panel. */
        panel.updateUI(); //.repaint();
        updateUI();
    }

    public static synchronized int getMaxCharacters()
    {
        return biggestSize;
    }
    
    public synchronized void updatePanelsPadding()
    {
        biggestSize = 0;

        /* Find max characters panel. */
        for (ContentPanel panel : contentPanels)
            if (panel.getMaxCharacters() > biggestSize)
                biggestSize = panel.getMaxCharacters();

        /* Add to each panel empty characters to fit maximum size. */
        for (ContentPanel p : contentPanels)
            p.setPadding(biggestSize);

        /* This is needed probably because each time we empty the container 
         * (remove all panels) the scrollbar gets automatically removed (most 
         * likely the scrollpane is doing this). */
        if (!contentPanels.isEmpty())
            this.add(globalScrollBar, 0);
    }

    /* Remove a content panel from this panel. */
    public synchronized void removeContentPanel(ContentPanel panel)
    {
        assert panel != null;
        assert contentPanels != null;
        assert !contentPanels.isEmpty();
        
        contentPanel.remove(panel.getContentPanel());
        contentPanels.remove(panel);
        panel.deletePanel();

        updatePanelsPadding();

        if (contentPanels.isEmpty())
        {
            System.out.println("SCroll Visible: " + globalScrollBar.isVisible() + "  ContnetPanels empty: "  + contentPanels.isEmpty());
            showHideScrollbar(false);
        }
        
        this.updateUI();
    }

    public Project getProject()
    {
        assert project != null;

        return project;
    }

    public JScrollBar getScrollBar()
    {
        assert globalScrollBar != null;

        return globalScrollBar;
    }
    
    public final void showHideScrollbar(boolean show)
    {
//        System.out.println("************ Setting scrollbar visibility to " + show);
        globalScrollBar.setVisible(show);
    }

}
