/*
 * TextSequencePanel.java
 */
package pt.ua.ieeta.geneoptimizer.GUI.GenePanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import pt.ua.ieeta.geneoptimizer.GUI.Protein3DViewerPanel;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.Study;
import pt.ua.ieeta.geneoptimizer.Main.ApplicationSettings;
import pt.ua.ieeta.geneoptimizer.Main.ProjectManager;
import pt.ua.ieeta.geneoptimizer.geneDB.BioStructure;
import pt.ua.ieeta.geneoptimizer.geneDB.Gene;

/**
 *
 * @author Paulo Gaspar
 */
public class TextSequencePanel extends SequencePanel implements MouseListener, MouseMotionListener
{
    /* List of all labels of this panel. */
    private List<JLabel> labelSequence;

    /* Color vector, if there is one, to ilustrate labels. */
    private List<Color> colorVector;

    /* Original bio structure for this panel. */
    private BioStructure structure;

    /* Container of this panel. */
    private SingleGenePanel container;

    /* Boolean var to control if this panel takes a codon sequence. */
    private boolean isCodonSequence = false, isAminoSequence = false;
    
    private BoxLayout layoutBox;
    private FlowLayout layoutFlow;
    
    /* Control variable to changing a codon*/
    private int codonIdx = -1;
    
    /* JComboBox singleton */
    private final JComboBox changeCodonBox = new JComboBox();
    
    /* Boolean creation event */
    private boolean preEvent = false;
    
    public TextSequencePanel(SingleGenePanel container, BioStructure structure, boolean detach)
    {
        this(container, structure, null, detach);
    }

