package pt.ua.ieeta.geneoptimizer.GUI.RedesignPanel;

// Import the GUI classes
import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;

/**
 * A JOutlookBar provides a component that is similar to a JTabbedPane, but instead of maintaining
 * tabs, it uses Outlook-style bars to control the visible component
 */
public class JOutlookBar extends JPanel implements MouseListener
{
  /**
   * The top panel: contains the buttons displayed on the top of the JOutlookBar
   */
  private JPanel topPanel = new JPanel( new GridLayout( 1, 1 ) );

  /**
   * The bottom panel: contains the buttons displayed on the bottom of the JOutlookBar
   */
  private JPanel bottomPanel = new JPanel( new GridLayout( 1, 1 ) );

  /**
   * A LinkedHashMap of bars: we use a linked hash map to preserve the order of the bars
   */
  private Map bars = new LinkedHashMap();

  /**
   * The currently visible bar (zero-based index)
   */
  private int visibleBar = 0;

  /**
   * A place-holder for the currently visible component
   */
  private JComponent visibleComponent = null;

  /**
   * Creates a new JOutlookBar; after which you should make repeated calls to
   * addBar() for each bar
   */
  public JOutlookBar()
  {
        this.setLayout( new BorderLayout() );
        this.add( topPanel, BorderLayout.NORTH );
        this.add( bottomPanel, BorderLayout.SOUTH );
  }

  /**
   * Adds the specified component to the JOutlookBar and sets the bar's name
   * 
   * @param  name      The name of the outlook bar
   * @param  componenet   The component to add to the bar
   */
  public void addBar( JComponent header, JComponent component )
  {
    BarInfo barInfo = new BarInfo( header, component );
    barInfo.getHeader().addMouseListener( this );
    this.bars.put( header, barInfo );
    render();
  }

 
  /**
   * Removes the specified bar from the JOutlookBar
   * 
   * @param  name  The name of the bar to remove
   */
  public void removeBar( String name )
  {
    this.bars.remove( name );
    render();
  }

  /**
   * Returns the index of the currently visible bar (zero-based)
   * 
   * @return The index of the currently visible bar
   */
  public int getVisibleBar()
  {
    return this.visibleBar;
  }

  /**
   * Programmatically sets the currently visible bar; the visible bar
   * index must be in the range of 0 to size() - 1
   * 
   * @param  visibleBar   The zero-based index of the component to make visible
   */
  public void setVisibleBar( int visibleBar )
  {
    if( visibleBar > 0 &&
      visibleBar < this.bars.size() - 1 )
    {
      this.visibleBar = visibleBar;
      render();
    }
  }

  /**
   * Causes the outlook bar component to rebuild itself; this means that
   * it rebuilds the top and bottom panels of bars as well as making the
   * currently selected bar's panel visible
   */
    public void render()
    {
        // Compute how many bars we are going to have where
        int totalBars = this.bars.size();
        int topBars = this.visibleBar + 1;
        int bottomBars = totalBars - topBars;


        // Get an iterator to walk through out bars with
        Iterator itr = this.bars.keySet().iterator();


        // Render the top bars: remove all components, reset the GridLayout to
        // hold to correct number of bars, add the bars, and "validate" it to
        // cause it to re-layout its components
        BarInfo barInfo = null;
        if (itr.hasNext())
        {
            this.topPanel.removeAll();
            GridLayout topLayout = (GridLayout) this.topPanel.getLayout();
            topLayout.setRows(topBars);

            for (int i = 0; i < topBars; i++)
            {
                JComponent header = (JComponent) itr.next();
                barInfo = (BarInfo) this.bars.get(header);
                this.topPanel.add(header);
            }
        }
        this.topPanel.validate();


//    if ((barInfo.component == visibleComponent) && (bottomPanel.getComponents().length != 0))
//    {
//        this.remove( this.visibleComponent );
//        return;
//    }

        // Render the center component: remove the current component (if there
        // is one) and then put the visible component in the center of this panel
        if (this.visibleComponent != null)
        {
            this.remove(this.visibleComponent);
        }
        
        if ((barInfo != null) && (barInfo.component != visibleComponent))
        {
            this.visibleComponent = barInfo.getComponent();
            this.add(visibleComponent, BorderLayout.CENTER);
//            visibleComponent.updateUI();
        }
        else
            if (barInfo.component == visibleComponent)
                visibleComponent = null;

        // Render the bottom bars: remove all components, reset the GridLayout to
        // hold to correct number of bars, add the bars, and "validate" it to
        // cause it to re-layout its components
        if (itr.hasNext())
        {
            this.bottomPanel.removeAll();
            GridLayout bottomLayout = (GridLayout) this.bottomPanel.getLayout();
            bottomLayout.setRows(bottomBars);
            for (int i = 0; i < bottomBars; i++)
            {
                JComponent header = (JComponent) itr.next();
                this.bottomPanel.add(header);
            }
        }
        this.bottomPanel.validate();

        // Validate all of our components: cause this container to re-layout its subcomponents
//        this.validate();
        updateUI();
    }

