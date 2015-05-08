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

package org.uberfire.client.views.pfly.tab;

import javax.enterprise.context.Dependent;

import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.base.HasActive;
import org.uberfire.client.resources.WorkbenchResources;
import org.uberfire.client.util.Layouts;

@Dependent
public class ResizeTabPanel extends TabPanelWithDropdowns implements RequiresResize, ProvidesResize {

    @Override
    public void onResize() {
        Layouts.setToFillParent(tabContent);

        // TabContent is just a container for all the TabPane divs, one of which is made visible at a time.
        // For compatibility with GWT LayoutPanel, we have to set both layers of children to fill their parents.
        // We do it in onResize() to get to the TabPanes no matter how they were added.
        for (Widget child : tabContent) {
            Layouts.setToFillParent(child);
            if (((HasActive) child).isActive()) {
                child.setPixelSize(getOffsetWidth(), getOffsetHeight() - getTabBarHeight());
                if (child instanceof RequiresResize) {
                    ((RequiresResize) child).onResize();
                }
            }
        }
    }

    /**
     * Returns the height (in pixels) taken up by the tab bar.
     */
    public int getTabBarHeight() {
        return tabBar.getOffsetHeight();
    }

    /**
     * Makes the tab panel look more or less prominent.
     * @param hasFocus if true, the tab panel will look more prominent. If false, the tab panel will look normal.
     */
    public void setFocus(boolean hasFocus) {
        if (hasFocus) {
            tabBar.addStyleName(WorkbenchResources.INSTANCE.CSS().activeNavTabs());
        } else {
            tabBar.removeStyleName(WorkbenchResources.INSTANCE.CSS().activeNavTabs());
        }
    }

}
