package org.eclipse.epsilon.peacemaker.dt;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.presentation.EcoreEditor;
import org.eclipse.emf.ecore.presentation.EcoreEditorPlugin;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.emf.edit.ui.provider.DecoratingColumLabelProvider;
import org.eclipse.emf.edit.ui.provider.DiagnosticDecorator;
import org.eclipse.emf.edit.ui.provider.UnwrappingSelectionProvider;
import org.eclipse.epsilon.peacemaker.PeaceMakerXMIResource;
import org.eclipse.epsilon.peacemaker.PeaceMakerXMIResourceFactory;
import org.eclipse.epsilon.peacemaker.conflicts.Conflict;
import org.eclipse.epsilon.peacemaker.conflicts.Conflict.ResolveAction;
import org.eclipse.epsilon.peacemaker.conflicts.ConflictSection;
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
import org.eclipse.swt.widgets.Tree;

public class PeaceMakerEditor extends EcoreEditor {

	public class ResolveActionGroup {

		protected Group group;
		protected Map<Button, ResolveAction> button2action = new HashMap<>();

		public ResolveActionGroup(Composite parent, int style) {
			group = new Group(parent, style);
			group.setLayout(new RowLayout(SWT.VERTICAL));
			group.setText("Actions");
			resolveGroups.put(group, this);
		}

		public void createActionButtons(Conflict conflict) {

			for (ResolveAction action : conflict.getSupportedActions()) {
				Button actionButton = new Button(group, SWT.RADIO);
				actionButton.setText(action.toString());
				button2action.put(actionButton, action);

				actionButton.addSelectionListener(new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						// if actionButton is the previously selected one
						if (!actionButton.getSelection()) {
							//TODO: undo the action of the actionButton (needs new commandstack)

							Button newSelection = ResolveActionGroup.this.getSelectedButton();
							if (button2action.containsKey(newSelection)) {
								//TODO: study how to add the radio button as listener (do-undo stuff)
								ConflictResolveCommand command = new ConflictResolveCommand(
										notifiers, conflict, button2action.get(newSelection));
								editingDomain.getCommandStack().execute(command);
							}
						}
					}
				});
			}

			Button noAction = new Button(group, SWT.RADIO);
			noAction.setText("No action");
			noAction.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					// if noAction was the previously selected button
					if (!noAction.getSelection()) {
						// nothing to undo in this case

						// by definition, the new button has an action
						Button newSelection = ResolveActionGroup.this.getSelectedButton();
						ConflictResolveCommand command = new ConflictResolveCommand(
								notifiers, conflict, button2action.get(newSelection));
						editingDomain.getCommandStack().execute(command);
					}
				}
			});
			noAction.setSelection(true);
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

			Composite leftVersion = new Composite(leftRightVersionsSash, SWT.BORDER);
			leftVersion.setLayout(new GridLayout(1, false));

			Label leftLabel = new Label(leftVersion, SWT.NONE);
			leftLabel.setText("Left version");

			Tree leftTree = new Tree(leftVersion, SWT.MULTI);
			GridDataFactory.fillDefaults().grab(true, true).minSize(1, 1).applyTo(leftTree);

			leftViewer = createViewer(leftTree, pmResource.getLeftResource());
			setCurrentViewer(leftViewer);

			Composite rightVersion = new Composite(leftRightVersionsSash, SWT.BORDER);
			rightVersion.setLayout(new GridLayout(1, false));

			Label rightLabel = new Label(rightVersion, SWT.NONE);
			rightLabel.setText("Right version");

			Tree rightTree = new Tree(rightVersion, SWT.MULTI);
			GridDataFactory.fillDefaults().grab(true, true).minSize(1, 1).applyTo(rightTree);

			rightViewer = createViewer(rightTree, pmResource.getRightResource());
			rightViewer.addSelectionChangedListener(viewerChangedListener);

			Composite mergedVersion = new Composite(topBottomVersionsSash, SWT.BORDER);
			mergedVersion.setLayout(new GridLayout(1, false));

			Label mergedLabel = new Label(mergedVersion, SWT.NONE);
			mergedLabel.setText("Merged version (currently the left one)");

			Tree mergedTree = new Tree(mergedVersion, SWT.MULTI);
			GridDataFactory.fillDefaults().grab(true, true).minSize(1, 1).applyTo(mergedTree);

			mergedViewer = createViewer(mergedTree, mergedResource);
			mergedViewer.addSelectionChangedListener(viewerChangedListener);


			Composite conflictsList = new Composite(resourceConflictsSash, SWT.BORDER);
			GridDataFactory.fillDefaults().grab(true, true).minSize(1, 1).applyTo(conflictsList);

			GridLayout listLayout = new GridLayout(1, false);
			conflictsList.setLayout(listLayout);

			for (Conflict c : pmResource.getConflicts()) {
				Composite conflictControl = new Composite(conflictsList, SWT.BORDER);
				GridDataFactory.fillDefaults().grab(true, false).minSize(1, 1).applyTo(conflictControl);

				GridLayout conflictLayout = new GridLayout(1, false);
				conflictControl.setLayout(conflictLayout);

				Link text = new Link(conflictControl, SWT.WRAP);
				GridDataFactory.fillDefaults().grab(true, true).minSize(1, 1).applyTo(text);
				text.setText(c.toString());

				Link showInTreeViewers = new Link(conflictControl, SWT.WRAP);
				GridDataFactory.fillDefaults().grab(true, false).minSize(1, 1).applyTo(showInTreeViewers);
				showInTreeViewers.setText("Show in tree viewers");

				ResolveActionGroup resolveGroup = new ResolveActionGroup(conflictControl, SWT.NONE);
				GridDataFactory.fillDefaults().grab(false, false).minSize(1, 1).applyTo(resolveGroup.getGroup());
				resolveGroup.createActionButtons(c);
			}

			for (ConflictSection cs : pmResource.getConflictSections()) {
				if (!cs.isEmpty()) {
					Composite conflictSection = new Composite(conflictsList, SWT.BORDER);
					GridDataFactory.fillDefaults().grab(true, false).minSize(1, 1).applyTo(conflictSection);
					GridLayout conflictSectionLayout = new GridLayout(1, false);
					conflictSection.setLayout(conflictSectionLayout);

					Label label = new Label(conflictSection, SWT.WRAP);
					label.setText("Conflict Section");

					Link text = new Link(conflictSection, SWT.BORDER | SWT.WRAP);
					GridDataFactory.fillDefaults().grab(true, false).minSize(1, 1).applyTo(text);
					text.setText(cs.toString());
				}
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

	protected XMIResource createMergedResource() {
		// TODO: make this an actual merged resource
		return pmResource.getLeftResource();
	}

	private TreeViewer createViewer(Tree tree, Resource resource) {
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
}
