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
package org.uberfire.client.views.pfly.listbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.DropDown;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.ListItem;
import org.gwtbootstrap3.client.ui.NavbarLink;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.PanelBody;
import org.gwtbootstrap3.client.ui.PanelHeader;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.constants.Toggle;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Strong;
import org.gwtbootstrap3.client.ui.html.Text;
import org.jboss.errai.ioc.client.container.IOCResolutionException;
import org.jboss.errai.security.shared.api.identity.User;
import org.uberfire.client.util.Layouts;
import org.uberfire.client.views.pfly.maximize.MaximizeToggleButton;
import org.uberfire.client.workbench.PanelManager;
import org.uberfire.client.workbench.panels.MaximizeToggleButtonPresenter;
import org.uberfire.client.workbench.panels.WorkbenchPanelPresenter;
import org.uberfire.client.workbench.part.WorkbenchPartPresenter;
import org.uberfire.client.workbench.widgets.dnd.DragArea;
import org.uberfire.client.workbench.widgets.dnd.WorkbenchDragAndDropManager;
import org.uberfire.client.workbench.widgets.listbar.ListBarWidget;
import org.uberfire.client.workbench.widgets.listbar.ListbarPreferences;
import org.uberfire.client.workbench.widgets.listbar.ResizeFocusPanel;
import org.uberfire.commons.data.Pair;
import org.uberfire.mvp.Command;
import org.uberfire.security.authz.AuthorizationManager;
import org.uberfire.workbench.model.PartDefinition;
import org.uberfire.workbench.model.menu.EnabledStateChangeListener;
import org.uberfire.workbench.model.menu.MenuCustom;
import org.uberfire.workbench.model.menu.MenuGroup;
import org.uberfire.workbench.model.menu.MenuItem;
import org.uberfire.workbench.model.menu.MenuItemCommand;

import static com.google.gwt.dom.client.Style.Display.*;

/**
 * Implementation of ListBarWidget based on PatternFly components.
 */
