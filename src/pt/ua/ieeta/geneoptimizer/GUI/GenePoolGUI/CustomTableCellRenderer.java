package pt.ua.ieeta.geneoptimizer.GUI.GenePoolGUI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import static javax.swing.SwingConstants.LEFT;
import static javax.swing.SwingConstants.RIGHT;
import javax.swing.table.DefaultTableCellRenderer;
import pt.ua.ieeta.geneoptimizer.Main.Tuple;

/**
 *
 * @author Nuno Silva <nuno.mogas@ua.pt>
 */
public class CustomTableCellRenderer extends DefaultTableCellRenderer {

    private List<String> rowsName;
    private List<Tuple<String, Color>> rowColorSet;    

    public CustomTableCellRenderer(List<String> rowsName, int column) {
        //gene name
        if (column == 0) {
            this.setHorizontalAlignment(LEFT);
        } else if (column == 1) {
            //codons size
            this.setHorizontalAlignment(RIGHT);
        }
        this.rowsName = rowsName;
        setOpaque(true);

        rowColorSet = new ArrayList<Tuple<String, Color>>();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (column == 0) {
            if (rowsName.contains((String) value)) {
                boolean found = false;
                for (int i = 0; i < rowColorSet.size(); i++) {
                    if (rowColorSet.get(i).getX().equalsIgnoreCase((String) value)) {
                        found = true;
                    }
                }
                if (!found) {
                    rowColorSet.add(new Tuple<String, Color>((String) value, Color.BLUE));
                }
            } else {
                boolean found = false;
                for (int i = 0; i < rowColorSet.size(); i++) {
                    if (rowColorSet.get(i).getX().equalsIgnoreCase((String) value)) {
                        found = true;
                    }
                }
                if (!found) {
                    rowColorSet.add(new Tuple<String, Color>((String) value, Color.BLACK));
                }
            }
        }
        for (int i = 0; i < rowColorSet.size(); i++) {
            if (rowColorSet.get(i).getX().equalsIgnoreCase((String) value)) {
                c.setForeground(rowColorSet.get(i).getY());
                if (c.getForeground() == Color.BLUE) {
                    c.setFont(new Font(c.getFont().getName(), Font.BOLD, c.getFont().getSize()));
                }
            }
        }
        return c;
    }
}
