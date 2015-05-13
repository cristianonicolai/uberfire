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

package org.uberfire.client.screens;

import com.google.gwt.user.client.ui.Label;
import org.uberfire.client.annotations.WorkbenchEditor;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.workbench.type.AnyResourceType;

@WorkbenchEditor(identifier = "SampleWorkbenchEditor", supportedTypes = AnyResourceType.class)
public class SampleWorkbenchEditor {

    private final Label view = new Label("This screen is always displayed as editor.");

    @WorkbenchPartTitle
    public String getTitle() {
        return "Sample Workbench Editor";
    }

    @WorkbenchPartView
    public Label getView() {
        return view;
    }
}
