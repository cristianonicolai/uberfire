/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.uberfire.client.views.pfly.menu;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.DropDown;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Navbar;
import org.gwtbootstrap3.client.ui.NavbarBrand;
import org.gwtbootstrap3.client.ui.NavbarCollapse;
import org.gwtbootstrap3.client.ui.NavbarHeader;
import org.gwtbootstrap3.client.ui.NavbarNav;
import org.gwtbootstrap3.client.ui.base.AbstractListItem;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.constants.Toggle;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Text;
import org.jboss.errai.ioc.client.container.IOCResolutionException;
import org.jboss.errai.security.shared.api.identity.User;
import org.uberfire.client.workbench.widgets.menu.WorkbenchMenuBarPresenter;
import org.uberfire.security.authz.AuthorizationManager;
import org.uberfire.workbench.model.menu.MenuPosition;
import org.uberfire.workbench.model.menu.Menus;

/**
 * The Menu Bar widget
 */
@ApplicationScoped
public class WorkbenchMenuBarView extends Composite implements WorkbenchMenuBarPresenter.View {

    @Inject
    private AuthorizationManager authzManager;

    @Inject
    private User identity;

    @Inject
    private Instance<MainBrand> menuBarBrand;

    private final NavbarNav primaryNavBar = new NavbarNav();

    private final NavbarNav navBar = new NavbarNav();

    private final DropDown menuItemDropDown = new DropDown();

    private final DropDownMenu menuItemDropDownMenu = new DropDownMenu();

    @Inject
    private UtilityNavbar utilityNavbar;

    @PostConstruct
    private void setup() {
        final Navbar root = new Navbar();
        root.addStyleName( "navbar-pf" );

        final NavbarHeader headerContainer = new NavbarHeader();
        try {
            final NavbarBrand brand = new NavbarBrand();
            brand.add(menuBarBrand.get());
            headerContainer.add(brand);
            final Span divider = new Span();
            divider.addStyleName( "divider-vertical" );
            headerContainer.add( divider );
        } catch ( IOCResolutionException e ) {
            // app didn't provide a branded header bean
        }
        root.add( headerContainer );

        primaryNavBar.addStyleName( "navbar-primary" );
        primaryNavBar.addStyleName( "uf-menu-compact" );

        final NavbarCollapse collapsibleContainer = new NavbarCollapse();
        collapsibleContainer.add( utilityNavbar );
//        collapsibleContainer.add( navBar );
        collapsibleContainer.add( primaryNavBar );

        root.add( collapsibleContainer );

        final Button btnToggle = new Button();
        btnToggle.removeStyleName( "btn-default" );
        btnToggle.addStyleName( Styles.NAVBAR_TOGGLE );
        btnToggle.setDataToggle( Toggle.COLLAPSE );
        btnToggle.setDataTargetWidget( collapsibleContainer );
        final Icon icon = new Icon( IconType.BARS );
        icon.addStyleName( "fa-inverse" );
        btnToggle.add( icon );
        headerContainer.add( btnToggle );

        final Anchor anchor = new Anchor();
        anchor.add( new Text( "Menus" ) );
        final Span caret = new Span();
        caret.addStyleName( Styles.CARET );
        anchor.add( caret );
        anchor.setDataToggle( Toggle.DROPDOWN );
        anchor.setDataTargetWidget( menuItemDropDown );
        menuItemDropDown.add( anchor );
        menuItemDropDown.add( menuItemDropDownMenu );
        navBar.add( menuItemDropDown );

        initWidget( root );
    }

    @Override
    public void addMenuItems( final Menus menus ) {
        HasMenuItems topLevelContainer = new HasMenuItems() {

            @Override
            public Widget asWidget() {
                return WorkbenchMenuBarView.this;
            }

            @Override
            public int getMenuItemCount() {
                return primaryNavBar.getWidgetCount() + utilityNavbar.getWidgetCount();
            }

            @Override
            public void addMenuItem( MenuPosition position,
                                     AbstractListItem menuContent ) {
                if(menuContent instanceof UtilityMenu){
                    utilityNavbar.add( menuContent );
                } else {
                    if ( position == null ) {
                        position = MenuPosition.CENTER;
                    }
                    switch (position) {
                        case LEFT:
                            primaryNavBar.add(menuContent);
//                            menuItemDropDownMenu.add( menuContent );
                            break;
                        case CENTER:
                            primaryNavBar.insert(menuContent, primaryNavBar.getWidgetCount() - 1);
                        case RIGHT:
                            menuContent.addStyleName(Styles.PULL_RIGHT);
                            primaryNavBar.add(menuContent);
                            break;
                    }
                }
            }
        };
        Bs3Menus.constructMenuView( menus, authzManager, identity, topLevelContainer );
    }

    @Override
    public void clear() {
        primaryNavBar.clear();
        utilityNavbar.clear();
    }

    public void expand(){
        primaryNavBar.removeStyleName( "hide" );
    }

    public void collapse(){
        primaryNavBar.addStyleName( "hide" );
    }

}
