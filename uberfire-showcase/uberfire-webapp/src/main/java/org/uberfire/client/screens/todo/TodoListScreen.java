/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.uberfire.client.screens.todo;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.VFSService;
import org.uberfire.client.annotations.DefaultPosition;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.workbench.model.CompassPosition;
import org.uberfire.workbench.model.Position;

@Dependent
@WorkbenchScreen(identifier = "TodoListScreen", preferredWidth = 450)
public class TodoListScreen {

    private static final String EMPTY = "<p>-- empty --</p>";

    @Inject
    private Caller<VFSService> vfsServices;

    private HTML markdown = new HTML(EMPTY);

    @PostConstruct
    public void init() {
        vfsServices.call(new RemoteCallback<Path>() {
            @Override
            public void callback(final Path o) {
                vfsServices.call(new RemoteCallback<String>() {
                    @Override
                    public void callback(final String response) {

                        if (response == null) {
                            setContent(EMPTY);
                        } else {
                            try {
                                setContent(parseMarkdown(response));
                            } catch (Exception e) {
                                setContent(EMPTY);
                                GWT.log("Error parsing markdown content", e);
                            }
                        }
                    }
                }).readAllString(o);
            }
        }).get("default://uf-playground/todo.md");
    }

    @WorkbenchPartView
    public IsWidget getView() {
        return markdown;
    }

    private void setContent(final String content) {
        this.markdown.setHTML(content);
    }

    public static native String parseMarkdown(String content)/*-{
        return $wnd.marked(content);
    }-*/;

    @DefaultPosition
    public Position getDefaultPosition() {
        return CompassPosition.EAST;
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return "Todo List";
    }

}