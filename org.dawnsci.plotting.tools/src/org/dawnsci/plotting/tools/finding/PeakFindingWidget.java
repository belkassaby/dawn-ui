package org.dawnsci.plotting.tools.finding;
	
import java.awt.Container;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dawnsci.common.widgets.spinner.FloatSpinner;
import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.preference.PeakFindingConstants;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.peakfinding.IPeakFindingService;
import uk.ac.diamond.scisoft.analysis.peakfinding.Peak;
import uk.ac.diamond.scisoft.analysis.peakfinding.PeakFindingData;

/**
 * 
 * Widget to handle all the components that would go into using a peak finder algorithm.
 * 
 * @author Dean P. Ottewell
 */
public class PeakFindingWidget {
	
	private static final Logger logger = LoggerFactory.getLogger(PeakFindingWidget.class);

	PeakFindingManager manager;

	private FloatSpinner uprBndVal;
	private FloatSpinner lwrBndVal;	

	private FloatSpinner searchIntensity;
	public double searchScaleVal;
	private Scale searchScale;
	
	Button runPeakSearch;
	
	List<Peak> peaks = new ArrayList<Peak>();
	IDataset xData = null;
	IDataset yData = null ;
	
	public PeakFindingWidget(PeakFindingManager controller){
		this.manager = controller;
	}
	
