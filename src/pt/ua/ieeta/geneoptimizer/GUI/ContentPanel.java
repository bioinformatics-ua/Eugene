/*
 * ContentPanel.java
 */
package pt.ua.ieeta.geneoptimizer.GUI;

import com.l2fprod.common.swing.JTaskPaneGroup;
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 *
 * @author Paulo Gaspar
 */
public abstract class ContentPanel extends JPanel
{
    /**
     * Flag to indicate if this content panel needs a global scroll bar.
     */
    private boolean scrollbarPolicy;

    /**
     * Exterior panels just for graphical purposes.
     */
    protected JTaskPaneGroup contentPanel = null;

    /**
     * Panel title. 
     */
    private String title;
    
    static Color defaultColour1 = (Color) UIManager.get("TaskPaneGroup.titleBackgroundGradientStart");
    static Color defaultColour2 = (Color) UIManager.get("TaskPaneGroup.titleBackgroundGradientEnd");
   
    /**
     * Constructor with a specialType argument that allows choosing a type/color 
     * for the content pane.
     * 
     * @param title Title of panel
     * @param globalScroolbar use global scrool bar
     * @param specialType if a special type or 0 if not
     */
    public ContentPanel(String title, boolean globalScroolbar, int specialType)
    {
        assert title != null;

        this.title = title;
        this.scrollbarPolicy = globalScroolbar;
        
        boolean hasSpecialType = specialType != 0;
        Color c1 = null, c2 = null;
        
        switch(specialType)
        {
            case 0 : c1 = defaultColour1; c2 = defaultColour2; break;
            case 1 : c1 = new Color(255, 255, 255); c2 = new Color(253, 245, 147); break;
        }
        
        /* Change colours if needed. */
        if (hasSpecialType)
        {
            UIManager.put("TaskPaneGroup.titleBackgroundGradientStart", c1);
            UIManager.put("TaskPaneGroup.titleBackgroundGradientEnd", c2);
        }
        
        /* Create the task pane group. This is the nice looking panel that allows collapsing and closing. */
        contentPanel = new JTaskPaneGroup();
        contentPanel.add(this);
        contentPanel.setTitle(title);
        contentPanel.setCollapsable(globalScroolbar);
        
        
        /* Replace defaults. */
        if (hasSpecialType)
        {
            UIManager.put("TaskPaneGroup.titleBackgroundGradientStart", defaultColour1);
            UIManager.put("TaskPaneGroup.titleBackgroundGradientEnd", defaultColour2);
        }
    }
    
    /**
     * Constructor with a specialType argument that allows choosing a type/color 
     * for the content pane.
     * Same as ContentPanel(title, globalScroolbar, 0)
     * 
     * @param title Title of panel
     * @param globalScroolbar use global scrool bar
     */
    public ContentPanel(String title, boolean globalScroolbar)
    {
        this(title, globalScroolbar, 0);
    }
    

    public synchronized boolean needsScrollbar() { return scrollbarPolicy; }
    public synchronized int getMaxCharacters() { return 0; }
    public synchronized void setPadding(int finalSize) {}
    public void remakePanel() {}
    public void deletePanel() {}
    
    /**
     * getTitle
     * @return the title of panel
     */
    public String getTitle() 
    {
        return title;
    }

    /**
     * Define the title of panel
     * @param title title of panel
     */
    public void setTitle(String title) 
    {
        this.title = title;

        /* IF there is an exterior panel, update its title. */
        if (contentPanel != null)
        {
            contentPanel.setTitle(title);
            contentPanel.updateUI();
        }
    }

    /* Mark this panel border as the selected panel of this project. */
    public synchronized void setSelected(boolean flag)
    {
        assert contentPanel != null;
        
        contentPanel.setSpecial(flag);
    }

    void setExteriorPanel(JTaskPaneGroup actionPane)
    {
        assert actionPane != null;

        this.contentPanel = actionPane;
    }

    public JTaskPaneGroup getContentPanel()
    {
        return contentPanel;
    }
}
