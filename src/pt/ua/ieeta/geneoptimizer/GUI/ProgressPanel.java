package pt.ua.ieeta.geneoptimizer.GUI;

import java.awt.*;
import java.util.Vector;
import javax.swing.*;

/**
 *
 * @author Paulo Gaspar
 * @author Nuno Silva <nuno.mogas@ua.pt>
 */
public class ProgressPanel extends ContentPanel {
    /* Sigleton instance. */

    private static volatile ProgressPanel instance = null;

    /* List of progress panels inside this panel. */
    private Vector<ProcessPanel> processes;
    private JPanel contentPanel;

    public static ProgressPanel getInstance() {
        if (instance == null) {
            synchronized (ProgressPanel.class) {
                if (instance == null) {
                    instance = new ProgressPanel();
                }
            }
        }

        return instance;
    }

    private ProgressPanel() {
        super("Progress Panel", false);
        contentPanel = new JPanel();
        processes = new Vector<ProcessPanel>();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        this.setMaximumSize(new Dimension(230, 200));
        this.setPreferredSize(new Dimension(230, 117));
        this.setLayout(new BorderLayout());

        JScrollPane newScroller = new JScrollPane(contentPanel);
        newScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        newScroller.setBorder(null);
        newScroller.getVerticalScrollBar().setUnitIncrement(22);

        this.add(newScroller);
    }

    public ProcessPanel newProgressProcess(String title) {
        assert title != null;

        ProcessPanel newProcess = new ProcessPanel(title);
        JPanel newPanel = new JPanel();

        newPanel.add(newProcess);
        newPanel.setBorder(BorderFactory.createLineBorder(new Color(118, 153, 188)));
        newPanel.setPreferredSize(new Dimension((int) newPanel.getPreferredSize().getWidth() + 13, 40));
        newPanel.setMaximumSize(new Dimension((int) newPanel.getPreferredSize().getWidth() + 13, 40));

        contentPanel.add(newPanel, 0);
        contentPanel.add(Box.createRigidArea(new Dimension(10, 4)), 1);

        this.updateUI();

        return newProcess;
    }

    public class ProcessPanel extends JPanel {

        private JLabel titleLabel;
        private JProgressBar progressBar;
        private String title;
        private String status;

        public ProcessPanel(String title) {
            assert title != null;

            this.setLayout(new GridLayout(2, 1));

            this.title = title;
            this.titleLabel = new JLabel("<html><b>" + title + "</b></html>");
            this.titleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            this.progressBar = new JProgressBar();
            this.status = "";

            this.add(this.titleLabel);
            this.add(this.progressBar);

            //this.setPreferredSize(new Dimension(170, 50));

            this.setBorder(BorderFactory.createEmptyBorder(1, 3, 1, 3));
        }

        public void setProgress(int percentage) {
            assert percentage >= 0;
            assert percentage <= 100;

            this.progressBar.setValue(percentage);
        }

        public void setTitle(String title) {
            assert title != null;

            this.title = title;
            this.titleLabel.setText("<html><b>" + title + ":</b> " + status + "</html>");
        }

        public void setStatus(String status) {
            assert this.title != null;
            assert status != null;
            assert this.status != null;
            assert this.titleLabel != null;

            this.status = status;
            this.titleLabel.setText("<html><b>" + this.title + ":</b> " + status + "</html>");
        }

        public void setIndeterminated() {
            assert this.progressBar != null;

            this.progressBar.setIndeterminate(true);
        }

        public void setComplete() {
            assert this.progressBar != null;

            //this.progressBar.setForeground(Color.blue);
            this.progressBar.setIndeterminate(false);
            this.progressBar.setValue(100);
        }

        public void setFailed() {
            assert this.progressBar != null;

            //this.progressBar.setForeground(Color.red);
            this.progressBar.setIndeterminate(false);
            this.progressBar.setValue(100);
        }
    }
}
