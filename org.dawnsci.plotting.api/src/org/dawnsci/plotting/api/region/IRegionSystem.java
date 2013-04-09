package org.dawnsci.plotting.api.region;

import java.util.Collection;

import org.dawnsci.plotting.api.region.IRegion.RegionType;


/**
 * No methods in this interface are thread safe.
 * 
 * @author fcp94556
 *
 */
public interface IRegionSystem {

	
	/**
	 * Creates a selection region by type. This does not create any user interface
	 * for the region. You can then call methods on the region to set colour and 
	 * position for the selection. Use addRegion(...) and removeRegion(...) to control
	 * if the selection is active on the graph.
	 * 
	 * Usually thread safe.
	 * 
	 * @param name
	 * @param regionType
	 * @return
	 * @throws Exception if name exists already.
	 */
	public IRegion createRegion(final String name, final RegionType regionType) throws Exception;
	
	/**
	 * Add a selection region to the graph. Not thread safe, call from UI thread.
	 * @param region
	 */
	public void addRegion(final IRegion region);
	
	
	/**
	 * Remove a selection region to the graph. Not thread safe, call from UI thread.
	 * @param region
	 */
	public void removeRegion(final IRegion region);
	
	/**
	 * Get a region by name.
	 * @param name
	 * @return
	 */
	public IRegion getRegion(final String name);
	
	/**
	 * Get all the regions for a given region type, null if there are no regions, empty list if there
	 * are regions but not of this type.
	 * 
	 * @param type
	 * @return
	 */
	public Collection<IRegion> getRegions(RegionType type);

	/**
	 * 
	 * @param l
	 */
	public boolean addRegionListener(final IRegionListener l);
	
	/**
	 * 
	 * @param l
	 */
	public boolean removeRegionListener(final IRegionListener l);

	/**
	 * Remove all regions. Not thread safe, call from UI thread.
	 */
	public void clearRegions();

	/**
	 * Current regions in the plotting system.
	 * @return
	 */
	public Collection<IRegion> getRegions();
	
	/**
	 * Renames the region, better than calling setName on the IRegion as the
	 * collection of regions is updated properly. No event will be fired.
	 */
	public void renameRegion(IRegion region, String name) throws Exception;
}