@Dependent
public class ListBarWidgetImpl
        extends ResizeComposite implements ListBarWidget {

    /**
     * When a part is added to the list bar, a special title widget is created for it. This title widget is draggable.
     * To promote testability, the draggable title widget is given a predictable debug ID of the form
     * {@code DEBUG_ID_PREFIX + DEBUG_TITLE_PREFIX + partName}.
     * <p>
     * Note that debug IDs are only assigned when the app inherits the GWT Debug module. See
     * {@link Widget#ensureDebugId(com.google.gwt.dom.client.Element, String)} for details.
     */
    public static final String DEBUG_TITLE_PREFIX = "ListBar-title-";

    interface ListBarWidgetBinder
            extends
            UiBinder<ResizeFocusPanel, ListBarWidgetImpl> {

    }

    private static ListBarWidgetBinder uiBinder = GWT.create( ListBarWidgetBinder.class );

    /**
     * Preferences bean that applications can optionally provide. If this injection is unsatisfied, default settings are used.
     */
    @Inject
    Instance<ListbarPreferences> optionalListBarPrefs;

    @Inject
    PanelManager panelManager;

    @Inject
    private AuthorizationManager authzManager;

    @Inject
    private User identity;

    @UiField
    FocusPanel container;

    @UiField
    Anchor titleAnchor;

    @UiField
    DropDown titleDropDown;

    @UiField
    Heading titleHeading;

//    @UiField
//    Button contextDisplay;

    @UiField
    PanelHeader header;

    @UiField
    Panel panel;

    @UiField
    ButtonGroup contextMenu;

    @UiField
    AnchorListItem closeButton;

    @UiField
    AnchorListItem drag;

    @UiField
    ButtonGroup toolBar;

    @UiField
    DropDownMenu toolBarDropDownMenu;
//
//    @UiField
//    Anchor dropdownCaret;
//
//    @UiField
//    ButtonGroup dropdownCaretContainer;
//
    @UiField
    DropDownMenu titleDropdownMenu;
//
//    @UiField
//    ButtonGroup closeButtonContainer;
//
    @UiField
    MaximizeToggleButton maximizeButton;

    /** Wraps maximizeButton, which is the view. */
    MaximizeToggleButtonPresenter maximizeButtonPresenter;

    DragArea dragHandle;

    @UiField
    PanelBody content;

    @UiField
    DropDown toolBarDropDown;

    @UiField
    Anchor toolBarDropDownMenuButton;

//    @UiField
//    FlowPanel menuArea;

    WorkbenchPanelPresenter presenter;

    private WorkbenchDragAndDropManager dndManager;

    private final Map<PartDefinition, FlowPanel> partContentView = new HashMap<PartDefinition, FlowPanel>();
    private final Map<PartDefinition, Widget> partTitle = new HashMap<PartDefinition, Widget>();
    LinkedHashSet<PartDefinition> parts = new LinkedHashSet<PartDefinition>();

    boolean isMultiPart = true;
    boolean isDndEnabled = true;
    Pair<PartDefinition, FlowPanel> currentPart;

    @PostConstruct
    void postConstruct() {
        initWidget( uiBinder.createAndBindUi( this ) );
        maximizeButtonPresenter = new MaximizeToggleButtonPresenter( maximizeButton );
        setupEventHandlers();
        setup( true, true );

        Layouts.setToFillParent( this );
        scheduleResize();
    }

    void setupEventHandlers(){
        this.container.addMouseOutHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                toolBarDropDown.removeStyleName("open");
            }
        });

        this.container.addFocusHandler(new FocusHandler() {
            @Override
            public void onFocus(FocusEvent event) {
                if (currentPart != null && currentPart.getK1() != null) {
                    selectPart(currentPart.getK1());
                }
            }
        });

        this.maximizeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (maximizeButton.isMaximized()) {
                    toolBar.clear();
                    maximizeButton.addStyleName(Styles.BTN);
                    maximizeButton.addStyleName("btn-default");
                    maximizeButton.addStyleName("btn-sm");
                    toolBar.add(maximizeButton);
                } else {
                    toolBar.clear();
                    toolBar.add(toolBarDropDown);
                    maximizeButton.removeStyleName(Styles.BTN);
                    maximizeButton.removeStyleName("btn-default");
                    maximizeButton.removeStyleName("btn-sm");
                    toolBarDropDownMenu.insert(maximizeButton, 0);
                }
            }
        });

    }

    @Override
    public void setup( boolean isMultiPart,
                       boolean isDndEnabled ) {
        this.isMultiPart = isMultiPart;
        this.isDndEnabled = isDndEnabled;

//        this.menuArea.setVisible( false );
        if ( isMultiPart ) {
            closeButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (currentPart != null) {
                        panelManager.closePart(currentPart.getK1());
                    }
                }
            });
        } else {
//            dropdownCaretContainer.setVisible( false );
//            closeButtonContainer.setVisible( false );
        }

        container.addFocusHandler( new FocusHandler() {
            @Override
            public void onFocus( FocusEvent event ) {
                if ( currentPart != null && currentPart.getK1() != null ) {
                    selectPart( currentPart.getK1() );
                }
            }
        } );

//        if ( isPropertyListbarContextDisable() ) {
//            contextDisplay.removeFromParent();
//        }

