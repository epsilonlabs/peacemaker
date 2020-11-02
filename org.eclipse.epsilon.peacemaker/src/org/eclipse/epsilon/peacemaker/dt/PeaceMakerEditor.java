package org.eclipse.epsilon.peacemaker.dt;

import java.util.Map;

import org.eclipse.emf.common.ui.viewer.ColumnViewerInformationControlToolTipSupport;
import org.eclipse.emf.ecore.presentation.EcoreEditor;
import org.eclipse.emf.ecore.presentation.EcoreEditorPlugin;
import org.eclipse.emf.edit.ui.celleditor.AdapterFactoryTreeEditor;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.emf.edit.ui.provider.DecoratingColumLabelProvider;
import org.eclipse.emf.edit.ui.provider.DiagnosticDecorator;
import org.eclipse.epsilon.peacemaker.PeaceMakerXMIResourceFactory;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Tree;

public class PeaceMakerEditor extends EcoreEditor {

	protected ListViewer conflictsListViewer;

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

		// Only creates the other pages if there is something that can be edited
		if (!getEditingDomain().getResourceSet().getResources().isEmpty()) {

			Composite conflictsPage = new Composite(getContainer(), SWT.BORDER);
			GridLayout pageLayout = new GridLayout(1, false);
			conflictsPage.setLayout(pageLayout);

			SashForm sashForm = new SashForm(conflictsPage, SWT.HORIZONTAL);
			GridDataFactory.fillDefaults().grab(true, true).minSize(1, 1).applyTo(sashForm);

			Tree tree = new Tree(sashForm, SWT.MULTI);
			//			GridData treeData = new GridData(SWT.FILL, SWT.FILL, true, true);
			GridDataFactory.fillDefaults().grab(true, true).minSize(1, 1).applyTo(tree);

			selectionViewer = new TreeViewer(tree);
			setCurrentViewer(selectionViewer);

			selectionViewer.setUseHashlookup(true);
			selectionViewer.setContentProvider(new AdapterFactoryContentProvider(adapterFactory));
			selectionViewer.setLabelProvider(new DecoratingColumLabelProvider(new AdapterFactoryLabelProvider(adapterFactory),
					new DiagnosticDecorator(editingDomain, selectionViewer, EcoreEditorPlugin.getPlugin().getDialogSettings())));
			selectionViewer.setInput(editingDomain.getResourceSet());
			selectionViewer.setSelection(new StructuredSelection(editingDomain.getResourceSet().getResources().get(0)), true);

			new AdapterFactoryTreeEditor(selectionViewer.getTree(), adapterFactory);
			new ColumnViewerInformationControlToolTipSupport(selectionViewer,
					new DiagnosticDecorator.EditingDomainLocationListener(editingDomain, selectionViewer));

			createContextMenuFor(selectionViewer);

			List list = new List(sashForm, SWT.READ_ONLY);
			GridDataFactory.fillDefaults().grab(true, true).minSize(1, 1).applyTo(list);
			
			conflictsListViewer = new ListViewer(list);

			sashForm.setWeights(new int[] { 3, 1 });

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
}
