package pt.ua.ieeta.geneoptimizer.GUI.GenePanel;

import java.awt.*;

/**
 * A modified version of FlowLayout that allows containers using this
 * Layout to behave in a reasonable manner when placed inside a
 * JScrollPane
 * @author Babu Kalakrishnan
 * Modifications by Paulo Gaspar and Ricardo Gonzaga
 */
public class ModifiedFlowLayout extends FlowLayout
{

    public ModifiedFlowLayout()
    {
        super();
    }

    public ModifiedFlowLayout(int align)
    {
        super(align);
    }

    public ModifiedFlowLayout(int align, int hgap, int vgap)
    {
        super(align, hgap, vgap);
    }

    @Override
    public Dimension minimumLayoutSize(Container target)
    {
        // Size of largest component, so we can resize it in
        // either direction with something like a split-pane.
        return computeMinSize(target);
    }

    @Override
    public Dimension preferredLayoutSize(Container target)
    {
        return computeSize(target);
    }

    private Dimension computeSize(Container target)
    {
        //if (true)
        //    return super.preferredLayoutSize(target);
        
        synchronized (target.getTreeLock()) {
            int hgap = getHgap();
            int vgap = getVgap();
            int w = target.getWidth();

            // Let this behave like a regular FlowLayout (single row)
            // if the container hasn't been assigned any size yet
            if (w == 0) {
                w = Integer.MAX_VALUE;
            }

            Insets insets = target.getInsets();
            if (insets == null) {
                insets = new Insets(0, 0, 0, 0);
            }
            int reqdWidth = 0;

            int maxwidth = w - (insets.left + insets.right + hgap * 2);
            int n = target.getComponentCount();
            int x = 0;
            int y = insets.top + vgap; // FlowLayout starts by adding vgap, so do that here too.
            int rowHeight = 0;
            
            Component ci = target.getComponent(0);
            Dimension di = ci.getPreferredSize();  //get width from first component
            int perLine = Math.min(maxwidth / (di.width + hgap), n) ;
            int lines = (int) Math.min(Math.ceil((float)n / (float)perLine), n);
            int gy = lines*(vgap + di.height) + insets.bottom + 1;
            int gx = perLine * (di.width + hgap) + insets.left + insets.right - 1;
            
//            System.out.println("Window Dimension: " + target.getSize().toString() + "    o normal daria: " + super.preferredLayoutSize(target).toString() + "   este deu: [" + gx+ ", " + gy+"]");
//            System.out.println("Insets: " + insets.left + " " + insets.right);
//            System.out.println("Horizontal Count: " + perLine);
//            System.out.println("Vertical Count: " + lines);
//            System.out.println("Calculated: " + gx + "  " + gy);

            for (int i = 0; i < n; i++) {
                Component c = target.getComponent(i);
                if (c.isVisible()) {
                    Dimension d = c.getPreferredSize();
                    if ((x == 0) || ((x + d.width) <= maxwidth)) {
                        // fits in current row.
                        if (x > 0) {
                            x += hgap;
                        }
                        x += d.width;
                        rowHeight = Math.max(rowHeight, d.height);
                    } else {
                        // Start of new row
                        x = d.width;
                        y += vgap + rowHeight;
                        rowHeight = d.height;
                    }
                    reqdWidth = Math.max(reqdWidth, x);
                }
            }
            y += rowHeight;
            y += insets.bottom;
            
            
            // hard coded version to optimize the resize
            // avoid stupid animation
            //return new Dimension(reqdWidth + insets.left + insets.right, y);
            return new Dimension(1, y);
        }
    }

    private Dimension computeMinSize(Container target)
    {
        synchronized (target.getTreeLock()) {
            int minx = Integer.MAX_VALUE;
            int miny = Integer.MIN_VALUE;
            boolean found_one = false;
            int n = target.getComponentCount();

            for (int i = 0; i < n; i++) {
                Component c = target.getComponent(i);
                if (c.isVisible()) {
                    found_one = true;
                    Dimension d = c.getPreferredSize();
                    minx = Math.min(minx, d.width);
                    miny = Math.min(miny, d.height);
                }
            }
            if (found_one) {
                return new Dimension(minx, miny);
            }
            return new Dimension(0, 0);
        }
    }
}
