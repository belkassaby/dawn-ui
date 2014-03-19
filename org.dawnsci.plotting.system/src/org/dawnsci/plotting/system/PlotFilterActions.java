package org.dawnsci.plotting.system;

import org.dawb.common.ui.menu.MenuAction;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlottingFactory;
import org.dawnsci.plotting.api.filter.AbstractPlottingFilter;
import org.dawnsci.plotting.api.filter.IFilterDecorator;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;


/**
 * Class to provide actions to set filter decorators on the plotting system
 * 
 * @author vdp96513
 *
 */
public class PlotFilterActions {

	
	/**
	 * Get filters for 1D data
	 * 
	 * @param system - plotting system to decorate
	 * @return actions
	 */
	public static MenuAction getXYFilterActions(IPlottingSystem system) {
		
		MenuAction menu = new MenuAction("Filters");
		menu.setId("org.dawnsci.plotting.system.XY.Filters");
		
		final IFilterDecorator dec = PlottingFactory.createFilterDecorator(system);
		
		final AbstractPlottingFilter der = new AbstractPlottingFilter() {

			@Override
			public int getRank() {
				return 1;
			}

			protected IDataset[] filter(IDataset x, IDataset y) {

				return new IDataset[]{x, Maths.derivative(DatasetUtils.convertToAbstractDataset(x),
						DatasetUtils.convertToAbstractDataset(y), 1)};
			}
		};
		
		final AbstractPlottingFilter der2 = new AbstractPlottingFilter() {

			@Override
			public int getRank() {
				return 1;
			}

			protected IDataset[] filter(IDataset x, IDataset y) {

				AbstractDataset xds = DatasetUtils.convertToAbstractDataset(x);
				AbstractDataset yds = DatasetUtils.convertToAbstractDataset(y);
				
				return new IDataset[]{x, Maths.derivative(xds,Maths.derivative(xds, yds,1),1)};
			}
		};
		
		IAction off = new Action("Off", IAction.AS_RADIO_BUTTON) {
			public void run() {
				if (isChecked()) {
					dec.clear();
				} 
			}
		};
		
		IAction d1 = new Action("1st derivative", IAction.AS_RADIO_BUTTON) {
			public void run() {
				if (isChecked()) {
					dec.clear();
					dec.addFilter(der);
					dec.apply();
				} 
			}
		};
		IAction d2 = new Action("2nd derivative", IAction.AS_RADIO_BUTTON) {
			public void run() {
				if (isChecked()) {
					dec.clear();
					dec.addFilter(der2);
					dec.apply();
				} 
			}
		};
		
		off.setChecked(true);
		menu.add(off);
		menu.add(d1);
		menu.add(d2);
		
		return menu;
	}
	
}