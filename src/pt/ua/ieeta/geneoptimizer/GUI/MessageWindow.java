package pt.ua.ieeta.geneoptimizer.GUI;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 *
 * @author Paulo Gaspar
 */
public class MessageWindow extends JDialog implements Runnable
{

    private boolean showProgressBar = false;
    private boolean hasOkButton = false;
    private String message = "";
    private boolean stopThread = false;

    public MessageWindow(Dialog parent, boolean showProgressBar, boolean hasOkButton, String message)
    {
        super(parent);
        
        assert message != null;
        
        this.showProgressBar = showProgressBar;
        this.hasOkButton = hasOkButton;
        this.message = message;

//        this.setModalityType(ModalityType.APPLICATION_MODAL);
        
        showWindow();
    }

    public MessageWindow(Dialog parent, String message)
    {
        super(parent);
        this.message = message;
        
//        this.setModalityType(ModalityType.APPLICATION_MODAL);
    }

    public void showWindow()
    {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        JPanel contentPanel = new JPanel();
        int baseHeight = 20;

        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 10));

        /* Add message. */
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        JLabel messageLabel = new JLabel(message, JLabel.CENTER);
        int labelHeight = Math.max(60, messageLabel.getPreferredSize().height);
        messageLabel.setPreferredSize(new Dimension(300, labelHeight));
        labelPanel.add(messageLabel);
        labelPanel.setPreferredSize(new Dimension(300, labelHeight));
//        labelPanel.setBorder(BorderFactory.createLineBorder(Color.blue));
        contentPanel.add(labelPanel);

        /* Add progress bar */
        if (showProgressBar)
        {
            contentPanel.add(Box.createVerticalStrut(10));
            JProgressBar p = new JProgressBar();
            p.setIndeterminate(true);
            contentPanel.add(p);
        }

        /* Add OK button. */
        JPanel buttonPanel = null;
        if (hasOkButton)
        {
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            contentPanel.add(Box.createVerticalStrut(10));
            JButton b = new JButton("OK");
            b.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    dispose();
                }
            });
            buttonPanel = new JPanel();
//            buttonPanel.setBorder(BorderFactory.createLineBorder(Color.yellow, 1));
            buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
            buttonPanel.add(b);
            contentPanel.add(buttonPanel);
        }

        int width = Math.max(300, messageLabel.getPreferredSize().width);
        int height = Math.max(baseHeight, 10 + 15 + labelPanel.getPreferredSize().height) + (showProgressBar ? 30 : 0) + (hasOkButton ? buttonPanel.getPreferredSize().height : 0);
        contentPanel.setPreferredSize(new Dimension(width, height));
        getContentPane().add(contentPanel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void closeWindow()
    {
        stopThread = true;
        
        synchronized (this)
        {
            notifyAll();
        }
    }
    
    @Override
    public void run()
    {
        synchronized (this)
        {
            while (!stopThread)
                try
                {
                    wait();
                } catch (InterruptedException ex)
                {
                    stopThread = true;
                }
        }

        dispose();
    }
}