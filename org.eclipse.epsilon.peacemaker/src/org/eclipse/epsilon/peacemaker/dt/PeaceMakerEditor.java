package org.eclipse.epsilon.peacemaker.dt;

import java.util.Arrays;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.command.AbstractCommand;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CommandStackListener;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.presentation.EcoreEditor;
import org.eclipse.emf.ecore.presentation.EcoreEditorPlugin;
import org.eclipse.emf.ecore.provider.EcoreItemProviderAdapterFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.emf.edit.ui.provider.DecoratingColumLabelProvider;
import org.eclipse.emf.edit.ui.provider.DiagnosticDecorator;
import org.eclipse.emf.edit.ui.provider.UnwrappingSelectionProvider;
import org.eclipse.epsilon.peacemaker.PeaceMakerXMIResource;
import org.eclipse.epsilon.peacemaker.PeaceMakerXMIResourceFactory;
import org.eclipse.epsilon.peacemaker.conflicts.Conflict;
import org.eclipse.epsilon.peacemaker.conflicts.Conflict.ResolveAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.views.properties.PropertySheetPage;

public class PeaceMakerEditor extends EcoreEditor {

	public class ResolveActionGroup {

		protected Conflict conflict; // necessary for event listeners
		protected Group group;
		protected Map<Button, ResolveAction> button2action = new HashMap<>();
		protected Map<ResolveAction, Button> action2button = new HashMap<>();

		// avoid triggering events when changing radio buttons programmatically
		protected boolean buttonEventsDisabled = false;

		public ResolveActionGroup(Composite parent, int style, Conflict conflict) {
			group = new Group(parent, style);
			group.setLayout(new RowLayout(SWT.VERTICAL));
			group.setText("Actions");
			resolveGroups.put(group, this);
			this.conflict = conflict;
		}

		public void createActionButtons(Conflict conflict) {

			for (ResolveAction action : ResolveAction.values()) {
				if (!conflict.supports(action)) {
					continue;
				}
				Button actionButton = new Button(group, SWT.RADIO);
				actionButton.setText(action.toString());
				button2action.put(actionButton, action);
				action2button.put(action, actionButton);

				if (action == ResolveAction.NO_ACTION) {
					actionButton.setSelection(true);
				}

				actionButton.addSelectionListener(new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						// if actionButton is the previously selected one
						if (!buttonEventsDisabled && !actionButton.getSelection()) {
							getCommandStack().undo(conflict, action);

							Button newSelection = getSelectedButton();
							ConflictResolveCommand command = new ConflictResolveCommand(
									notifiers, conflict,
									button2action.get(newSelection), action);
							getCommandStack().execute(command);
						}
					}
				});
			}

