package pt.ua.ieeta.geneoptimizer.GUI.GenePanel;

import java.awt.*;
import java.util.Vector;
import javax.swing.*;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.Study;
import pt.ua.ieeta.geneoptimizer.Main.ApplicationSettings;
import pt.ua.ieeta.geneoptimizer.geneDB.BioStructure;

/**
 *
 * @author Paulo Gaspar
 */
public final class InteriorContentPanel extends JPanel
{
    /* Scrolling zone. Where the sequences are. */
    private JScrollPane scrollpane;
    private JPanel scrollContent;

    /* This box will take all the content. */
    private Box contentBox;

    /* List of sequence panels in this content panel. */
    private Vector<SequencePanel> sequencePanels;

//    /* Names of each sequence that is shown in the panel. Described before each sequence. */
//    private Vector<String> names;
//
//    /* Set of genes to show in this panel. */
//    private Vector<Gene> genes;
//
//    /* Set of structure types that should be displayed for each gene. */
//    private Vector<BioStructure.Type> types;
//
//    /* Color maps to paint the sequence panels. */
//    private Vector<Vector<Color>>  colorVector;

    private Vector<GenePanelEntry> sequencePanelEntries;
    
    /* Content panel where this panel is inserted. */
    private SingleGenePanel container;
    
    /* scrollbar that controls the viewport's horizontal view position to the scrollpane */
    private JScrollBar scrollBar;

    /* Tells wether or not to show aligned sequences if available. */
    private boolean alignSequences;
    private boolean showProtSecondStruct;
    private boolean showNumbering;
    
    private boolean isDetach;
    private JScrollPane detachScrollPane;

