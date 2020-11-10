package org.eclipse.epsilon.peacemaker.dt;

import java.util.Map;

import org.eclipse.emf.ecore.presentation.EcoreEditor;
import org.eclipse.emf.ecore.presentation.EcoreEditorPlugin;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.emf.edit.ui.provider.DecoratingColumLabelProvider;
import org.eclipse.emf.edit.ui.provider.DiagnosticDecorator;
import org.eclipse.emf.edit.ui.provider.UnwrappingSelectionProvider;
import org.eclipse.epsilon.peacemaker.PeaceMakerXMIResource;
import org.eclipse.epsilon.peacemaker.PeaceMakerXMIResourceFactory;
import org.eclipse.epsilon.peacemaker.conflicts.Conflict;
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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;

public class PeaceMakerEditor extends EcoreEditor {

	protected TreeViewer leftViewer;
	protected TreeViewer rightViewer;

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

		// Only creates the other pages if a conflicts resource has been loaded
		if (getEditingDomain().getResourceSet().getResources().size() >= 3) {

			Composite conflictsPage = new Composite(getContainer(), SWT.BORDER);
			GridLayout pageLayout = new GridLayout(1, false);
			conflictsPage.setLayout(pageLayout);

			SashForm sashForm = new SashForm(conflictsPage, SWT.HORIZONTAL);
			GridDataFactory.fillDefaults().grab(true, true).minSize(1, 1).applyTo(sashForm);

			Composite leftVersion = new Composite(sashForm, SWT.BORDER);
			GridLayout leftLayout = new GridLayout(1, false);
			leftVersion.setLayout(leftLayout);

			Label leftLabel = new Label(leftVersion, SWT.NONE);
			leftLabel.setText("Left version");

			Tree leftTree = new Tree(leftVersion, SWT.MULTI);
			GridDataFactory.fillDefaults().grab(true, true).minSize(1, 1).applyTo(leftTree);

			leftViewer = createViewer(leftTree, editingDomain.getResourceSet().getResources().get(1));
			setCurrentViewer(leftViewer);

			Composite rightVersion = new Composite(sashForm, SWT.BORDER);
			GridLayout rightLayout = new GridLayout(1, false);
			rightVersion.setLayout(rightLayout);

			Label rightLabel = new Label(rightVersion, SWT.NONE);
			rightLabel.setText("Right version");

			Tree rightTree = new Tree(rightVersion, SWT.MULTI);
			GridDataFactory.fillDefaults().grab(true, true).minSize(1, 1).applyTo(rightTree);

			rightViewer = createViewer(rightTree, editingDomain.getResourceSet().getResources().get(2));
			rightViewer.addSelectionChangedListener(viewerChangedListener);


			Composite conflictsList = new Composite(sashForm, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, true).minSize(1, 1).applyTo(conflictsList);

			GridLayout listLayout = new GridLayout(1, false);
			conflictsList.setLayout(listLayout);

			PeaceMakerXMIResource pmResource = (PeaceMakerXMIResource) editingDomain.getResourceSet().getResources().get(0);
			
			for (Conflict c : pmResource.getConflicts()) {
				Composite conflict = new Composite(conflictsList, SWT.BORDER);
				GridDataFactory.fillDefaults().grab(true, false).minSize(1, 1).applyTo(conflict);

				GridLayout conflictLayout = new GridLayout(1, false);
				conflict.setLayout(conflictLayout);

				Link text = new Link(conflict, SWT.WRAP);
				GridDataFactory.fillDefaults().grab(true, true).minSize(1, 1).applyTo(text);
				text.setText(c.toString());

				Link showInTreeViewers = new Link(conflict, SWT.WRAP);
				GridDataFactory.fillDefaults().grab(true, false).minSize(1, 1).applyTo(showInTreeViewers);
				showInTreeViewers.setText("Show in tree viewers");

				Group resolveGroup = new Group(conflict, SWT.NONE);
				GridDataFactory.fillDefaults().grab(false, false).minSize(1, 1).applyTo(resolveGroup);
				resolveGroup.setLayout(new RowLayout(SWT.VERTICAL));
				resolveGroup.setText("Actions");

				Button keepLeft = new Button(resolveGroup, SWT.RADIO);
				keepLeft.setText("Keep left");

				Button keepRight = new Button(resolveGroup, SWT.RADIO);
				keepRight.setText("Keep right");

				Button noAction = new Button(resolveGroup, SWT.RADIO);
				noAction.setText("No action");
				noAction.setSelection(true);
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

			sashForm.setWeights(new int[] { 1, 1, 1 });

			int pageIndex = addPage(conflictsPage);
			setPageText(pageIndex, "PeaceMaker Conflicts");

			getSite().getShell().getDisplay().asyncExec(new Runnable() {

				public void run() {
					if (!getContainer().isDisposed()) {
						setActivePage(0);
					}
				}
			});
		}
		else {
			addPage(new Composite(getContainer(), SWT.BORDER)); // blank one
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