			// update radio buttons selections according to the command actions
			editingDomain.getCommandStack().addCommandStackListener(new CommandStackListener() {

				@Override
				public void commandStackChanged(EventObject event) {
					ConflictResolveCommand mostRecentCommand =
							(ConflictResolveCommand) getCommandStack().getMostRecentCommand();

					// if the command stack has not been flushed
					//   and it is a command related to the conflict of this group
					if (mostRecentCommand != null && mostRecentCommand.getConflict() == conflict) {
						synchronized (ResolveActionGroup.this) {
							buttonEventsDisabled = true;
						}

						// if the command has been undone
						if (mostRecentCommand.canExecute()) {
							// select the previous radio button
							action2button.get(mostRecentCommand.getAction()).setSelection(false);
							action2button.get(mostRecentCommand.getPreviousAction()).setSelection(true);

						}
						// else if the comand has been redone
						else if (!action2button.get(mostRecentCommand.getAction()).getSelection()) {
							// select the next button
							action2button.get(mostRecentCommand.getAction()).setSelection(true);
							action2button.get(mostRecentCommand.getPreviousAction()).setSelection(false);
						}

						synchronized (ResolveActionGroup.this) {
							buttonEventsDisabled = false;
						}
					}
				}
			});
		}

		protected Button getSelectedButton() {
			for (Control c : group.getChildren()) {
				Button b = (Button) c;
				if (b.getSelection()) {
					return b;
				}
			}
			return null; // unreachable, there is a button selected from the start
		}

		public Group getGroup() {
			return group;
		}
	}

	protected TreeViewer leftViewer;
	protected TreeViewer rightViewer;
	protected TreeViewer mergedViewer;

	protected PeaceMakerXMIResource pmResource;
	protected XMIResource mergedResource;

	protected Map<Group, ResolveActionGroup> resolveGroups = new HashMap<>();
	protected List<Notifier> notifiers;

	protected ISelectionChangedListener viewerChangedListener = new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent selectionChangedEvent) {
			setCurrentViewer((TreeViewer) selectionChangedEvent.getSelectionProvider());
		}
	};

	public PeaceMakerEditor() {
		super();

		final Map<String, Object> extensionToFactoryMap =
				editingDomain.getResourceSet().getResourceFactoryRegistry().getExtensionToFactoryMap();

		// Using "*" here might be dangerous based on Exeed constructor comments
		extensionToFactoryMap.put("*", new PeaceMakerXMIResourceFactory());
	}

	@Override
	public void createPages() {
		// Creates the model from the editor input
		createModel();

		Composite conflictsPage = new Composite(getContainer(), SWT.BORDER);
		conflictsPage.setBackground(new Color(255, 255, 255));
		int pageIndex = addPage(conflictsPage);
		setPageText(pageIndex, "PeaceMaker Conflicts");

		// Only creates contents if the resource has been loaded
		if (!getEditingDomain().getResourceSet().getResources().isEmpty()) {

			pmResource = (PeaceMakerXMIResource) editingDomain.getResourceSet().getResources().get(0);
			mergedResource = createMergedResource();
			notifiers = Arrays.asList(pmResource.getLeftResource(), pmResource.getRightResource());

			GridLayout pageLayout = new GridLayout(1, false);
			conflictsPage.setLayout(pageLayout);

			// separates the resources from the conflicts list
			SashForm resourceConflictsSash = new SashForm(conflictsPage, SWT.HORIZONTAL);
			GridDataFactory.fillDefaults().grab(true, true).minSize(1, 1).applyTo(resourceConflictsSash);

			// separates left and right versions from the merged one
			SashForm topBottomVersionsSash = new SashForm(resourceConflictsSash, SWT.VERTICAL);
			GridDataFactory.fillDefaults().grab(true, true).minSize(1, 1).applyTo(topBottomVersionsSash);

			// separates left and right versions
			SashForm leftRightVersionsSash = new SashForm(topBottomVersionsSash, SWT.HORIZONTAL);


			// viewer for the left version resource (top left)
			Composite leftVersion = new Composite(leftRightVersionsSash, SWT.BORDER);
			leftVersion.setLayout(new GridLayout(1, false));

			Label leftLabel = new Label(leftVersion, SWT.NONE);
			leftLabel.setText("Left version");

			Tree leftTree = new Tree(leftVersion, SWT.MULTI);
			GridDataFactory.fillDefaults().grab(true, true).minSize(1, 1).applyTo(leftTree);

			leftViewer = createViewer(leftTree, pmResource.getLeftResource());
			setCurrentViewer(leftViewer);


			// viewer for the right version resource (right of left version viewer)
			Composite rightVersion = new Composite(leftRightVersionsSash, SWT.BORDER);
			rightVersion.setLayout(new GridLayout(1, false));

			Label rightLabel = new Label(rightVersion, SWT.NONE);
			rightLabel.setText("Right version");

			Tree rightTree = new Tree(rightVersion, SWT.MULTI);
			GridDataFactory.fillDefaults().grab(true, true).minSize(1, 1).applyTo(rightTree);

			rightViewer = createViewer(rightTree, pmResource.getRightResource());
			rightViewer.addSelectionChangedListener(viewerChangedListener);


			// viewer for the merged version (below left and right viewers)
			Composite mergedVersion = new Composite(topBottomVersionsSash, SWT.BORDER);
			mergedVersion.setLayout(new GridLayout(1, false));

			Label mergedLabel = new Label(mergedVersion, SWT.NONE);
			mergedLabel.setText("Merged version (currently the left one)");

			Tree mergedTree = new Tree(mergedVersion, SWT.MULTI);
			GridDataFactory.fillDefaults().grab(true, true).minSize(1, 1).applyTo(mergedTree);

			mergedViewer = createViewer(mergedTree, mergedResource);
			mergedViewer.addSelectionChangedListener(viewerChangedListener);


			// list of conflicts along with the available actions (right side)
			Composite conflictsList = new Composite(resourceConflictsSash, SWT.BORDER);
			GridDataFactory.fillDefaults().grab(true, true).minSize(1, 1).applyTo(conflictsList);

			GridLayout listLayout = new GridLayout(1, false);
			conflictsList.setLayout(listLayout);

			for (Conflict conflict : pmResource.getConflicts()) {
				Composite conflictControl = new Composite(conflictsList, SWT.BORDER);
				GridDataFactory.fillDefaults().grab(true, false).minSize(1, 1).applyTo(conflictControl);

				GridLayout conflictLayout = new GridLayout(1, false);
				conflictControl.setLayout(conflictLayout);

				StyledText title = new StyledText(conflictControl, SWT.NONE);
				title.setText(conflict.getTitle());
				StyleRange style = new StyleRange();
				style.start = 0;
				style.length = title.getText().length();
				style.fontStyle = SWT.BOLD;
				title.setStyleRange(style);

				Text description = new Text(conflictControl, SWT.WRAP);
				GridDataFactory.fillDefaults().grab(true, true).minSize(1, 1).applyTo(description);
				description.setText(conflict.getDescription());

				Link showInTreeViewers = new Link(conflictControl, SWT.WRAP);
				GridDataFactory.fillDefaults().grab(true, false).minSize(1, 1).applyTo(showInTreeViewers);
				showInTreeViewers.setText("<a>Show in tree viewers</a>");
				showInTreeViewers.addSelectionListener(new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						updateTreeViewerSelections(conflict);
					}
				});

				ResolveActionGroup resolveGroup = new ResolveActionGroup(conflictControl, SWT.NONE, conflict);
				GridDataFactory.fillDefaults().grab(false, false).minSize(1, 1).applyTo(resolveGroup.getGroup());
				resolveGroup.createActionButtons(conflict);
			}

			resourceConflictsSash.setWeights(new int[] { 2, 1 });
			topBottomVersionsSash.setWeights(new int[] { 1, 1 });
			leftRightVersionsSash.setWeights(new int[] { 1, 1 });

			getSite().getShell().getDisplay().asyncExec(new Runnable() {

				public void run() {
					if (!getContainer().isDisposed()) {
						setActivePage(0);
					}
				}
			});
		}

		// Ensures that this editor will only display the page's tab
		// area if there are more than one page
		getContainer().addControlListener(new ControlAdapter() {

			boolean guard = false;

			@Override
			public void controlResized(ControlEvent event) {
				if (!guard) {
					guard = true;
					hideTabs();
					guard = false;
				}
			}
		});

		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				updateProblemIndication();
			}
		});
	}

	protected void updateTreeViewerSelections(Conflict conflict) {
		EObject leftVersionObject = conflict.getLeftVersionObject();
		if (leftVersionObject != null) {
			leftViewer.setSelection(new StructuredSelection(leftVersionObject), true);
		}

		EObject rightVersionObject = conflict.getRightVersionObject();
		if (rightVersionObject != null) {
			rightViewer.setSelection(new StructuredSelection(rightVersionObject), true);
		}
	}

	protected XMIResource createMergedResource() {
		// TODO: make this an actual merged resource
		return pmResource.getLeftResource();
	}

	protected TreeViewer createViewer(Tree tree, Resource resource) {
		TreeViewer viewer = new TreeViewer(tree);

		viewer.setUseHashlookup(true);
		viewer.setContentProvider(new AdapterFactoryContentProvider(adapterFactory));
		viewer.setLabelProvider(new DecoratingColumLabelProvider(new AdapterFactoryLabelProvider(adapterFactory),
				new DiagnosticDecorator(editingDomain, viewer, EcoreEditorPlugin.getPlugin().getDialogSettings())));
		viewer.setInput(resource);
		createContextMenuFor(viewer);

		return viewer;
	}

	@Override
	protected void createContextMenuForGen(StructuredViewer viewer) {
		MenuManager contextMenu = new MenuManager("#PopUp");
		contextMenu.add(new Separator("additions"));
		contextMenu.setRemoveAllWhenShown(true);
		contextMenu.addMenuListener(this);
		Menu menu = contextMenu.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(contextMenu, new UnwrappingSelectionProvider(viewer));

		// no drag and drop
	}
	
	@Override
	protected void handleActivateGen() {
		// Recompute the read only state.
		//
		if (editingDomain.getResourceToReadOnlyMap() != null) {
			// PEACEMAKER: single changed line with respect to superclass
			initReadOnlyResources();

			// Refresh any actions that may become enabled or disabled.
			//
			setSelection(getSelection());
		}

		if (!removedResources.isEmpty()) {
			if (handleDirtyConflict()) {
				getSite().getPage().closeEditor(PeaceMakerEditor.this, false);
			}
			else {
				removedResources.clear();
				changedResources.clear();
				savedResources.clear();
			}
		}
		else if (!changedResources.isEmpty()) {
			changedResources.removeAll(savedResources);
			handleChangedResources();
			changedResources.clear();
			savedResources.clear();
		}
	}

	@Override
	protected void initializeEditingDomain() {
		// Create an adapter factory that yields item providers.
		//
		adapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);

		adapterFactory.addAdapterFactory(new ResourceItemProviderAdapterFactory());
		ecoreItemProviderAdapterFactory = new EcoreItemProviderAdapterFactory();
		adapterFactory.addAdapterFactory(ecoreItemProviderAdapterFactory);
		adapterFactory.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());

		// Create the command stack that will notify this editor as commands are executed.
		//
		ConflictsCommandStack commandStack = new ConflictsCommandStack() {

			@Override
			public void execute(Command command) {
				if (!(command instanceof AbstractCommand.NonDirtying)) {
					DiagnosticDecorator.cancel(editingDomain);
				}
				super.execute(command);
			}
		};

		// Add a listener to set the most recent command's affected objects to be the selection of the viewer with focus.
		//
		commandStack.addCommandStackListener(new CommandStackListener() {

			public void commandStackChanged(final EventObject event) {
				getContainer().getDisplay().asyncExec(new Runnable() {

					public void run() {
						firePropertyChange(IEditorPart.PROP_DIRTY);

						// update selections based on conflict resolutions
						ConflictResolveCommand mostRecentCommand = (ConflictResolveCommand) getCommandStack().getMostRecentCommand();
						if (mostRecentCommand != null) {
							final Conflict conflict = mostRecentCommand.getConflict();
							Runnable runnable = new Runnable() {

								public void run() {
									updateTreeViewerSelections(conflict);
								}
							};
							getSite().getShell().getDisplay().asyncExec(runnable);
						}
						for (Iterator<PropertySheetPage> i = propertySheetPages.iterator(); i.hasNext();) {
							PropertySheetPage propertySheetPage = i.next();
							if (propertySheetPage.getControl() == null || propertySheetPage.getControl().isDisposed()) {
								i.remove();
							}
							else {
								propertySheetPage.refresh();
							}
						}
					}
				});
			}
		});

		// Create the editing domain with a special command stack.
		//
		editingDomain =
				new AdapterFactoryEditingDomain(adapterFactory, commandStack) {

					{
						resourceToReadOnlyMap = new HashMap<>();
					}

					@Override
					public boolean isReadOnly(Resource resource) {
						if (super.isReadOnly(resource) || resource == null) {
							return true;
						}
						else {
							URI uri = resource.getURI();
							boolean result =
									"java".equals(uri.scheme()) ||
											"xcore".equals(uri.fileExtension()) ||
											"xcoreiq".equals(uri.fileExtension()) ||
											"oclinecore".equals(uri.fileExtension()) ||
											"genmodel".equals(uri.fileExtension()) ||
											uri.isPlatformResource() && !resourceSet.getURIConverter().normalize(uri).isPlatformResource() ||
											uri.isPlatformPlugin();
							if (resourceToReadOnlyMap != null) {
								resourceToReadOnlyMap.put(resource, result);
							}
							return result;
						}
					}
				};
	}

	/**
	 * Disable manual edition of certain resources of the view
	 */
	protected void initReadOnlyResources() {
		editingDomain.getResourceToReadOnlyMap().clear();
		editingDomain.getResourceToReadOnlyMap().put(pmResource.getLeftResource(), true);
		editingDomain.getResourceToReadOnlyMap().put(pmResource.getRightResource(), true);
		editingDomain.getResourceToReadOnlyMap().put(mergedResource, true);
	}

	public void setCurrentViewer(Viewer viewer) {
		// If it is changing...
		if (currentViewer != viewer) {
			if (selectionChangedListener == null) {
				// Create the listener on demand.
				selectionChangedListener =
						new ISelectionChangedListener() {
							// This just notifies those things that are affected by the section.
							public void selectionChanged(SelectionChangedEvent selectionChangedEvent) {
								setSelection(selectionChangedEvent.getSelection());
							}
						};
			}

			// Stop listening to the old one, and add the listener for changing editors
			if (currentViewer != null) {
				currentViewer.removeSelectionChangedListener(selectionChangedListener);
				currentViewer.addSelectionChangedListener(viewerChangedListener);
			}

			// Do the opposite for the new current viewer
			if (viewer != null) {
				viewer.removeSelectionChangedListener(viewerChangedListener);
				viewer.addSelectionChangedListener(selectionChangedListener);
			}

			// Remember it.
			currentViewer = viewer;

			// Set the editors selection based on the current viewer's selection.
			setSelection(currentViewer == null ? StructuredSelection.EMPTY : currentViewer.getSelection());
		}
	}

	public ConflictsCommandStack getCommandStack() {
		return (ConflictsCommandStack) editingDomain.getCommandStack();
	}
}
