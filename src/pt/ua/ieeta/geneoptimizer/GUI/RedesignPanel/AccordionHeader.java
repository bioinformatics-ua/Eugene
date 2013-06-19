
package pt.ua.ieeta.geneoptimizer.GUI.RedesignPanel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.*;
import nu.epsilon.rss.ui.utils.DrawUtil;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.OptimizationModel;
import pt.ua.ieeta.geneoptimizer.PluginSystem.IOptimizationPlugin;

/**
 *
 * @author Paulo Gaspar
 */
public class AccordionHeader extends JPanel {

    private JLabel title;
    private final JCheckBox checkbox;

    private IOptimizationPlugin plugin;
    private final static int RADIUS = 7;
    
    private boolean isExpanded = false;
    
    private ChevronIcon chevronTrue = new ChevronIcon(true);
    private ChevronIcon chevronFalse = new ChevronIcon(false);
    private ChevronIcon chevron;

    public AccordionHeader(IOptimizationPlugin iPlugin)
    {
        assert iPlugin != null;

//        this.setLayout(new GridLayout(1,2, 10, 0));
        this.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
        
        this.plugin = iPlugin;
        title = new JLabel("     " + plugin.getPluginName());

        /* Create title font, and apply to title label. */
        Font font = UIManager.getFont("Label.font").deriveFont(Font.BOLD);
        title.setFont(font);
        title.setForeground(new Color(70, 70, 70));

        add(title);
        add(Box.createHorizontalGlue());

        checkbox = new JCheckBox();
        checkbox.setOpaque(false);
        checkbox.addActionListener(
                new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        if (checkbox.isSelected())
                            title.setForeground(new Color(70, 70, 160));
                        else
                            title.setForeground(new Color(70, 70, 70));

                        if (!OptimizationModel.getInstance().isRunning())
                            StudyMakerPanel.getInstance().getButtonsPanel().enableDisableButtons();
                        plugin.setSelected(checkbox.isSelected());
                    }
                });

        add(checkbox);
        
        chevron = isExpanded ? chevronTrue :  chevronFalse;
                
        MouseListener mouseListener = new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent me) 
            {
                isExpanded = !isExpanded;
                chevron = isExpanded ? chevronTrue :  chevronFalse;
                repaint(); 
            }
            @Override
            public void mousePressed(MouseEvent me) {    repaint(); }
            @Override
            public void mouseEntered(MouseEvent me) {    repaint();}
            @Override
            public void mouseExited(MouseEvent me)
            {}
            @Override
            public void mouseReleased(MouseEvent me)
            {}
        };
        
        this.addMouseListener(mouseListener);
    }

    public IOptimizationPlugin getPlugin() {
        return plugin;
    }
    
    

    public void setSelected(boolean selected)
    {
        checkbox.setSelected(selected);
        
        if (selected)
            title.setForeground(new Color(70, 70, 160));
        else
            title.setForeground(new Color(70, 70, 70));

        StudyMakerPanel.getInstance().getButtonsPanel().enableDisableButtons();
        
        updateUI();
    }
    
    public boolean isSelected()
    {
        return checkbox.isSelected();
    }

    @Override
    public Dimension getPreferredSize()
    {
        return new Dimension(super.getPreferredSize().width, 24);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        Graphics2D g2 = (Graphics2D) g;
        Color color1 = new Color(191, 205, 219);
        Color color2 = new Color(255, 255, 255);
        Paint paint = new LinearGradientPaint(0, 0, getWidth(), 0, new float[]{0f, 1f}, new Color[]{color2, color1});
        g2.setPaint(paint);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS, RADIUS);
        DrawUtil.drawRoundEdge(g2, new Color(100, 100, 100), 0, 0, getWidth() - 1, RADIUS, true, true);
        //g2.setColor(new Color(70, 70, 70));
        //g.drawString(title.getText(), RADIUS, getHeight() - g.getFontMetrics().getHeight() / 2);
        
        int x = 5, y = 4, width = 15, height = 15;
        
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g.setColor(UIManager.getColor("TaskPaneGroup.titleBackgroundGradientStart"));
        g.fillOval(x,y,width,width);

        g.setColor(UIManager.getColor("TaskPaneGroup.titleBackgroundGradientStart").darker());
        g.drawOval(x,y,width,width);
        
        g.setColor(UIManager.getColor("TaskPaneGroup.titleForeground"));
        
        int chevronX = x + width / 2 - chevron.getIconWidth() / 2;
        int chevronY = y + (height / 2 - chevron.getIconHeight());
        chevron.paintIcon(this, g, chevronX, chevronY);
        chevron.paintIcon(this, g, chevronX, chevronY + chevron.getIconHeight() + 1);
        
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }
    
    protected static class ChevronIcon implements Icon 
    {
        boolean up = true;

        public ChevronIcon(boolean up) {
            this.up = up;
        }

        public int getIconHeight() {
            return 3;
        }

        public int getIconWidth() {
            return 6;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (up) {
                g.drawLine(x + 3, y, x, y + 3);
                g.drawLine(x + 3, y, x + 6, y + 3);
            } else {
                g.drawLine(x, y, x + 3, y + 3);
                g.drawLine(x + 3, y + 3, x + 6, y);
            }
        }
    }
}
