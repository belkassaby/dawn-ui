/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.javafx.trace;

import java.awt.image.BufferedImage;

import javafx.scene.paint.Color;

import org.dawnsci.plotting.javafx.SurfaceDisplayer;
import org.dawnsci.plotting.javafx.volume.VolumeRender;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.IntegerDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystemViewer;
import org.eclipse.dawnsci.plotting.api.jreality.data.ColourImageData;
import org.eclipse.dawnsci.plotting.api.trace.IVolumeRenderTrace;
import org.eclipse.swt.widgets.Display;

/**
 * 
 * @author Joel Ogden
 *
 * @Internal
 */
public class VolumeTrace  extends Image3DTrace implements IVolumeRenderTrace
{
	
	private VolumeRender volume; 
	private SurfaceDisplayer scene;
	private double opacity;
	private Color colour;
	
	
	public VolumeTrace(IPlottingSystemViewer plotter, SurfaceDisplayer newScene, String name) {
		super(plotter, name);
		this.scene = newScene;
		
		// set the defaults
		this.opacity = 1d;
		this.colour = new Color(1, 0, 0, 0);
	}

	@Override
	public void setPalette(String paletteName) {
		// TODO Auto-generated method stub
	}

	@Override
	public void dispose() {
        scene.removeVolume(volume);
		super.dispose();
	}
	
	@Override
	public IDataset getData() {
		return new IntegerDataset(1, 1);
	}

	@Override
	public void setData(final int[] size, final IDataset dataset, final double intensityValue)
	{
		if (volume == null)
			volume = new VolumeRender();
		
		// needs to run in javafx thread
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				volume.compute(size, dataset, intensityValue);
				volume.setColour(colour);
	    	}
	    });
		
	}
	
	public VolumeRender getVolume()
	{
		return volume;
	}

	@Override
	public void setOpacity(double opacity) {
		// if the volume has been created, set the opacity
		if (volume != null)
		{
			volume.setOpacity_Matrial(opacity);
		}
		this.opacity = opacity;
	}

	@Override
	public void setColour(int red, int green, int blue) {
		Color colour = new Color((double)red/255, (double)green/255, (double)blue/255, 0);
		
		// if the volume has been created, set the colour
		if (volume != null)
		{
			volume.setColour(colour);
		}
		
		this.colour = colour;
	}
	
}