    public InteriorContentPanel(Study study, SingleGenePanel container, Vector<GenePanelEntry> entries, boolean alignSequences, boolean showProtSecondStruct, boolean showNumbering, boolean detach)
    {
        assert entries != null;
        assert !entries.isEmpty();
        
        this.sequencePanelEntries = entries;
        
        this.container = container;
        this.alignSequences = alignSequences;
        this.showProtSecondStruct = showProtSecondStruct;
        this.showNumbering = showNumbering;
        this.isDetach = detach;
        this.scrollBar = study.getProject().getContainerPanel().getScrollBar();

        /* Create scrolling suport to take sequence panels. */
        this.scrollContent = new JPanel();
        this.scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        this.scrollpane = new JScrollPane(scrollContent, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.scrollpane.setBorder(BorderFactory.createEmptyBorder());
        this.scrollpane.setHorizontalScrollBar(scrollBar);
        
        /* Create box to hold everything, and create a list to save labels. */
        this.contentBox = new Box(BoxLayout.X_AXIS);
        this.sequencePanels = new Vector<SequencePanel>();
        
        /* Create a new border layout to dispose components. */
        this.setLayout(new BorderLayout());
        
        /* Fill this panel. */
        fillContentBox();
    }

    /* Fill content box with sequences and names. */
    public void fillContentBox()
    {
        /* Clear contents. */
        this.removeAll();
        
        /* Add all the created contents to this panel. */
        this.add(contentBox, BorderLayout.NORTH);
        
        /* Clear content box, scrolling content and sequencePanels list. */
        this.contentBox.removeAll();
        this.scrollContent.removeAll();
        this.sequencePanels.removeAllElements();
        
        if(this.detachScrollPane != null) {
            this.detachScrollPane.removeAll();
        }
        
        // redefine scroll bar
        this.scrollpane.setHorizontalScrollBar(scrollBar);

        /* Create panel for the labels of the sequences. */
        JPanel namesPanel = new JPanel();
        namesPanel.setLayout(new GridLayout(sequencePanelEntries.size()+1, 1));
        namesPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        namesPanel.addMouseListener(container);

        /* Add numbers panel (sequence panel with each codon numbered) */
        if (showNumbering && !isDetach)
        {
            int lenght = alignSequences && sequencePanelEntries.get(0).getGene().hasAlignedStructure(BioStructure.Type.mRNAPrimaryStructure) ? sequencePanelEntries.get(0).getGene().getAlignedStructure(BioStructure.Type.mRNAPrimaryStructure).getLength() : sequencePanelEntries.get(0).getGene().getSequenceLength();
            addSequenceLabelPanel(new NumberingSequencePanel(container, lenght));
            namesPanel.add(new JLabel(""));
        }

        for (GenePanelEntry entry : sequencePanelEntries)
        {
            /* Add new label. */
            String name = entry.getName().length()>20? entry.getName().substring(0, 17) + "..." : entry.getName();
            JLabel newLabel = new JLabel("<html><center>" + name + "</center></html>");
            newLabel.setToolTipText(entry.getName());
            newLabel.setFont(new Font(null, Font.PLAIN, 9));
            newLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            newLabel.addMouseListener(container);
            namesPanel.add(newLabel);

            /* Add sequence. */
            BioStructure sequence;
            if (entry.getGene().hasAlignedStructure(entry.getSequenceType()) && alignSequences)
                sequence = entry.getGene().getAlignedStructure(entry.getSequenceType());
            else if (entry.getGene().hasSequenceOfType(entry.getSequenceType()))
                sequence = entry.getGene().getStructure(entry.getSequenceType());
            else
                sequence = null;
            
            if (sequence != null) 
                addSequence(sequence, entry.getColorScheme());
        }
        
        // define size of names panel
        int sizeOfNameLabel = (Integer) ApplicationSettings.getProperty("sizeOfSpecieNameLabel", Integer.class);
        namesPanel.setPreferredSize(new Dimension(sizeOfNameLabel, 10));
        namesPanel.setMinimumSize(new Dimension(sizeOfNameLabel, 10));
        
        /* Add scrollPane to this content panel. */
        this.contentBox.add(namesPanel);

        if (!isDetach) 
        {
            /* Add scrollPane to the content box. */
            this.contentBox.add(scrollpane);
            
            /* Set preferred and maximum sizes. */
            int height = scrollpane.getPreferredSize().height;
            
            /* Fit this panel horizontaly into the container panel. */
            this.setPreferredSize(new Dimension(Integer.MAX_VALUE, height + 1));
            this.setMaximumSize(this.getPreferredSize());
        }
        else 
        {
            this.setPreferredSize(null);
            this.setMaximumSize(null);
        }
    }

    public void setNewInfo(Vector<GenePanelEntry> entries, boolean alignSequences, boolean showProtSecondStruct, boolean showNumbering, boolean isDetach)
    {
        assert entries != null;

        this.sequencePanelEntries = entries;
        this.alignSequences = isDetach ? false : alignSequences; // to avoid exception when detach panel
        this.showProtSecondStruct = showProtSecondStruct;
        this.isDetach = isDetach;
        
        fillContentBox();
    }

    private void addSequence(BioStructure structure, Vector<Color> colorScheme)
    {
        assert structure != null;

        /* Create new sequence panel var. */
        SequencePanel sequence;

        /* Select which sequence type to create, and build it. */
        if ((structure.getType() == BioStructure.Type.proteinSecondaryStructure))
        {
            if (!showProtSecondStruct) return;
            sequence = new ImageSequencePanel(structure, ApplicationSettings.getImageMapForSecondaryStructure());
        } 
        else if (colorScheme != null)
            sequence = new TextSequencePanel(container, structure, colorScheme, isDetach);
        else
            sequence = new TextSequencePanel(container, structure, isDetach);

        addSequenceLabelPanel(sequence);
    }

    private void addSequenceLabelPanel(SequencePanel sequence)
    {
        assert sequence != null;
        
        sequence.setAlignmentX(0.0f);
        sequence.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
        
        /* Add padding to align with other sequence panels. */
//        sequence.setPaddingLabels(ContainerPanel.getMaxCharacters());
        
        sequence.addMouseListener(container);

//        sequence.setBorder(BorderFactory.createLineBorder(Color.ORANGE));
        
        /* Add sequence to parent panel. */
        if (!this.isDetach) {
            this.scrollContent.add(sequence);
        } else {
            this.detachScrollPane = new JScrollPane(sequence, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            this.add(detachScrollPane, BorderLayout.CENTER);
        }

        this.sequencePanels.add(sequence);

    }

    public synchronized void addPadding(int finalSize)
    {
        for (SequencePanel lsp: sequencePanels)
            lsp.setPaddingLabels(finalSize);
    }

    public synchronized int getMaxCharacters()
    {
        int biggest = 0;
        for (SequencePanel lsp: sequencePanels)
            if (lsp.getSequenceWidthInChars() > biggest)
                biggest = lsp.getSequenceWidthInChars();

        return biggest;
    }

    public synchronized JScrollPane getScrollingPane()
    { 
        return scrollpane;
    }

    public Iterable<SequencePanel> getSequencePanels()
    {
        return sequencePanels;
    }
}