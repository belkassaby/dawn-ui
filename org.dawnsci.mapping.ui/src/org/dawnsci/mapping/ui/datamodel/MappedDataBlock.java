package org.dawnsci.mapping.ui.datamodel;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;

public class MappedDataBlock implements MapObject {

	private String name;
	private ILazyDataset dataset;
	
	public MappedDataBlock(String name, ILazyDataset dataset) {
		this.name = name;
		this.dataset = dataset;
	}
	
	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public Object[] getChildren() {
		return null;
	}
	
	public ILazyDataset getSpectrum(int x, int y) {
		
		SliceND slice = new SliceND(dataset.getShape());
		slice.setSlice(0,y,y+1,1);
		slice.setSlice(1,x,x+1,1);
		
		return dataset.getSliceView(slice);
	}
	
	public IDataset getSpectrum(int index) {
		
		SliceND slice = new SliceND(dataset.getShape());
		slice.setSlice(0,index,index+1,1);
		
		return dataset.getSlice(slice);
	}

}