   /**
   * Debug, dummy method
   */
  public static JPanel getDummyPanel( String name )
  {
    JPanel panel = new JPanel( new BorderLayout() );
    panel.add( new JLabel( name, JLabel.CENTER ) );
    return panel;
  }

  /**
   * Debug test...
   */
  public static void main( String[] args )
  {
    JFrame frame = new JFrame( "JOutlookBar Test" );
    JOutlookBar outlookBar = new JOutlookBar();
    outlookBar.addBar(new JLabel("One"), getDummyPanel( "One" ));
    outlookBar.addBar(new JLabel("Two"), getDummyPanel( "Two" ));
    outlookBar.addBar(new JLabel("Three"), getDummyPanel( "Three" ));
    outlookBar.addBar(new JLabel("Four"), getDummyPanel( "Four" ));
    outlookBar.addBar(new JLabel("Five"), getDummyPanel( "Five" ));
    outlookBar.setVisibleBar( 2 );
    frame.getContentPane().add( outlookBar );

    frame.setSize( 800, 600 );
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation( d.width / 2 - 400, d.height / 2 - 300 );
    frame.setVisible( true );
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
  }
  

    @Override
    public void mouseClicked(java.awt.event.MouseEvent me)
    {
        int currentBar = 0;
        for (Iterator i = this.bars.keySet().iterator(); i.hasNext();)
        {
            JComponent header = (JComponent) i.next();
            if (header == me.getComponent())
            {
                // Found the selected button
                this.visibleBar = currentBar;
                render();
                return;
            }
            currentBar++;
        }
    }

    @Override
    public void mousePressed(java.awt.event.MouseEvent me)
    {
    }

    @Override
    public void mouseReleased(java.awt.event.MouseEvent me)
    {
    }

    @Override
    public void mouseEntered(java.awt.event.MouseEvent me)
    {
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    @Override
    public void mouseExited(java.awt.event.MouseEvent me)
    {
        setCursor(Cursor.getDefaultCursor());
    }

  /**
   * Internal class that maintains information about individual Outlook bars;
   * specifically it maintains the following information:
   * 
   * name      The name of the bar
   * button     The associated JButton for the bar
   * component    The component maintained in the Outlook bar
   */
  class BarInfo
  {
    /**
     * The name of this bar
     */
    private JComponent header;

    /**
     * The JButton that implements the Outlook bar itself
     */
//    private JButton button;

    /**
     * The component that is the body of the Outlook bar
     */
    private JComponent component;

    /**
     * Creates a new BarInfo
     * 
     * @param  name    The name of the bar
     * @param  component  The component that is the body of the Outlook Bar
     */
    public BarInfo( JComponent name, JComponent component )
    {
      this.header = name;
      this.component = component;
    }

     
    /**
     * Returns the outlook bar JButton implementation
     * 
     * @return   The Outlook Bar JButton implementation
     */
    public JComponent getHeader()
    {
      return header;
    }

    /**
     * Returns the component that implements the body of this Outlook Bar
     * 
     * @return The component that implements the body of this Outlook Bar
     */
    public JComponent getComponent()
    {
      return this.component;
    }
  }
  
  
}
