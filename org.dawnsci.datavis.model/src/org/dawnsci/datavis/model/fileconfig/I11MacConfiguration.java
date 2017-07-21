package org.dawnsci.datavis.model.fileconfig;

import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.LoadedFile;

public class I11MacConfiguration implements ILoadedFileConfiguration {

	@Override
	public boolean configure(LoadedFile f) {
		
		DataOptions tth = f.getDataOption("tth");
		DataOptions counts = f.getDataOption("counts");
		
		if (tth != null && counts != null 
				&& tth.getLazyDataset().getSize() == counts.getLazyDataset().getSize()) {
			counts.setAxes(new String[]{tth.getName()});
			counts.setSelected(true);
			return true;
		}

		
		return false;
	}

}