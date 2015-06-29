package org.dawnsci.plotting.tools.filter;

import java.util.List;

import org.dawnsci.plotting.tools.ImageFilterServiceLoader;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.image.IImageFilterService;
import org.eclipse.dawnsci.plotting.api.filter.AbstractDelayedFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The median factor filter.
 * 
 */
public class MedianFilter extends AbstractDelayedFilter {

	private static Logger logger = LoggerFactory.getLogger(MedianFilter.class);

	@Override
	public int getRank() {
		return 2;
	}

	@Override
	protected Object[] filter(IDataset data, List<IDataset> axes)
			throws Exception {
		int[] box = (int[]) getConfiguration().get("box");
		if (box == null) {
			box = new int[] { 3, 3 };
			logger.warn("Unexpected lack of box configuration parameter in "
					+ getClass().getName());
		}
		IImageFilterService service = ImageFilterServiceLoader.getFilter();
		final IDataset median = service.filterMedian(data, box[0]);
		return new Object[] { median, axes };
	}

}