    public TextSequencePanel(SingleGenePanel container, BioStructure structure, List<Color> colorVector, boolean detach)
    {
        assert structure != null;
        if (colorVector != null)
        {
            if (colorVector.size() != structure.getLength())
            {
                System.out.println("ColorVectorSize: " + colorVector.size() + "    StructureLen: " + structure.getLength());
                System.out.println(structure.getSequence());
            }
            assert colorVector.size() == structure.getLength();
        }
        this.colorVector = colorVector;
        this.structure = structure;
        this.container = container;

        /* Create layout to place labels correctly. */
        this.layoutBox = new BoxLayout(this, BoxLayout.X_AXIS);
        this.layoutFlow = new ModifiedFlowLayout(ModifiedFlowLayout.LEADING, 1, 0);

        // chose the correct layout as this detach or not
        if (!detach) {
            this.setLayout(layoutBox);
        } else {
            this.setLayout(layoutFlow);
            this.setSize(300, 150);
        }
        this.setOpaque(true);

        /* Create label list. */
        labelSequence = Collections.synchronizedList(new ArrayList<JLabel>(structure.getLength()));

        /* Create labels, and place them into the JPanel. */
        int sequenceLabelWidthPixel = (Integer) ApplicationSettings.getProperty("sequenceLabelWidthPixel", Integer.class);
        Dimension labelSize = new Dimension(sequenceLabelWidthPixel, 22);
        LineBorder lBorder = new LineBorder(new Color(240,240,240), 1);
        Font f = new Font(Font.MONOSPACED, Font.PLAIN, 14);
        for (int i=0; i<structure.getLength(); i++)
        {
            JLabel label = new JLabel(structure.getWordAt(i), SwingConstants.CENTER);

            /* Set monospaced font and line boarder. */
            label.setFont(f);
            if (!structure.getWordAt(i).equals(" ") && !structure.getWordAt(i).contains("-"))
                label.setBorder( BorderFactory.createLineBorder(Color.LIGHT_GRAY) );

            label.setPreferredSize( labelSize );
            label.setMinimumSize( labelSize  );
            label.setMaximumSize( labelSize );
         
            /* If it is a codon sequence, customize appearence and other options. */
            if (structure.getType().equals(BioStructure.Type.mRNAPrimaryStructure))
            {
                isCodonSequence = true;

                label.setBorder(lBorder);

                /* Hack to record in the label what is its index. */
                label.setIconTextGap(i);
            }
            else
            if (structure.getType().equals(BioStructure.Type.proteinPrimaryStructure))
            {
                isAminoSequence = true;
                label.setIconTextGap(i);
            }

            /* Set label mouse reaction */
            if (container != null)
            {
                label.addMouseListener(this);
                label.addMouseListener(container);
            }

            /* Set label color */
            if (colorVector != null)
            {
                label.setOpaque(true);
                label.setBackground(colorVector.get(i));
                if ((colorVector.get(i).getRed()+colorVector.get(i).getBlue()+colorVector.get(i).getGreen()) < 200)
                    label.setForeground(Color.WHITE);
            }

            labelSequence.add( label );
            this.add(label);
        }
        
        /* Defining properties of JComboBox that change codons */
        Dimension comboBoxSize = new Dimension(sequenceLabelWidthPixel+2, 22);
        changeCodonBox.setPreferredSize(comboBoxSize);
        changeCodonBox.setMinimumSize(comboBoxSize);
        changeCodonBox.setMaximumSize(comboBoxSize);
        changeCodonBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if(!preEvent)
                    preEvent = true;
                else {
                System.out.println((String) changeCodonBox.getSelectedItem());
                if(!ProjectManager.getInstance().getSelectedProject().getSelectedStudy().getResultingGene().getCodonAt(codonIdx).equals(changeCodonBox.getSelectedItem()))
                    createManualStudy();
                removeComboBox();
                }
            }
        });
        
        sequenceSize = structure.getSequenceOccupation();
        numberOfPadding = 0;

        assert labelSequence.size() == structure.getLength();
        assert sequenceSize > 0;
    }



    /**********************************************************/
    /************************* MOUSE **************************/
    /**********************************************************/

    public boolean dragging = false, isOut = false;
    public static boolean isSelected = false;
    public int dragStartIndex, dragEndIndex;


    private void clearSelection()
    {
        if (colorVector != null)
            for (int i=0; i<labelSequence.size(); i++)
            {
                labelSequence.get(i).setBackground(colorVector.get(i));
                labelSequence.get(i).setForeground(Color.black);
            }

        isSelected = false;

        Protein3DViewerPanel.getInstance().unselectAll();
    }

    /* When clicking the mouse, remove any selection. */
    @Override
    public void mouseClicked(MouseEvent e)
    {
        ProjectManager.getInstance().getSelectedProject().setSelectedStudy(container.getStudy());
        
        if(isCodonSequence && !isSelected && e.getClickCount() == 2 && !e.isConsumed()) {
            if(codonIdx == -1)
                changeCodon(getComponentIndex((JLabel) e.getSource()));
            e.consume();
        }
        
        if (isCodonSequence && !e.isShiftDown())
        {
            clearSelection();
            container.getStudy().removeSelection();
        }
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        if ((e.getButton() == MouseEvent.BUTTON1) && (ProjectManager.getInstance().getSelectedProject().getSelectedStudy().equals(container.getStudy())))
        {
            /* If holding shift while a selection is active, expand the selection. */
            if (isSelected && e.isShiftDown()) //&& isOutOfSelection(e)
            {
                dragEndIndex = ((JLabel)e.getComponent()).getIconTextGap();
                paintSelected();
                dragging = true;
            }
            else
            {
                dragStartIndex = ((JLabel)e.getComponent()).getIconTextGap();
                dragging = true;
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        if (e.isPopupTrigger())
            container.getPopup().show(e.getComponent(), e.getX(), e.getY());
        
        if (!ProjectManager.getInstance().getSelectedProject().getSelectedStudy().equals(container.getStudy()))
            ProjectManager.getInstance().getSelectedProject().setSelectedStudy(container.getStudy());

        if (dragging && isCodonSequence)
        {
            dragging = false;
            if ((dragStartIndex != dragEndIndex) && (!isOut))
            {
                if (dragStartIndex<dragEndIndex)
                    container.getStudy().setSelection(dragStartIndex, dragEndIndex);
                else
                    container.getStudy().setSelection(dragEndIndex, dragStartIndex);
            }
        }
    }

    private void paintSelected()
    {
        Protein3DViewerPanel.getInstance().unselectAll();

        if (colorVector != null)
            for (int i=0; i<labelSequence.size(); i++)
            {
                labelSequence.get(i).setBackground(colorVector.get(i));
                labelSequence.get(i).setForeground(Color.black);
            }

        int increment = (dragStartIndex < dragEndIndex)? 1: -1;
        for (int i = dragStartIndex; i != dragEndIndex + increment;)
        {
            labelSequence.get(i).setBackground(Color.blue);
            labelSequence.get(i).setForeground(Color.white);
             i += increment;
        }

        Protein3DViewerPanel.getInstance().selectAminoAcid(dragStartIndex, dragEndIndex);
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
        isOut = false;

        dragEndIndex = ((JLabel)e.getComponent()).getIconTextGap();
        if (dragging && isCodonSequence)
        {
            if (dragStartIndex != dragEndIndex)
            {
                isSelected = true;
                paintSelected();
            }
        }

        Cursor cursor = new Cursor(Cursor.HAND_CURSOR);
        setCursor(cursor);

        if (container.getStudy().equals(ProjectManager.getInstance().getSelectedProject().getSelectedStudy()) && isAminoSequence && !isSelected)
            Protein3DViewerPanel.getInstance().selectAminoAcid(dragEndIndex, dragEndIndex);
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
       isOut = true;

       if (dragging && (colorVector != null) && isCodonSequence)
            clearSelection();

       Cursor cursor = new Cursor(Cursor.DEFAULT_CURSOR);
       setCursor(cursor);

       if (container.getStudy().equals(ProjectManager.getInstance().getSelectedProject().getSelectedStudy()) && isAminoSequence && !isSelected)
            Protein3DViewerPanel.getInstance().unselectAll();
    }

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
//    
//    /**********************************************************/
//    /******************** Scrollable **************************/
//    /**********************************************************/
//    
//    @Override
//     public Dimension getPreferredSize()
//    {
//        return getPreferredScrollableViewportSize();
//    }
//
//    @Override
//    public Dimension getPreferredScrollableViewportSize()
//    {
//        if( getParent() == null )
//            return getSize();
//        Dimension d = getParent().getSize();
//        int c = (int)Math.floor((d.width - getInsets().left - getInsets().right) / 50.0);
//        if( c == 0 )
//            return d;
//        int r = 20 / c;
//        if( r * c < 20 )
//            ++r;
//        return new Dimension(c * 50, r * 50);
//    }
//
//    @Override
//    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
//    {
//        return 50;
//    }
//
//    @Override
//    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
//    {
//        return 10;
//    }
//
//    @Override
//    public boolean getScrollableTracksViewportHeight()
//    {
//        return false;
//    }
//
//    @Override
//    public boolean getScrollableTracksViewportWidth()
//    {
//        return getParent() != null ? getParent().getSize().width > getPreferredSize().width : true;
//    }

    private void changeCodon(int idx) {
        String codon = ProjectManager.getInstance().getSelectedProject().getSelectedStudy().getResultingGene().getCodonAt(idx);
        codonIdx = idx;
        
        this.remove(codonIdx);
        List<String> synonims = ProjectManager.getInstance().getSelectedProject().getSelectedStudy().getResultingGene().getGenome().getGeneticCodeTable().getSynonymousFromCodon(codon);
        
        for (int i = 0; i < synonims.size(); ++i) {
            changeCodonBox.addItem(synonims.get(i));
        }

        this.add(changeCodonBox, codonIdx);
        changeCodonBox.setVisible(true);
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                changeCodonBox.showPopup();
            }
        });
        
        this.updateUI();
    }
    
    private int getComponentIndex(Component obj) {
        Component[] comps = this.getComponents();
        for(int i = 0; i < comps.length; ++i) {
            if(comps[i].equals(obj)) return i;
        }
        
        return -1;
    }
    
    private void removeComboBox() {
        changeCodonBox.removeAllItems();
        this.remove(codonIdx);
        this.add(labelSequence.get(codonIdx), codonIdx);
        codonIdx = -1;
        preEvent = false;
        this.updateUI();
   }
    
    private void createManualStudy() {
        String codonTemp = (String) changeCodonBox.getSelectedItem();
        Gene originalGene;
        BioStructure structure;

        if(ProjectManager.getInstance().getSelectedProject().getSelectedStudy().getName().endsWith("(manually edited)")) {
            originalGene = ProjectManager.getInstance().getSelectedProject().getSelectedStudy().getResultingGene();
            structure = originalGene.getStructure(BioStructure.Type.mRNAPrimaryStructure);
            originalGene.createStructure(structure.getSubSequence(0, codonIdx) + codonTemp + structure.getSubSequence(codonIdx+1, structure.getLength()), BioStructure.Type.mRNAPrimaryStructure);
            labelSequence.get(codonIdx).setText(codonTemp);
            labelSequence.get(codonIdx).setBackground(Color.white);
            this.updateUI();
        } else {
            originalGene = ProjectManager.getInstance().getSelectedProject().getSelectedStudy().getResultingGene();
            structure = originalGene.getStructure(BioStructure.Type.mRNAPrimaryStructure);
            Gene resultingGene = new Gene(originalGene.getName() + " (manually edited)", originalGene.getGenome());
            resultingGene.createStructure(structure.getSubSequence(0, codonIdx) + codonTemp + structure.getSubSequence(codonIdx+1, structure.getLength()), BioStructure.Type.mRNAPrimaryStructure);
            Study temp = new Study(originalGene, resultingGene, ProjectManager.getInstance().getSelectedProject().getSelectedStudy().getName() + " (manually edited)");
            ProjectManager.getInstance().getSelectedProject().addNewStudy(temp);
        }
    }
}
