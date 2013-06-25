package pt.ua.ieeta.geneoptimizer.GUI.GenePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;
import pt.ua.ieeta.geneoptimizer.GUI.ContainerPanel;
import pt.ua.ieeta.geneoptimizer.GUI.ContentPanel;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.Study;
import pt.ua.ieeta.geneoptimizer.Main.ApplicationSettings;
import pt.ua.ieeta.geneoptimizer.Main.ProjectManager;
import pt.ua.ieeta.geneoptimizer.PluginSystem.IOptimizationPlugin;
import pt.ua.ieeta.geneoptimizer.geneDB.BioStructure;
import pt.ua.ieeta.geneoptimizer.geneDB.Gene;

/**
 *
 * @author Paulo Gaspar
 */
public class MultiSequencePanel extends ContentPanel implements MouseListener, MouseMotionListener
{
    /* The gene in this panel. */
    private Study study;

    /* Scrolling zone. Where the sequences codes are. */
    private JScrollPane scrollpane;
    private JPanel scrollContent;

    /* This box will take all the content. (used for alignment purposes, otherwise useless) */
    private Box contentBox;

    /* List of sequence panels in this content panel. */
    private List<SequencePanel> sequencePanels;

    /* Selected structure type to be displayed. */
    private BioStructure.Type selectedStructureType;

    /* Popup menu of this panel.  */
    private JPopupMenu popup;

    /* Color maps to paint the sequence panels. */
    private List<List<Color>>  colorList;

    /* Names of each sequence that is shown in the panel. Described before each sequence. */
    private List<IOptimizationPlugin> plugins;

    private int height;

    public MultiSequencePanel(Study study, String title)
    {
        this(study, title, null, null);
    }

    public MultiSequencePanel(Study study, String title, List<List<Color>> colorMap, List<IOptimizationPlugin> plugins)
    {
        super(title, true);

        assert study != null;
        assert title != null;
        assert plugins.size() == study.getResultingGenes().size();
        assert plugins.size() == colorMap.size();

        super.getContentPanel().setStudy(study);

        this.study = study;
        this.colorList = colorMap;
        this.plugins = plugins;

        /* Create scrolling suport to take sequence panels. */
        this.scrollContent = new JPanel();
        this.scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        this.scrollpane = new JScrollPane(scrollContent, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.scrollpane.setBorder(null);
        this.scrollpane.setHorizontalScrollBar(study.getProject().getContainerPanel().getScrollBar());

        /* Create box to hold everything, and create a list to keep each sequence panel. */
        this.contentBox = new Box(BoxLayout.X_AXIS);
        this.sequencePanels = Collections.synchronizedList(new ArrayList<SequencePanel>());

        /* Create a new border layout to dispose components. */
        setLayout(new BorderLayout());

        selectedStructureType = BioStructure.Type.proteinPrimaryStructure; //.mRNAPrimaryStructure;

        /* Create context menu. */
        popup = new JPopupMenu();
        buildContextMenu(popup);

        /* Fill content with genes and respective names. */
        fillContentBox();

        /* Add all the created contents to this panel. */
        add(contentBox, BorderLayout.NORTH);

        addMouseListener(this);
        addMouseMotionListener(this);
    }

    @Override
    public void deletePanel()
    {
            scrollpane.setHorizontalScrollBar(null);    
    }

    private void fillContentBox()
    {
        /* Clear content box, scrolling content and sequencePanels list. */
        this.contentBox.removeAll();
        this.scrollContent.removeAll();
        this.sequencePanels.clear();

        /* Get genes. */
        List<Gene> genes = study.getResultingGenes();

        /* Create and fill panel with names of sequences. */
        JPanel namesPanel = new JPanel();
        namesPanel.setLayout(new GridLayout(genes.size(),1)); //BoxLayout(namesPanel, BoxLayout.Y_AXIS));//
        
        for (int i=0; i<genes.size(); i++)
        {
            Gene gene = genes.get(i);
            if (gene.hasSequenceOfType(selectedStructureType))
            {
                addSequenceLabelPanel(gene.getStructure(selectedStructureType), i);

                JLabel geneName = new JLabel(plugins.get(i).getPluginName());
//                geneName.setPreferredSize(new Dimension(ApplicationSettings.getSizeOfGeneNameLabel(), sequencePanels.firstElement().getPreferredSize().height));
//                geneName.setMinimumSize(new Dimension(ApplicationSettings.getSizeOfGeneNameLabel(), sequencePanels.firstElement().getPreferredSize().height));
                namesPanel.add(geneName);
            }
        }

        int sizeOfNameLabel = (Integer) ApplicationSettings.getProperty("sizeOfSpecieNameLabel", Integer.class);
        namesPanel.setPreferredSize(new Dimension(sizeOfNameLabel, 10));
        namesPanel.setMinimumSize(new Dimension(sizeOfNameLabel, 10));

        /* Add scrollPane to this content panel. */
        this.contentBox.add(namesPanel);

        /* Add scrollPane to the content box. */
        this.contentBox.add(scrollpane);

        updateUI();
    }
    
    /* Returns the plugin currently being used to colour the codon sequence. */
    public List<IOptimizationPlugin> getColouringPlugins()
    {
        return plugins;
    }

    private void addSequenceLabelPanel(BioStructure structure, int index)
    {
        assert structure != null;

        /* Create new sequence panel. */
        SequencePanel sequence = null;

        if (colorList != null)
            sequence = new TextSequencePanel(null, structure, colorList.get(index), false);
        else
            sequence = new TextSequencePanel(null, structure, false);

        addSequenceLabelPanel(sequence);
    }

    private void addSequenceLabelPanel(SequencePanel sequence)
    {
        assert sequence != null;

        sequence.setAlignmentX(0.0f);
        sequence.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));

