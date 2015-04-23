package org.uberfire.client.views.pfly.modal;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import org.gwtbootstrap3.client.shared.event.ModalHiddenEvent;
import org.gwtbootstrap3.client.shared.event.ModalHiddenHandler;
import org.gwtbootstrap3.client.shared.event.ModalShownEvent;
import org.gwtbootstrap3.client.shared.event.ModalShownHandler;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.ModalFooter;
import org.gwtbootstrap3.client.ui.constants.Attributes;
import org.gwtbootstrap3.client.ui.constants.ModalBackdrop;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.Commands;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;

import static org.uberfire.commons.validation.PortablePreconditions.checkNotNull;

/**
 * A modal dialog that floats above the workbench. Each instance can only be shown once.
 */
@Dependent
public class Bs3Modal extends Composite {

    private final Modal modal = new Modal();

    private final ModalBody body = new ModalBody();

    private final ModalFooter footer = new ModalFooter();

    /**
     * Used for enforcing the "only show one time" rule.
     */
    boolean hasBeenShown;

    /**
     * This is a CDI bean. It should be instantiated through the Errai bean manager.
     */
    public Bs3Modal() {
    }

    @PostConstruct
    void setup() {
        modal.add(body);
        modal.add(footer);

        modal.setDataBackdrop(ModalBackdrop.STATIC);
        modal.setFade(true);
        modal.getElement().setAttribute(Attributes.ROLE, "dialog");
        modal.getElement().setAttribute(Attributes.TABINDEX, "-1");

        final Button close = new Button("OK");
        close.getElement().setAttribute(Attributes.DATA_DISMISS, "modal");
        footer.add(close);

        final SimplePanel panel = new SimplePanel( modal );
        initWidget( panel );
    }

    /**
     * Shows this modal dialog above the current workbench.
     *
     * @param afterShown the action to perform once the dialog has been shown. Not null. Use {@link Commands#DO_NOTHING} if you don't have an "after show" action.
     * @param afterClosed the action to perform once the dialog has been dismissed. Not null. Use {@link Commands#DO_NOTHING} if you don't have an "after close" action.
     */
    public void show( final Command afterShown,
                      final Command afterClosed ) {
        if ( hasBeenShown ) {
            throw new IllegalStateException( "This modal has already been shown. Create a new instance if you want to show another modal." );
        }
        checkNotNull( "afterShown", afterShown );
        checkNotNull( "afterClosed", afterClosed );
        modal.addShownHandler( new ModalShownHandler() {
            @Override
            public void onShown( final ModalShownEvent showEvent ) {
                if ( afterShown != null ) {
                    afterShown.execute();
                }
            }
        } );
        modal.addHiddenHandler( new ModalHiddenHandler() {
            @Override
            public void onHidden( final ModalHiddenEvent hiddenEvent ) {
                if ( afterClosed != null ) {
                    afterClosed.execute();
                }
            }
        } );
        modal.show();
    }

    public void hide() {
        modal.hide();
    }

    /**
     * Replaces the contents within the main body area of the modal. By default, the main body area is empty.
     *
     * @param content the new content for the main body area.
     */
    public void setContent(IsWidget content) {
        body.clear();
        body.add(content);
    }

    public void setModalTitle(final String title){
        modal.setTitle(SafeHtmlUtils.htmlEscape(title));
    }

    /**
     * Replaces the current contents of the footer area with the given widget. By default (if you do not call this
     * method), the footer contains an OK button that dismisses the dialog when clicked.
     *
     * @param content the new content for the footer area.
     */
    public void setFooterContent(IsWidget content) {
        footer.clear();
        footer.add( content );
    }

    /**
     * Sets the pixel height of the main content container.
     */
    public void setBodyHeight( int height ) {
        body.setHeight( height + "px" );
    }

}