	public void createControl(final Composite parent){

		Group configure = new Group(parent, SWT.NONE);
		configure.setText("Peakfinding Configuration ");
		configure.setLayout(new GridLayout(1, false));
		configure.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		configure.setBackground(parent.getBackground());

		Composite configureComposite = new Composite(configure, SWT.NONE);
		configureComposite.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		configureComposite.setLayout(new GridLayout(2, false));

		//ILineTrace sampleTrace = controller.getPeakfindingtool().sampleTrace;
		//TODO: tmp limit. Base of trace
		int max = 100000;
		
		Label lwrBndLab = new Label(configureComposite, SWT.NONE);
		lwrBndLab.setText("Lower Bound");
		lwrBndLab.setToolTipText("As shown by the second vertical line");
		
		lwrBndVal = new FloatSpinner(configureComposite, SWT.BORDER,  max , 3);
		lwrBndVal.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));

		lwrBndVal.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PeakOppurtunity peakOpp = new PeakOppurtunity();
				peakOpp.setUpperBound(uprBndVal.getDouble());
				peakOpp.setLowerBound(lwrBndVal.getDouble());
				manager.loadPeakOppurtunity(peakOpp);
			}
		});
		
		Label upperBndLab = new Label(configureComposite, SWT.NONE);
		upperBndLab.setText("Upper Bound");
		upperBndLab.setToolTipText("As shown by the vertical line");
	
		uprBndVal = new FloatSpinner(configureComposite, SWT.BORDER, max , 3);
		uprBndVal.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		uprBndVal.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PeakOppurtunity peakOpp = new PeakOppurtunity();
				peakOpp.setUpperBound(uprBndVal.getDouble());
				peakOpp.setLowerBound(lwrBndVal.getDouble());
				manager.loadPeakOppurtunity(peakOpp);
			}
		});


		/* Adjust Peak Finding searchIntensity */
		final Label searchIntensityLab = new Label(configureComposite, SWT.NONE);
		searchIntensityLab.setText("Search Intensity");
		searchIntensityLab.setToolTipText("Higher values tend to lead to less peaks");

		searchIntensity = new FloatSpinner(configureComposite, SWT.BORDER, 1000, 3);
		searchIntensity.setMaximum(300);
		searchIntensity.setMinimum(1);
		searchIntensity.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		searchIntensity.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				searchScaleVal = searchIntensity.getDouble();
				manager.setSearchScaleIntensity(searchScaleVal);
			}
		});	
		searchIntensity.setEnabled(false);
		
		searchScale = new Scale(configure, SWT.HORIZONTAL);
		searchScale.setBounds(0, 0, 40, 200);
		searchScale.setOrientation(SWT.LEFT_TO_RIGHT);

		searchScale.setMaximum(300);
		searchScale.setMinimum(1);
		
		searchScale.setIncrement(1);
		searchScale.setPageIncrement(30);
		
		searchScale.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		searchScale.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int perspectiveValue = searchScale.getSelection(); 
				searchIntensity.setDouble(perspectiveValue); 
				
				//TODO: need to change as changing the search intensity and using that trigger...
				searchScaleVal = searchIntensity.getDouble();
				manager.setSearchScaleIntensity(searchScaleVal);
			}
		});
		
		/*Default setups*/
		searchScale.setEnabled(false);
		searchScale.setVisible(false);
		searchScaleVal = searchIntensity.getDouble();

		manager.setSearchScaleIntensity(searchScaleVal);
		
		/*
		 * Swap out peak finders		
		 * */
		Label peakFinderLab = new Label(configure, SWT.NONE);
		peakFinderLab.setLayoutData(new GridData(SWT.CENTER, SWT.LEFT, true, false));
		peakFinderLab.setText("Peak Finder:");
		
		final Combo peakfinderCombo = new Combo(configure, SWT.READ_ONLY);
		peakfinderCombo.setToolTipText("Select a type of peakfinder");
		peakfinderCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		peakfinderCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//XXX: hot ui fix as wavelet only component that needs the intensity active
				if(peakfinderCombo.getText().equals("Wavelet Transform")){
					searchIntensity.setEnabled(true);
					searchScale.setEnabled(true);
					searchScale.setVisible(true);
					searchIntensityLab.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
				} else {
					searchIntensityLab.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
					searchIntensity.setEnabled(false);
					searchScale.setEnabled(false);
					searchScale.setVisible(false);
				}
				
				manager.setPeakFinderID(peakfinderCombo.getText());			
			}
		});
		
		manager.setPeakFindServ((IPeakFindingService) Activator.getService(IPeakFindingService.class));
		//Load in peak finders
		Collection<String> peakFinders = manager.getPeakFindServ().getRegisteredPeakFinders();

		manager.setPeakFindData(new PeakFindingData(manager.getPeakFindServ()));

		for (String pfID : peakFinders) {
			String nameID = manager.getPeakFindServ().getPeakFinderName(pfID);
			peakfinderCombo.add(nameID);
			manager.setPeakFinderID(pfID); 
		}			
		peakfinderCombo.select(0);
		manager.setPeakFinderID(peakfinderCombo.getText().toString()); 
		Activator.getPlottingPreferenceStore().setValue(PeakFindingConstants.PeakAlgorithm, peakfinderCombo.getText().toString());		
		
		runPeakSearch = new Button(configure, SWT.PUSH);
		runPeakSearch.setImage(Activator.getImageDescriptor("icons/peakSearch.png").createImage());
		runPeakSearch.setText("Run Peak Finder");
		runPeakSearch.setLayoutData(new GridData(GridData.FILL_BOTH));
		runPeakSearch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//Bounds check 
				if(lwrBndVal.getDouble() > uprBndVal.getDouble()){
					//Well they want a boudnd setup. So give them it swapped 
					PeakOppurtunity peakOpp = new PeakOppurtunity();
					peakOpp.setLowerBound(uprBndVal.getDouble());
					peakOpp.setUpperBound(lwrBndVal.getDouble());
					manager.loadPeakOppurtunity(peakOpp);
				} 	
				
				// Run peakSearch
	 			if(xData != null && yData != null){
	 				runPeakSearch.setEnabled(false);
					runPeakSearch.setImage(Activator.getImageDescriptor("icons/peakSearching.png").createImage());			
					manager.peakSearchJob= new PeakFindingSearchJob(manager, xData, yData);
					manager.peakSearchJob.schedule();
				} else {
					//No peak data set...
					peaks.clear();
					manager.setPeaks(peaks);
					runPeakSearch.setEnabled(true);
					runPeakSearch.setImage(Activator.getImageDescriptor("icons/peakSearch.png").createImage());
				}
			}
		});
	
		//TODO: check is searching instead
		manager.addPeakListener(new IPeakOpportunityListener() {
			@Override
			public void peaksChanged(PeakOpportunityEvent evt) {
				peaks = evt.getPeaks();
				//XXX: needs to be in own event checking for peak changing as not all searches lead to a change in peaks! what if empty huh! maybe that should update the peaks though...
				runPeakSearch.setEnabled(true);
				runPeakSearch.setImage(Activator.getImageDescriptor("icons/peakSearch.png").createImage());
			}

			@Override
			public void boundsChanged(double upper, double lower) {
				setLowerBound(lower);
				setUpperBound(upper);
			}

			
			//TODO: trigger this intially on a base search
			@Override
			public void dataChanged(IDataset nXData, IDataset nYData) {
				xData = nXData;
				yData = nYData;
			}
			
		});
	
	}
	
	public void setLowerBound(double lowerVal) {
		lwrBndVal.setDouble(lowerVal);
		lwrBndVal.update();
	}
	
	public void setUpperBound(double upperVal) {
		uprBndVal.setDouble(upperVal);
		uprBndVal.update();
	}

}