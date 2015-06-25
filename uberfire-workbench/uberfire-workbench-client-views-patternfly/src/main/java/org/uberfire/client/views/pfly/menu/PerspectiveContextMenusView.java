package org.uberfire.client.views.pfly.menu;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.AnchorButton;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.ListDropDown;
import org.gwtbootstrap3.client.ui.constants.Toggle;
import org.gwtbootstrap3.client.ui.html.Text;
import org.jboss.errai.security.shared.api.identity.User;
import org.uberfire.client.menu.AuthFilterMenuVisitor;
import org.uberfire.client.workbench.widgets.menu.PespectiveContextMenusPresenter;
import org.uberfire.security.authz.AuthorizationManager;
import org.uberfire.workbench.model.menu.MenuGroup;
import org.uberfire.workbench.model.menu.MenuPosition;
import org.uberfire.workbench.model.menu.Menus;

@ApplicationScoped
public class PerspectiveContextMenusView
        extends Composite
        implements PespectiveContextMenusPresenter.View, HasMenuItems {

    @Inject
    private AuthorizationManager authzManager;

    @Inject
    private User identity;

    private final AnchorButton anchor = new AnchorButton();
    private final Text text = new Text();
    private final DropDownMenu dropDownMenu = new DropDownMenu();
    private final ListDropDown listDropDown = new ListDropDown();

    public PerspectiveContextMenusView() {
        anchor.setDataToggle( Toggle.DROPDOWN );
        anchor.add( text );
        listDropDown.add( anchor );
        listDropDown.add( dropDownMenu );

        initWidget( listDropDown );
    }

    @Override
    public void buildMenu( final Menus menus ) {
        clear();
        menus.accept( new AuthFilterMenuVisitor( authzManager, identity, new PerspectiveContextMenuVisitor( this ) ) );
        this.setVisible( true );
    }

    @Override
    public void addMenuItem( final MenuPosition position, final Widget menuContent ) {
        dropDownMenu.add( menuContent );
    }

    @Override
    public void clear() {
        text.setText( null );
        dropDownMenu.clear();
        this.setVisible( false );
    }

    private class PerspectiveContextMenuVisitor extends DropdownMenuVisitor {

        public PerspectiveContextMenuVisitor( final HasMenuItems hasMenuItems ) {
            super( hasMenuItems );
        }

        @Override
        public boolean visitEnter( final Menus menus ) {
            if ( menus.getItems().size() == 1 && menus.getItems().get( 0 ) instanceof MenuGroup ) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean visitEnter( final MenuGroup menuGroup ) {
            text.setText( menuGroup.getCaption() );
            return true;
        }

    }
}
