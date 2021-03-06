/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function.internal;

import org.dawnsci.common.widgets.gda.function.descriptors.FunctionDescriptor;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

/**
 * Content proposal provider for simple functions i.e. not JEXL expression
 * functions
 *
 */
public class SimpleFunctionContentProposalProvider implements
		IContentProposalProvider {

	private FunctionDescriptor functionDescriptor;

	public SimpleFunctionContentProposalProvider(
			FunctionDescriptor functionDescriptor) {
		this.functionDescriptor = functionDescriptor;
	}

	@Override
	public IContentProposal[] getProposals(String contents, int position) {
		String name = functionDescriptor.getName();
		if (name.length() >= contents.length()
				&& name.substring(0, contents.length()).equalsIgnoreCase(
						contents)) {
			return new IContentProposal[] { new FunctionContentProposal(
					functionDescriptor) };
		}
		return new IContentProposal[0];
	}

}
