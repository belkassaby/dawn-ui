package org.dawnsci.common.widgets.gda.function.descriptors;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.dawnsci.common.widgets.gda.function.jexl.JexlExpressionFunction;
import org.dawnsci.common.widgets.gda.function.jexl.JexlExpressionFunctionDescriptor;

import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction;

public class DefaultFunctionDescriptorProvider implements
		IFunctionDescriptorProvider {
	private Map<String, IFunctionDescriptor> nameToDescriptor;
	private HashMap<Class<? extends IFunction>, IFunctionDescriptor> clazzToDescriptor;
	private JexlExpressionFunctionDescriptor jexlDescriptor;

	public DefaultFunctionDescriptorProvider() {
		nameToDescriptor = new HashMap<>();
		clazzToDescriptor = new HashMap<>();
		fill(PluginFunctionDescriptor.getDescriptors());
		jexlDescriptor = new JexlExpressionFunctionDescriptor();
		clazzToDescriptor.put(jexlDescriptor.getIFunctionClass(),
				jexlDescriptor);
	}

	protected void fill(FunctionDescriptor[] descriptors) {
		for (FunctionDescriptor descriptor : descriptors) {
			nameToDescriptor.put(descriptor.getName(), descriptor);
			clazzToDescriptor.put(descriptor.getIFunctionClass(), descriptor);
		}
	}

	@Override
	public IFunctionDescriptor getDescriptor(String functionName) {
		if (nameToDescriptor.containsKey(functionName))
			return nameToDescriptor.get(functionName);
		return jexlDescriptor;
	}

	@Override
	public IFunction getFunction(String functionName) throws FunctionInstantiationFailedException {
		IFunctionDescriptor descriptor = getDescriptor(functionName);
		if (descriptor == jexlDescriptor) {
			return new JexlExpressionFunction(functionName);
		} else {
			return descriptor.getFunction();
		}
	}

	@Override
	public IFunctionDescriptor getDescriptor(IFunction functionInstance) {
		return clazzToDescriptor.get(functionInstance.getClass());
	}

	@Override
	public IFunctionDescriptor[] getFunctionDescriptors() {
		Collection<IFunctionDescriptor> values = clazzToDescriptor.values();
		return values.toArray(new IFunctionDescriptor[values.size()]);
	}
}