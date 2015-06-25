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
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Navbar;
import org.gwtbootstrap3.client.ui.NavbarBrand;
import org.gwtbootstrap3.client.ui.NavbarCollapse;
import org.gwtbootstrap3.client.ui.NavbarHeader;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.NavbarType;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.constants.Toggle;
import org.jboss.errai.ioc.client.container.IOCResolutionException;
import org.uberfire.client.workbench.widgets.menu.WorkbenchMenuBarPresenter;
import org.uberfire.workbench.model.menu.MenuItem;
import org.uberfire.workbench.model.menu.Menus;

/**
 * The Menu Bar widget
 */
@ApplicationScoped
public class WorkbenchMenuBarView extends Composite implements WorkbenchMenuBarPresenter.View {

    @Inject
    private Instance<MainBrand> menuBarBrand;

    private final Navbar navBar = new Navbar();

    @Inject
    private WorkbenchMenuCompactNavBarView workbenchMenuCompactNavBarView;

    @Inject
    private WorkbenchMenuStandardNavBarView workbenchMenuStandardNavBarView;

    @Inject
    private UtilityMenuBarView utilityMenuBarView;

    @PostConstruct
    protected void setup() {
        navBar.setType( NavbarType.INVERSE );
        navBar.addStyleName( "navbar-pf" );

        final NavbarHeader headerContainer = new NavbarHeader();
        try {
            final NavbarBrand brand = new NavbarBrand();
            brand.add(menuBarBrand.get());
            headerContainer.add(brand);
        } catch ( IOCResolutionException e ) {
            // app didn't provide a branded header bean
        }
        navBar.add( headerContainer );

        final NavbarCollapse collapsibleContainer = new NavbarCollapse();
        collapsibleContainer.add( workbenchMenuCompactNavBarView );
        collapsibleContainer.add( workbenchMenuStandardNavBarView );
        collapsibleContainer.add( utilityMenuBarView );

        navBar.add( collapsibleContainer );

        final Button btnToggle = new Button();
        btnToggle.removeStyleName( "btn-default" );
        btnToggle.addStyleName( Styles.NAVBAR_TOGGLE );
        btnToggle.setDataToggle( Toggle.COLLAPSE );
        btnToggle.setDataTargetWidget( collapsibleContainer );
        final Icon icon = new Icon( IconType.BARS );
        icon.addStyleName( "fa-inverse" );
        btnToggle.add( icon );
        headerContainer.add( btnToggle );

        initWidget( navBar );

        expand();
    }

    @Override
    public void addMenuItems( final Menus menus ) {
        workbenchMenuStandardNavBarView.addMenus( menus );
        workbenchMenuCompactNavBarView.addMenus( menus );
    }

    @Override
    public void clear() {
        workbenchMenuStandardNavBarView.clear();
        workbenchMenuCompactNavBarView.clear();
        utilityMenuBarView.clear();
    }

    @Override
    public void expand(){
        navBar.removeStyleName( "uf-navbar-compact" );
    }

    @Override
    public void collapse(){
        navBar.addStyleName( "uf-navbar-compact" );
    }

    @Override
    public void selectMenu( final MenuItem menu ) {

    }
}
