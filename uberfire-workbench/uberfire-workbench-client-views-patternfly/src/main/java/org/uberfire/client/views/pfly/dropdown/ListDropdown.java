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

package org.uberfire.client.views.pfly.dropdown;

import java.util.Iterator;

import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.DropDown;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.ListItem;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.constants.Toggle;
import org.gwtbootstrap3.client.ui.html.Span;

/**
 * Utility component for creating dropdown menus.
 * It also allows to determine if dropdown should shown when there is a single element in the list.
 *
 * Created by Cristiano Nicolai.
 */
public class ListDropdown extends DropDown {

    protected Anchor anchor = new Anchor();
    protected DropDownMenu dropDownMenu = new DropDownMenu();
    protected Span caret = new Span();
    protected boolean hideOnSingleElement = true;

    public ListDropdown() {
        super.add( anchor );
        super.add( dropDownMenu );
        caret.addStyleName( Styles.CARET );
        this.addStyleName( "uf-list-dropdown" );
    }

    @Override
    public void add( final Widget child ) {
        if( child instanceof ListItem ){
            dropDownMenu.add( child );
        } else {
            child.addStyleName( Styles.PULL_LEFT );
            anchor.add( child );
        }
        addCaretToText();
    }

    public void setHideOnSingleElement(boolean hide){
        this.hideOnSingleElement = hide;
    }

    @Override
    public void clear() {
        removeChildWidgets( anchor );
        removeChildWidgets( dropDownMenu );
    }

    private void removeChildWidgets(final ComplexPanel panel){
        final Iterator<Widget> iterator = panel.iterator();
        while( iterator.hasNext() ){
            iterator.next();
            iterator.remove();
        }
    }

    /**
     * Checks whether or not anchor should be added/removed
     */
    protected void addCaretToText() {
        if( hideOnSingleElement && dropDownMenu.getWidgetCount() == 1 && anchor.getWidgetIndex( caret ) != -1 ){
            anchor.remove( caret );
            anchor.setDataToggle( null );
        } else if( anchor.getWidgetIndex( caret ) == -1){
            anchor.add( caret );
            anchor.setDataToggle( Toggle.DROPDOWN );
            anchor.setDataTargetWidget( this );
        }
    }

}
