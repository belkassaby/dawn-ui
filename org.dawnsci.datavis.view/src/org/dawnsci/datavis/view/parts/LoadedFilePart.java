package org.dawnsci.datavis.view.parts;


import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.dawnsci.datavis.api.IRecentPlaces;
import org.dawnsci.datavis.model.FileControllerStateEvent;
import org.dawnsci.datavis.model.FileControllerStateEventListener;
import org.dawnsci.datavis.model.IFileController;
import org.dawnsci.datavis.model.IPlotController;
import org.dawnsci.datavis.model.IRefreshable;
import org.dawnsci.datavis.model.LoadedFile;
import org.dawnsci.datavis.view.Activator;
import org.dawnsci.datavis.view.quickfile.IQuickFileWidgetListener;
import org.dawnsci.datavis.view.quickfile.QuickFileWidget;
import org.eclipse.core.resources.IFile;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.IProgressService;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadedFilePart {

	private static final Logger logger = LoggerFactory.getLogger(LoadedFilePart.class);
	
	private final Image icon = Activator.getImage("icons/document-block.png");
	private final Image iconLive = Activator.getImage("icons/document-light.png");
	
	private TableViewer viewer;
	
	@Inject ILoaderService lService;
	@Inject ESelectionService selectionService;
	@Inject EventAdmin eventAdmin;
	@Inject IFileController fileController;
	@Inject IPlotController plotController;
	
	private Image ticked;
	private Image unticked;
	
	private FileControllerStateEventListener fileStateListener;

	@PostConstruct
	public void createComposite(Composite parent) {
		logger.info("Perspective Created: DataVis");
		parent.setLayout(new FormLayout());
		FormData checkForm = new FormData();
		checkForm.top = new FormAttachment(0,0);
		checkForm.left = new FormAttachment(0,0);
		checkForm.right = new FormAttachment(100,0);
		
		Composite tableComposite = new Composite(parent, SWT.NONE);
		tableComposite.setLayoutData(checkForm);
		
		viewer = new TableViewer(tableComposite, SWT.MULTI |SWT.FULL_SELECTION | SWT.BORDER);
		viewer.getTable().setHeaderVisible(true);
		
		QuickFileWidget qfw = null;
		
		if (ServiceManager.getRecentPlaces() != null) {
			qfw = new QuickFileWidget(parent);
			FormData comboForm = new FormData();
			checkForm.bottom = new FormAttachment(qfw);
			comboForm.left = new FormAttachment(0,0);
			comboForm.right = new FormAttachment(100,0);
			comboForm.bottom = new FormAttachment(100,0);
			comboForm.height = 32;
			qfw.setLayoutData(comboForm);
			
			IRecentPlaces recentPlaces = ServiceManager.getRecentPlaces();
			List<String> p = recentPlaces.getRecentPlaces();
			if (p.isEmpty()) {
				qfw.setDirectoryPath(System.getProperty("user.home"));
			} else {
				qfw.setDirectoryPath(p.get(0));
			}
			
		} else {
			checkForm.bottom = new FormAttachment(100,0);
		}
		
		final QuickFileWidget quickFileWidget = qfw;
		
		if (quickFileWidget != null) {
			qfw.addListener(new IQuickFileWidgetListener() {
				
				@Override
				public void fileSelected(String directory, String name) {
					String match = name;
					File folder = new File(directory);
					File[] files = folder.listFiles((FilenameFilter)new WildcardFileFilter(match));
					Arrays.sort(files);
					String[] names = Arrays.stream(files)
							.filter(f -> !f.isDirectory())
							.map(File::getAbsolutePath)
							.toArray(String[]::new);
					
					loadData(names);
					logger.debug("Loaded files using quickwidget");
				}
			});
		}
		
		ticked = AbstractUIPlugin.imageDescriptorFromPlugin("org.dawnsci.datavis.view", "icons/ticked.png").createImage();
		unticked = AbstractUIPlugin.imageDescriptorFromPlugin("org.dawnsci.datavis.view", "icons/unticked.gif").createImage();
		
		viewer.setContentProvider(new IStructuredContentProvider() {



			@Override
			public Object[] getElements(Object inputElement) {
				return ((IFileController)inputElement).getLoadedFiles().toArray();
			}

			@Override
			public void dispose() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		TableViewerColumn check   = new TableViewerColumn(viewer, SWT.CENTER, 0);
		check.setEditingSupport(new CheckBoxEditSupport(viewer));
		check.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return "";
			}
			
			@Override
			public Image getImage(Object element) {
				return ((LoadedFile)element).isSelected() ? ticked : unticked;
			}
			
		});

		check.getColumn().setWidth(28);
		
		TableViewerColumn name = new TableViewerColumn(viewer, SWT.LEFT);
		name.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				
				String name = ((LoadedFile)element).getName();
				
				return name;
			}
			
			@Override
			public Image getImage(Object element) {
				
				if (element instanceof IRefreshable && ((IRefreshable)element).isLive()) {
					return iconLive;
				}
				
				return icon;
			}
		});
		
		name.getColumn().setText("Filename");
		name.getColumn().setWidth(200);
		
		TableColumnLayout columnLayout = new TableColumnLayout();
	    columnLayout.setColumnData(check.getColumn(), new ColumnPixelData(24));
	    columnLayout.setColumnData(name.getColumn(), new ColumnWeightData(100,20));
	    
	    tableComposite.setLayout(columnLayout);
		
		ColumnViewerToolTipSupport.enableFor(viewer);
		viewer.setInput(fileController);
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			  @Override
			  public void selectionChanged(SelectionChangedEvent event) {
			    IStructuredSelection selection = viewer.getStructuredSelection();
			    if (selection.getFirstElement() instanceof LoadedFile) {
			    	LoadedFile selected = (LoadedFile)selection.getFirstElement();
			    	fileController.setCurrentFile(selected, selected.isSelected());
			    }
			    
			    selectionService.setSelection(new StructuredSelection(selection.toArray()));
			    
			  }
			});
		
		MenuManager menuMgr = new MenuManager();
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		menuMgr.addMenuListener(new LoadedFileMenuListener(fileController, viewer));
		menuMgr.setRemoveAllWhenShown(true);
		viewer.getControl().setMenu(menu);
		
		fileStateListener = new FileControllerStateEventListener() {
			
			@Override
			public void stateChanged(FileControllerStateEvent event) {
				updateOnStateChange(event);
				
				if (quickFileWidget != null && ServiceManager.getRecentPlaces() != null) {

					List<String> recentPlaces = ServiceManager.getRecentPlaces().getRecentPlaces();
					if (recentPlaces != null && !recentPlaces.isEmpty()) {
						Display.getDefault().asyncExec(() ->quickFileWidget.setDirectoryPath(ServiceManager.getRecentPlaces().getRecentPlaces().get(0)));
					}

				}
			}
		};
		
		fileController.addStateListener(fileStateListener);
		
		fileController.attachLive();
		plotController.init();
		
		DropTargetAdapter dropListener = new DropTargetAdapter() {
			@Override
			public void drop(DropTargetEvent event) {
				Object dropData = event.data;
				if (dropData instanceof TreeSelection) {
					TreeSelection selectedNode = (TreeSelection) dropData;
					Object obj[] = selectedNode.toArray();
					List<String> paths = new ArrayList<String>();
					for (int i = 0; i < obj.length; i++) {
						if (obj[i] instanceof IFile) {
							IFile file = (IFile) obj[i];
							paths.add(file.getLocation().toOSString());
						}
					}
					
					if (!paths.isEmpty()) {
						loadData(paths.toArray(new String[paths.size()]));
					}
					
				} else if (dropData instanceof String[]) {
					loadData((String[])dropData);
				}
			}
		};
		
		DropTarget dt = new DropTarget(viewer.getControl(), DND.DROP_MOVE | DND.DROP_DEFAULT | DND.DROP_COPY);
		dt.setTransfer(new Transfer[] { TextTransfer.getInstance(),
				FileTransfer.getInstance(),
				LocalSelectionTransfer.getTransfer() });
		dt.addDropListener(dropListener);
		
		//hook up delete key to remove from list
		viewer.getTable().addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					new LoadedFileMenuListener(fileController,viewer).new CloseAction(fileController,viewer).run();
				}
			}
		});

	}
	
	private void loadData(String[] paths){
		List<String> loadFiles = fileController.loadFiles(paths,(IProgressService) PlatformUI.getWorkbench().getService(IProgressService.class));
		
		if (loadFiles != null && !loadFiles.isEmpty()) {
			MessageDialog.openError(
					Display.getCurrent().getActiveShell(),
					"File Loading Error",
					"An error occured during data loading: " + loadFiles.size() + " files could not be opened!");
		}
		
	}
	
	private void updateOnStateChange(final FileControllerStateEvent event) {
		if (Display.getCurrent() == null) {
			Display.getDefault().syncExec(new Runnable() {
				
				@Override
				public void run() {
					updateOnStateChange(event);
				}
			});
			
			return;
		}
		
		viewer.refresh();
	}
	
	@PreDestroy
	public void dispose(){
		fileController.removeStateListener(fileStateListener);
		fileController.detachLive();
		ticked.dispose();
		unticked.dispose();
	}

	@Focus
	public void setFocus() {
		if (viewer != null) viewer.getControl().setFocus();
	}
	
	@Inject
	@Optional
	private void subscribeFileOpenE3(@UIEventTopic("org/dawnsci/events/file/OPEN") Event data ) {
		String[] paths = (String[])data.getProperty("paths");
		if (paths == null) {
			String path = (String)data.getProperty("path");
			paths = new String[]{path};
		}
		
		if (data.getProperty("live_bean") != null) return;
		
		loadData(paths);

	} 
	
	private class CheckBoxEditSupport extends EditingSupport {

		public CheckBoxEditSupport(ColumnViewer viewer) {
			super(viewer);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			CheckboxCellEditor edit = new CheckboxCellEditor(viewer.getTable());
			edit.setValue(((LoadedFile)element).isSelected());
			return edit;
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			if (element instanceof LoadedFile) return ((LoadedFile)element).isSelected();
			return null;
		}

		@Override
		protected void setValue(Object element, Object value) {
			if (element instanceof LoadedFile && value instanceof Boolean){
				fileController.setCurrentFile((LoadedFile)element, (Boolean)value);
			}
//			fileController().setCurrentData((DataOptions)element, (Boolean)value);
		}
		
	}


}