        /* Add padding to align with other sequence panels. */
        sequence.setPaddingLabels(ContainerPanel.getMaxCharacters());

        /* Add sequence to scrolling panel. */
        this.scrollContent.add(sequence);
        this.sequencePanels.add(sequence);

        sequence.addMouseListener(this);
        sequence.addMouseMotionListener(this);

        /* Set preferred and maximum sizes. */
        height = scrollpane.getPreferredSize().height;

        /* Fit this panel horizontaly into the container panel. */
        this.setPreferredSize(new Dimension(Integer.MAX_VALUE, height+1));
        this.setMaximumSize( this.getPreferredSize() );

        super.updateUI();
    }

    /* Build menu that popups when user right clicks this panel. */
    private void buildContextMenu(JPopupMenu popup)
    {
        /* MENU ITEM: show codon sequence. */
        JMenuItem menuItem = new JMenuItem("<html>View as <b>codon</b> sequences</html>");
        menuItem.addActionListener(
                new ActionListener()
                {@Override
                public void actionPerformed(ActionEvent e)
                 {
                        selectedStructureType = BioStructure.Type.mRNAPrimaryStructure;
                        fillContentBox();
                 } });
        popup.add(menuItem);

        /* MENU ITEM: show aminoacid sequence. */
        menuItem = new JMenuItem("<html>View as <b>aminoacid</b> sequences</html>");
        menuItem.addActionListener(
                new ActionListener()
                {@Override
                public void actionPerformed(ActionEvent e)
                 {
                        selectedStructureType = BioStructure.Type.proteinPrimaryStructure;
                        fillContentBox();
                 } });
        popup.add(menuItem);

        /* MENU ITEM: calculate protein secondary structure*/
//        menuItem = new JMenuItem("<html>View as <b>protein secondary structure</b></html>");
//        final MultiSequencePanel instance = this;
//        menuItem.addActionListener(
//                new ActionListener()
//                {public void actionPerformed(ActionEvent e)
//                 {
//                        new Psipred(study.getResultingGene().getAminoacidSequence(), instance).start();
//                 } });
//        popup.add(menuItem);

        //this.addMouseListener(this);
    }

   @Override
    public synchronized int getMaxCharacters()
    {
        int biggest = 0;
        for (SequencePanel lsp: sequencePanels)
            if (lsp.getSequenceWidthInChars() > biggest)
                biggest = lsp.getSequenceWidthInChars();

        return biggest;
    }

    @Override
    public synchronized void setPadding(int finalSize)
    {
        for (SequencePanel lsp: sequencePanels)
            lsp.setPaddingLabels(finalSize);
    }

    @Override
    public void remakePanel() {}





    /**************************************************************/
    /*                      MOUSE LISTENING                       */
    /**************************************************************/


    @Override
    public void mousePressed(MouseEvent e)
    {
        /* If CTRL is pressed, then user wants to join two studies into one. */
        if (e.isControlDown())
        {
            if (ProjectManager.getInstance().getSelectedProject().getSelectedStudy() != study)
            {
                //System.out.println("Clicked here, but i'm not selected.");
                //setCursor(new Cursor(Cursor.MOVE_CURSOR));

                /* Create new study with the two selected ones. */
                Study jointStudy = new Study(study, ProjectManager.getInstance().getSelectedProject().getSelectedStudy());

                /* Add new study to project. */
                ProjectManager.getInstance().getSelectedProject().addNewStudy(jointStudy);

                /* TODO: Delete the old ones. */
            }
        }
        else
            
        /* If left mouse button cliked, select the clicked study. */
        if (e.getButton() == MouseEvent.BUTTON1)
            ProjectManager.getInstance().getSelectedProject().setSelectedStudy(this.study);
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
        Cursor cursor = new Cursor(Cursor.HAND_CURSOR);
        setCursor(cursor);
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
        Cursor cursor = new Cursor(Cursor.DEFAULT_CURSOR);
        setCursor(cursor);
    }

    @Override
    public void mouseClicked(MouseEvent e) { }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        showPopup(e);
    }

    private void showPopup(MouseEvent e)
    {
            if (e.isPopupTrigger())
                popup.show(e.getComponent(), e.getX(), e.getY());
    }

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) { }
    
}
