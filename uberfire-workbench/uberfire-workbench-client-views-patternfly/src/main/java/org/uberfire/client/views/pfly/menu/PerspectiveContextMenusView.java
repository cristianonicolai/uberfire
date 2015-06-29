package org.uberfire.client.views.pfly.menu;

import java.util.Iterator;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.security.shared.api.identity.User;
import org.uberfire.client.menu.AuthFilterMenuVisitor;
import org.uberfire.client.workbench.widgets.menu.PerspectiveContextMenusPresenter;
import org.uberfire.security.authz.AuthorizationManager;
import org.uberfire.workbench.model.menu.MenuPosition;
import org.uberfire.workbench.model.menu.Menus;

@ApplicationScoped
public class PerspectiveContextMenusView
        implements PerspectiveContextMenusPresenter.View, HasMenuItems {

    public static final String UF_PERSPECTIVE_CONTEXT_MENU = "uf-perspective-context-menu";

    @Inject
    private AuthorizationManager authzManager;

    @Inject
    private User identity;

    @Inject
    private WorkbenchMenuCompactNavBarView workbenchMenuCompactNavBarView;

    @Override
    public Widget asWidget() {
        return workbenchMenuCompactNavBarView;
    }

    @Override
    public void buildMenu( final Menus menus ) {
        clear();
        menus.accept( new AuthFilterMenuVisitor( authzManager, identity, new StackedDropdownMenuVisitor( this ) ) );
    }

    @Override
    public void addMenuItem( final MenuPosition position, final Widget menuContent ) {
        menuContent.addStyleName( UF_PERSPECTIVE_CONTEXT_MENU );
        workbenchMenuCompactNavBarView.add( menuContent );
    }

    @Override
    public void clear() {
        final Iterator<Widget> widgets = workbenchMenuCompactNavBarView.iterator();
        while ( widgets.hasNext() ) {
            final Widget child = widgets.next();

            if ( child.getElement().hasClassName( UF_PERSPECTIVE_CONTEXT_MENU ) ) {
                widgets.remove();
            }
        }
    }

}
