package pt.ua.ieeta.geneoptimizer.GUI.RedesignPanel;

/**
 *
 * @author Paulo Gaspar
 */

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class AccordionComponent extends JPanel {

    private static final Cursor HAND = new Cursor(Cursor.HAND_CURSOR);
    private static final long serialVersionUID = 8466799296915323080L;
    private boolean hideComponents = false;

    protected enum State {
        OPENING, OPEN, CLOSING, CLOSED;
    };
    private List<AccordionListener> listeners = new LinkedList<AccordionListener>();
    private Component label;
    private Component content;
    private State state;
    private BufferedImage animationBuffer;
    private boolean locked;

    public AccordionComponent(String label, Component component) {
        this(label, component, false);
    }

    public AccordionComponent(String label, Component component, boolean open) {
        this(new JLabel(label), component, open);
    }

    public AccordionComponent(Component label, Component component) {
        this(label, component, false);
    }

    public AccordionComponent(Component label, Component component, boolean open) 
    {
        label.addMouseListener(new MouseAdapter() 
        {
            @Override
            public void mouseReleased(MouseEvent e) 
            {
                // Prevent clicks when other components are animating
                if (isLocked()) return;

                setState(getNextState(getState()));

                // Notify listeners (parent)
                notifyActivated();
            }
        });

        setOpaque(false);
        label.setCursor(HAND);
        setLabel(label);
        setContent(component);

        setLayout(new BorderLayout());
        add(getLabel(), BorderLayout.NORTH);
        add(getContent(), BorderLayout.CENTER);
        initState(open);
    }

    private void initState(boolean open) {
        if (open) {
            setOpen();
            return;
        }

        setClosed();
    }

    public Component getLabel() {
        return label;
    }

    private void setLabel(Component label) {
        this.label = label;
    }

    public Component getContent() {
        return content;
    }

    public void setContent(Component content) {
        this.content = content;
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Override
    public void paint(Graphics g) {
        if (!isAnimating()) {
            super.paint(g);
            return;
        }

        g.drawImage(animationBuffer, 0, 0, null);
    }

    private void initAnimationBuffer() {
        animationBuffer = new BufferedImage(getWidth(),
                getPreferredSize().height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = (Graphics2D) animationBuffer.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

        Dimension oldSize = getSize();

        if (isClosed()) {
            setSize(new Dimension(getWidth(), getPreferredSize().height));
            validate();
        }

        // "Print" the component onto the animation buffer
        super.print(g);

        if (isClosed()) {
            setSize(oldSize);
        }
    }

    protected State getState() {
        return this.state;
    }

    protected void setState(State state) {
        if (!isAnimating() && isAnimating(state)) {
            prepareAnimation();
        }

        this.state = state;
    }

    /**
     * Returns the state this component should change into when it's clicked,
     * depending on the provided state.
     *
     * @return the next state
     */
    private static State getNextState(State state) {
        if (state == State.OPEN || state == State.OPENING) {
            return State.CLOSING;
        }

        return State.OPENING;
    }

    public boolean isOpen() {
        return isState(State.OPEN);
    }

    public boolean isClosed() {
        return isState(State.CLOSED);
    }

    public boolean isOpening() {
        return isState(State.OPENING);
    }

    public boolean isClosing() {
        return isState(State.CLOSING);
    }

    public boolean isAnimating() {
        return isAnimating(getState());
    }

    private boolean isAnimating(State state) {
        return state == State.OPENING || state == State.CLOSING;
    }

    public void setOpening() {
        setState(State.OPENING);
    }

    public void setOpen() {
        setState(State.OPEN);
    }

    public void setClosing() {
        setState(State.CLOSING);
    }

    public void setClosed() {
        setState(State.CLOSED);
    }

    private boolean isState(State state) {
        return this.state == state;
    }

    private void prepareAnimation() {
        if (isAnimationBufferDirty()) {
            initAnimationBuffer();
        }

        // Hide contents to avoid flickering when the buffered image is painted
        setContentsVisible(false);
    }

    private boolean isAnimationBufferDirty() {
        return isOpen() || animationBuffer == null || animationBuffer.getWidth() != getWidth();
    }

    public void finishAnimation() 
    {
        setContentsVisible(true);

        if (isOpening()) {
            setOpen();
        }

        if (isClosing()) {
            setClosed();
        }
    }

    private void setContentsVisible(boolean visible) {
        if (hideComponents) {
            getLabel().setVisible(true);
            getContent().setVisible(visible);
        } else {
            validate();
        }
    }

    // //////////////// //
    // Listener methods //
    // //////////////// //
    public void addListener(AccordionListener listener) {
        listeners.add(listener);
    }

    public void removeListener(AccordionListener listener) {
        listeners.remove(listener);
    }

    private void notifyActivated() {
        for (AccordionListener listener : listeners) {
            listener.clicked(this);
        }
    }
}

