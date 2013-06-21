package pt.ua.ieeta.geneoptimizer.GUI.RedesignPanel;

/**
 *
 * @author Paulo Gaspar
 */

import java.awt.Component;
import java.awt.LayoutManager;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.layout.*;
import net.miginfocom.swing.MigLayout;
import nu.epsilon.rss.ui.components.NanoSource;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;

public class AccordionPanel extends JPanel implements TimingTarget, AccordionListener 
{

    private static final int DEFAULT_DURATION = 350;
    private static final long serialVersionUID = -5387970285143913716L;
    private Animator animator;
    private float progress;
    int animationDuration;

    public AccordionPanel() {
        this(DEFAULT_DURATION);
    }

    public AccordionPanel(int animationDuration) 
    {
        setLayout(createLayout());
        this.animationDuration = animationDuration;
        this.animator = createAnimator();
    }

    private Animator createAnimator() 
    {
        Animator anim = new Animator(animationDuration, this);
        anim.setTimer(new NanoSource());
        anim.setResolution(5);
        anim.setAcceleration(0.4F);
        anim.setDeceleration(0.4F);
        return anim;
    }

    private LayoutManager createLayout() 
    {
        LC lc = new LC().wrap().fillX().insetsAll("0").gridGapX("0");
        final MigLayout layout = new MigLayout(lc);
        layout.addLayoutCallback(new AccordionLayoutCallback());
        
        return layout;
        
//        BoxLayout box = new BoxLayout(this, BoxLayout.Y_AXIS);
//        
//        return box;
    }

    private static BoundSize[] getSize(final Component component) {
        return getSize(component.getPreferredSize().height + "!");
    }

    private static BoundSize[] getSize(final String values) {
        final BoundSize size = ConstraintParser.parseBoundSize(values, false, false);
        return new BoundSize[]{null, size};
    }

    @Override
    public Component add(final String title, final Component component) {
        add(title, component, false);
        return component;
    }

    public void add(final Component title, final Component component) {
        add(title, component, false);
    }

    public void add(final String title, final Component component,
            boolean open) {
        add(new JLabel(title), component, open);
    }

    public void add(final Component title, final Component component, boolean open) {
        add(new AccordionComponent(title, component, open));
    }

    public void add(final AccordionComponent component) {
        component.addListener(this);
        add(component, "growx");
    }

    private void setProgress(final float progress) {
        this.progress = progress;
    }

    private float getProgress() {
        return this.progress;
    }

    private float getProgress(final boolean opening) {
        return opening ? getProgress() : 1 - getProgress();
    }

    // ///////////////////// //
    // Timing target methods //
    // ///////////////////// //
    @Override
    public void begin() 
    {
    }

    @Override
    public void end() 
    {
    }

    @Override
    public void repeat() {
    }

    @Override
    public void timingEvent(final float progress) 
    {
        setProgress(progress);
//
//        for (final Component component : getComponents()) 
//        {
//            if (!(component instanceof AccordionComponent))
//                continue;
//            
//            final AccordionComponent acc = (AccordionComponent) component;
//            
//            acc.setPreferredSize(new Dimension(acc.getWidth(), 13));
////            
////            if (acc.isOpening() || acc.isClosing())
////            {
////                final float prog = getProgress(acc.isOpening());
////                
////                final int componentHeight = acc.getContent().getPreferredSize().height;
////                final int labelHeight = acc.getLabel().getPreferredSize().height;
////                final int height = Math.round(componentHeight * prog) + labelHeight;
////                acc.setPreferredSize(new Dimension(acc.getWidth(), height));
////            }
//        }
//
//         /******************************************************/
         
        doLayout();
        repaint();

        final boolean done = progress == 1F;

        if (!done) {
            return;
        }

        for (final Component component : getComponents()) 
        {
            if (!(component instanceof AccordionComponent)) {
                continue;
            }

            AccordionComponent accordionComponent = ((AccordionComponent) component);
            if (accordionComponent.isAnimating()) {
                accordionComponent.finishAnimation();
            }
            accordionComponent.setLocked(false);
        }
    }

    @Override
    public void clicked(final AccordionComponent accordionComponent) 
    {
        prepareOpen(accordionComponent);
        
        if (!animator.isRunning()) {
            animator.start();
        }
    }

    private void prepareOpen(final AccordionComponent accordionComponent) 
    {
        for (final Component component : getComponents()) 
        {
            if (!(component instanceof AccordionComponent))
                continue;
            
            final AccordionComponent acc = (AccordionComponent) component;
            if (acc != accordionComponent && acc.isOpen()) 
                acc.setClosing();

            acc.setLocked(true);
        }
    }

    private final class AccordionLayoutCallback extends LayoutCallback 
    {
        @Override
        public BoundSize[] getSize(final ComponentWrapper comp) {
            if (!(comp.getComponent() instanceof AccordionComponent)) {
                return null;
            }

            final AccordionComponent component = (AccordionComponent) comp.getComponent();

            if (component.isOpen()) {
                return AccordionPanel.getSize(component);
            }

            if (component.isClosed()) {
                return AccordionPanel.getSize(component.getLabel());
            }

            // Animating
            return getAnimatingSize(component);
        }

        private BoundSize[] getAnimatingSize(final AccordionComponent component) 
        {
            final float progress = getProgress(component.isOpening());

            final int componentHeight = component.getContent().getPreferredSize().height;
            final int labelHeight = component.getLabel().getPreferredSize().height;
            final int height = Math.round(componentHeight * progress) + labelHeight;

            return AccordionPanel.getSize(height + "!");
        }
    }
}

