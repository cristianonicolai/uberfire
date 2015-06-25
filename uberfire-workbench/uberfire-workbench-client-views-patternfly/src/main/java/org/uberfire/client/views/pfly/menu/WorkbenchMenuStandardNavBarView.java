/*
 *
 *  * Copyright 2012 JBoss Inc
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  * use this file except in compliance with the License. You may obtain a copy of
 *  * the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  * License for the specific language governing permissions and limitations under
 *  * the License.
 *
 */

package org.uberfire.client.views.pfly.menu;

import java.util.Stack;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.AnchorButton;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.ListDropDown;
import org.gwtbootstrap3.client.ui.NavbarNav;
import org.gwtbootstrap3.client.ui.base.AbstractListItem;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.constants.Toggle;
import org.jboss.errai.security.shared.api.identity.User;
import org.uberfire.client.menu.AuthFilterMenuVisitor;
import org.uberfire.client.workbench.widgets.menu.HasMenus;
import org.uberfire.client.workbench.widgets.menu.PespectiveContextMenusPresenter;
import org.uberfire.security.authz.AuthorizationManager;
import org.uberfire.workbench.model.menu.MenuCustom;
import org.uberfire.workbench.model.menu.MenuGroup;
import org.uberfire.workbench.model.menu.MenuItemCommand;
import org.uberfire.workbench.model.menu.MenuItemPerspective;
import org.uberfire.workbench.model.menu.MenuItemPlain;
import org.uberfire.workbench.model.menu.MenuPosition;
import org.uberfire.workbench.model.menu.Menus;

/**
 * Created by Cristiano Nicolai.
 */
@Dependent
public class WorkbenchMenuStandardNavBarView extends NavbarNav implements HasMenus, HasMenuItems {

    @Inject
    private PespectiveContextMenusPresenter.View perspectiveContextMenuView;

    @Inject
    private AuthorizationManager authzManager;

    @Inject
    private User identity;

    @PostConstruct
    protected void setup() {
        this.addStyleName( "navbar-primary" );
    }

    @Override
    public void addMenus( final Menus menus ) {
        menus.accept( new AuthFilterMenuVisitor( authzManager, identity, new StandardMenuVisitor( this ) ) );
    }

    @Override
    public void addMenuItem( MenuPosition position, final Widget menuContent ) {
        if ( position == null ) {
            position = MenuPosition.CENTER;
        }
        switch ( position ) {
            case LEFT:
                this.add( menuContent );
                break;
            case CENTER:
                this.insert( menuContent, WorkbenchMenuStandardNavBarView.this.getWidgetCount() - 1 );
            case RIGHT:
                menuContent.addStyleName( Styles.PULL_RIGHT );
                this.add( menuContent );
                break;
        }
    }

    private class StandardMenuVisitor extends DropdownMenuVisitor {

        final Stack<HasMenuItems> parentMenus = new Stack<HasMenuItems>();

        public StandardMenuVisitor( final HasMenuItems hasMenuItems ) {
            super( hasMenuItems );
        }

        @Override
        public boolean visitEnter( final Menus menus ) {
            parentMenus.push( hasMenuItems );
            return true;
        }

        @Override
        public void visitLeave( final Menus menus ) {
            parentMenus.pop();
        }

        @Override
        public boolean visitEnter( final MenuGroup menuGroup ) {
            final ListDropDown listDropDown = new ListDropDown();
            final AnchorButton anchor = new AnchorButton();
            anchor.setDataToggle( Toggle.DROPDOWN );
            anchor.setText( menuGroup.getCaption() );
            final DropDownMenu dropDownMenu = new DropDownMenu();
            listDropDown.add( anchor );
            listDropDown.add( dropDownMenu );
            final HasMenuItems hasMenuItems = new HasMenuItems() {
                @Override
                public void addMenuItem( final MenuPosition position, final Widget menuContent ) {
                    dropDownMenu.add( menuContent );
                }

                @Override
                public Widget asWidget() {
                    return listDropDown;
                }
            };
            parentMenus.push( hasMenuItems );
            return true;
        }

        @Override
        public void visitLeave( final MenuGroup menuGroup ) {
            final HasMenuItems groupMenu = parentMenus.pop();
            parentMenus.peek().addMenuItem( menuGroup.getPosition(), groupMenu.asWidget() );
        }

        @Override
        public void visit( final MenuCustom<?> menuCustom ) {
            final IsWidget customMenuItem = ( (IsWidget) menuCustom.build() ).asWidget();
            if ( customMenuItem instanceof AbstractListItem ) {
                final AbstractListItem view = (AbstractListItem) customMenuItem;
                parentMenus.peek().addMenuItem( menuCustom.getPosition(), view );
            } else {
                buildMenuCustom( menuCustom, parentMenus.peek() );
            }
        }

        @Override
        public void visit( final MenuItemCommand menuItemCommand ) {
            buildMenuCommand( menuItemCommand, parentMenus.peek() );
        }

        @Override
        public void visit( final MenuItemPlain menuItemPlain ) {
            buildMenuPlain( menuItemPlain, parentMenus.peek() );
        }

        @Override
        public void visit( final MenuItemPerspective menuItemPerspective ) {
            buildMenuPerspective( menuItemPerspective, parentMenus.peek() );
        }
    }
}