//        if(isDndEnabled){
//            dragHandle = new DragArea(drag);
//           and  initWidget(dragHandle);
//            dndManager.makeDraggable( currentPart, drag );
//            drag.addClickHandler(new ClickHandler() {
//                @Override
//                public void onClick(ClickEvent event) {
//                    GWT.log("on click!");
//                }
//            });
//        }
    }

    boolean isPropertyListbarContextDisable() {
        if ( optionalListBarPrefs.isUnsatisfied() ) {
            return true;
        }

        // as of Errai 3.0.4.Final, Instance.isUnsatisfied() always returns false. The try-catch is a necessary safety net.
        try {
            return optionalListBarPrefs.get().isContextEnabled();
        } catch ( IOCResolutionException e ) {
            return true;
        }
    }

    @Override
    public void enableDnd() {
        this.isDndEnabled = true;
    }

    @Override
    public void setExpanderCommand( final Command command ) {
//        if ( !isPropertyListbarContextDisable() ) {
//            contextDisplay.addClickHandler( new ClickHandler() {
//                @Override
//                public void onClick( ClickEvent event ) {
//                    command.execute();
//                }
//            } );
//        }
    }

    @Override
    public void setPresenter( final WorkbenchPanelPresenter presenter ) {
        this.presenter = presenter;
    }

    @Override
    public void setDndManager( final WorkbenchDragAndDropManager dndManager ) {
        this.dndManager = dndManager;
    }

    @Override
    public void clear() {
        contextMenu.clear();
        header.clear();
        panel.clear();
        titleAnchor.clear();
        content.clear();

        parts.clear();
        partContentView.clear();
        partTitle.clear();
        titleDropdownMenu.clear();
        currentPart = null;
    }

    @Override
    public void addPart( final WorkbenchPartPresenter.View view ) {
        final PartDefinition partDefinition = view.getPresenter().getDefinition();
        if ( parts.contains( partDefinition ) ) {
            selectPart( partDefinition );
            return;
        }

        parts.add( partDefinition );

        final FlowPanel panel = new FlowPanel();
        Layouts.setToFillParent( panel );
        panel.add( view );
        content.add( panel );

        // IMPORTANT! if you change what goes in this map, update the remove(PartDefinition) method
        partContentView.put( partDefinition, panel );

        final Widget title = buildTitle( view.getPresenter().getTitle(), view.getPresenter().getTitleDecoration() );
        partTitle.put( partDefinition, title );
        title.ensureDebugId( DEBUG_TITLE_PREFIX + view.getPresenter().getTitle() );

        if ( isDndEnabled ) {
            dndManager.makeDraggable( view, title );
        }

        scheduleResize();
    }

    private void updateBreadcrumb( final PartDefinition partDefinition ) {
//        this.title.clear();
//
//        final Widget title = partTitle.get( partDefinition );
//        this.title.add( title );
    }

    private Widget buildTitle( final String title , final IsWidget titleDecoration) {
        final String titleWidget = (titleDecoration instanceof Image) ? titleDecoration.toString() : "";
        final Text text = new Text( SafeHtmlUtils.htmlEscape( titleWidget + " " + title ) );
        if( isMultiPart ){
            final FlowPanel panel = new FlowPanel();
            panel.add( text );
            final Span caret = new Span();
            caret.addStyleName(Styles.CARET);
            panel.add(caret);
            return new DragArea( panel );
        } else {
            return new DragArea( text );
        }
    }

    @Override
    public void changeTitle( final PartDefinition part,
                             final String title,
                             final IsWidget titleDecoration ) {
        final Widget _title = buildTitle( title, titleDecoration );
        partTitle.put( part, _title );
        if ( isDndEnabled ) {
            dndManager.makeDraggable( partContentView.get( part ), _title );
        }
        setupTitleDropdown();
        if ( currentPart != null && currentPart.getK1().equals( part ) ) {
            updateBreadcrumb( part );
        }
    }

    @Override
    public boolean selectPart( final PartDefinition part ) {
        if ( !parts.contains( part ) ) {
            //not necessary to check if current is part
            return false;
        }

        if ( currentPart != null ) {
            if ( currentPart.getK1().equals( part ) ) {
                return true;
            }
            parts.add( currentPart.getK1() );
            currentPart.getK2().getElement().getStyle().setDisplay( NONE );
        }

        currentPart = Pair.newPair( part, partContentView.get( part ) );
        currentPart.getK2().getElement().getStyle().setDisplay( BLOCK );
        updateBreadcrumb( part );
        parts.remove( currentPart.getK1() );

        setupTitleDropdown();
        setupContextMenu();

        scheduleResize();

        SelectionEvent.fire( ListBarWidgetImpl.this, part );

        return true;
    }

    private void setupTitleDropdown() {
        if( currentPart == null ){
            return;
        }
        final Widget title = partTitle.get( currentPart.getK1() );
        if ( isMultiPart ) {
            titleDropDown.setVisible(true);
            titleAnchor.clear();
            titleAnchor.add(title);
            refillPartChooserList();
        } else {
            titleHeading.setVisible(true);
            titleHeading.clear();
            titleHeading.add(title);
        }
    }

    private void setupContextMenu() {
        contextMenu.clear();
        final WorkbenchPartPresenter.View part = (WorkbenchPartPresenter.View) currentPart.getK2().getWidget( 0 );
        if ( part.getPresenter().getMenus() != null && part.getPresenter().getMenus().getItems().size() > 0 ) {
            for ( final MenuItem menuItem : part.getPresenter().getMenus().getItems() ) {
                final Widget result = makeItem( menuItem, true );
                if ( result != null ) {
                    contextMenu.add( result );
                }
            }
        }
    }

    @Override
    public boolean remove( final PartDefinition part ) {
        if ( currentPart.getK1().equals( part ) ) {
            if ( parts.size() > 0 ) {
                presenter.selectPart( parts.iterator().next() );
            } else {
                clear();
            }
        }

        boolean removed = parts.remove( part );
        FlowPanel view = partContentView.remove( part );
        if ( view != null ) {
            // FIXME null check should not be necessary, but sometimes the entry in partContentView is missing!
            content.remove( view );
        }
        partTitle.remove( part );
        setupTitleDropdown();

        scheduleResize();

        return removed;
    }

    @Override
    public void setFocus( final boolean hasFocus ) {
    }

    @Override
    public void addOnFocusHandler( final Command command ) {
    }

    @Override
    public int getPartsSize() {
        if ( currentPart == null ) {
            return 0;
        }
        return parts.size() + 1;
    }

    @Override
    public HandlerRegistration addBeforeSelectionHandler( final BeforeSelectionHandler<PartDefinition> handler ) {
        return addHandler( handler, BeforeSelectionEvent.getType() );
    }

    @Override
    public HandlerRegistration addSelectionHandler( final SelectionHandler<PartDefinition> handler ) {
        return addHandler( handler, SelectionEvent.getType() );
    }

    @Override
    public void onResize() {
        if ( !isAttached() ) {
            return;
        }

        // need explicit resize here because height: 100% in CSS makes the panel too tall
        int contentHeight = getOffsetHeight() - header.getOffsetHeight();

        if ( contentHeight < 0 ) {
            // occasionally (like 1 in 20 times) the panel has 0px height when we get the onResize() call
            // this is a temporary workaround until we figure it out
            content.getElement().getStyle().setHeight( 100, Unit.PCT );
        } else {
            content.getElement().getStyle().setHeight( contentHeight, Unit.PX );
        }

        super.onResize();

        // FIXME only need to do this for the one visible part .. need to call onResize() when switching parts anyway
        for ( int i = 0; i < content.getWidgetCount(); i++ ) {
            final FlowPanel container = (FlowPanel) content.getWidget( i );
            final Widget containedWidget = container.getWidget( 0 );
            if ( containedWidget instanceof RequiresResize ) {
                ( (RequiresResize) containedWidget ).onResize();
            }
        }
    }

    // TODO refactor this to use a MenuVisitor
    private Widget makeItem( final MenuItem item,
                             boolean isRoot ) {
        if ( !authzManager.authorize( item, identity ) ) {
            return null;
        }

        if ( item instanceof MenuItemCommand ) {
            final MenuItemCommand cmdItem = (MenuItemCommand) item;
            if ( isRoot ) {
                final Button button = new Button( cmdItem.getCaption() );
                button.setSize( ButtonSize.SMALL );
                button.setEnabled( item.isEnabled() );
                button.addClickHandler( new ClickHandler() {
                    @Override
                    public void onClick( final ClickEvent event ) {
                        cmdItem.getCommand().execute();
                    }
                } );
                item.addEnabledStateChangeListener( new EnabledStateChangeListener() {
                    @Override
                    public void enabledStateChanged( final boolean enabled ) {
                        button.setEnabled( enabled );
                    }
                } );
                return button;
            } else {
                final NavbarLink navbarLink = new NavbarLink();
                navbarLink.setText( cmdItem.getCaption() );
                if ( !item.isEnabled() ) {
                    navbarLink.addStyleName( "disabled" );
                }
                navbarLink.addClickHandler( new ClickHandler() {
                    @Override
                    public void onClick( final ClickEvent event ) {
                        cmdItem.getCommand().execute();
                    }
                } );
                item.addEnabledStateChangeListener( new EnabledStateChangeListener() {
                    @Override
                    public void enabledStateChanged( final boolean enabled ) {
                        if ( enabled ) {
                            navbarLink.removeStyleName( "disabled" );
                        } else {
                            navbarLink.addStyleName( "disabled" );
                        }
                    }
                } );
                return navbarLink;
            }

        } else if ( item instanceof MenuGroup ) {
            final MenuGroup groups = (MenuGroup) item;
            if ( isRoot ) {
                final List<Widget> widgetList = new ArrayList<Widget>();
                for ( final MenuItem _item : groups.getItems() ) {
                    final Widget widget = makeItem( _item, false );
                    if ( widget != null ) {
                        widgetList.add( widget );
                    }
                }

                if ( widgetList.isEmpty() ) {
                    return null;
                }

                return makeDropDownMenuButton( groups.getCaption(),
                                               widgetList );

            } else {
                final List<Widget> widgetList = new ArrayList<Widget>();
                for ( final MenuItem _item : groups.getItems() ) {
                    final Widget result = makeItem( _item, false );
                    if ( result != null ) {
                        widgetList.add( result );
                    }
                }

                if ( widgetList.isEmpty() ) {
                    return null;
                }

                return makeDropDownMenuButton( groups.getCaption(),
                                               widgetList );
            }

        } else if ( item instanceof MenuCustom ) {
            final Object result = ( (MenuCustom) item ).build();
            if ( result instanceof Widget ) {
                return (Widget) result;
            }
        }

        return null;
    }

    private Widget makeDropDownMenuButton( final String caption,
                                           final List<Widget> widgetList ) {
        final ButtonGroup buttonGroup = new ButtonGroup();
        final Button dropdownButton = new Button( caption );
        dropdownButton.setDataToggle( Toggle.DROPDOWN );
        dropdownButton.setSize( ButtonSize.EXTRA_SMALL );
        final DropDownMenu dropDownMenu = new DropDownMenu();
        for ( final Widget _item : widgetList ) {
            dropDownMenu.add( _item );
        }
        buttonGroup.add( dropdownButton );
        buttonGroup.add( dropDownMenu );
        return buttonGroup;
    }

    /**
     * This is the list that appears when you click the down-arrow button in the header (dropdownCaret). It lists all
     * the available parts. Clicking on a list item selects its associated part, making it visible, and hiding all other
     * parts.
     */
    private void refillPartChooserList() {
        titleDropdownMenu.clear();
        if ( currentPart != null ) {
            final String ctitle = ((WorkbenchPartPresenter.View) partContentView.get(currentPart.getK1()).getWidget(0)).getPresenter().getTitle();
            final ListItem currentPartEntry = new ListItem();
            final Anchor anchor = new Anchor();
            final Strong strong = new Strong(ctitle);
            anchor.add( strong );
            final Icon icon = new Icon(IconType.CHECK);
            final Span span = new Span();
            span.add( icon );
            span.addStyleName(Styles.PULL_RIGHT);
            anchor.add(span);

            currentPartEntry.add( anchor );
            titleDropdownMenu.add( currentPartEntry );

            for (final PartDefinition part : parts) {
                final String title = ((WorkbenchPartPresenter.View) partContentView.get(part).getWidget(0)).getPresenter().getTitle();
                final AnchorListItem selectPartEntry = new AnchorListItem(title);
                selectPartEntry.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(final ClickEvent event) {
                        selectPart(part);
                    }
                });
                titleDropdownMenu.add(selectPartEntry);
            }
        }
    }

    private void scheduleResize() {
        Scheduler.get().scheduleDeferred( new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                onResize();
            }
        } );
    }

    /**
     * Returns the toggle button, that can be used to trigger maximizing and unmaximizing
     * of the panel containing this list bar. Make the button visible by calling {@link Widget#setVisible(boolean)}
     * and set its maximize and unmaximize actions with {@link MaximizeToggleButton#setMaximizeCommand(Command)} and
     * {@link MaximizeToggleButton#setUnmaximizeCommand(Command)}.
     */
    @Override
    public MaximizeToggleButtonPresenter getMaximizeButton() {
        return maximizeButtonPresenter;
    }

    @Override
    public boolean isMultiPart() {
        return isMultiPart;
    }

    @Override
    public boolean isDndEnabled() {
        return isDndEnabled;
    }
}
