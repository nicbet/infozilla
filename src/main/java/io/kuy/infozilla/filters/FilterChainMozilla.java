/**
 * FilterChainMozilla.java
 * This file is part of the infoZilla framework and tool.
 */
package io.kuy.infozilla.filters;

import java.util.ArrayList;
import java.util.List;

import io.kuy.infozilla.elements.enumeration.Enumeration;
import io.kuy.infozilla.elements.patch.Patch;
import io.kuy.infozilla.elements.sourcecode.java.CodeRegion;
import io.kuy.infozilla.elements.stacktrace.talkback.TalkbackTrace;
import io.kuy.infozilla.helpers.RegExHelper;


/**
 * Class for runnning the complete filter chain on a Mozilla input and gathering the results
 * @author Nicolas Bettenburg
 *
 */
public class FilterChainMozilla implements FilterChain {
	// Private Attributes
	private FilterPatches patchFilter;
	private FilterTalkBack stacktraceFilter;
	private FilterSourceCodeJAVA sourcecodeFilter;
	private FilterEnumeration enumFilter;
	
	private String inputText = "";
	private String outputText = "";
	
	private List<Patch> patches;
	private List<TalkbackTrace> traces;
	private List<CodeRegion> regions;
	private List<Enumeration> enumerations;
	
	// Constructor runs the experiments
	public FilterChainMozilla(String inputText) {
		patchFilter = new FilterPatches();
		patchFilter.setRelaxed(true);
		
		stacktraceFilter = new FilterTalkBack();
		sourcecodeFilter = new FilterSourceCodeJAVA(FilterChainMozilla.class.getResource("/Java_CodeDB.txt"));
		enumFilter = new FilterEnumeration();
		
		this.inputText = RegExHelper.makeLinuxNewlines(inputText);
		this.outputText = this.inputText;
		
		patches = patchFilter.runFilter(outputText);
		outputText = patchFilter.getOutputText();
					
		traces = stacktraceFilter.runFilter(outputText);
		outputText = stacktraceFilter.getOutputText();
		 
		regions = sourcecodeFilter.runFilter(outputText);
		outputText = sourcecodeFilter.getOutputText();
		 
		enumerations = enumFilter.runFilter(outputText);
		// The output of the filter chain
		outputText = sourcecodeFilter.getOutputText();
	}
	
	public FilterChainMozilla(String inputText, boolean runPatches, boolean runTraces, boolean runSource, boolean runEnums) {
		patchFilter = new FilterPatches();
		//patchFilter.setRelaxed(true);
		stacktraceFilter = new FilterTalkBack();
		sourcecodeFilter = new FilterSourceCodeJAVA(FilterChainMozilla.class.getResource("/Java_CodeDB.txt"));
		enumFilter = new FilterEnumeration();
		
		this.inputText = RegExHelper.makeLinuxNewlines(inputText);
		this.outputText = this.inputText;
		
		if (runPatches) patches = patchFilter.runFilter(outputText);
		else patches = new ArrayList<Patch>();
		
		outputText = patchFilter.getOutputText();
					
		if (runTraces) traces = stacktraceFilter.runFilter(outputText);
		else traces = new ArrayList<TalkbackTrace>();
		
		outputText = stacktraceFilter.getOutputText();
		 
		if (runSource) regions = sourcecodeFilter.runFilter(outputText);
		else regions = new ArrayList<CodeRegion>();
		
		outputText = sourcecodeFilter.getOutputText();
		 
		if (runEnums) enumerations = enumFilter.runFilter(outputText);
		else enumerations = new ArrayList<Enumeration>();
		
		// The output of the filter chain
		outputText = sourcecodeFilter.getOutputText();
	}

	/**
	 * @return the outputText
	 */
	public String getOutputText() {
		return outputText;
	}

	/**
	 * @return the patches
	 */
	public List<Patch> getPatches() {
		return patches;
	}

	/**
	 * @return the traces
	 */
	public List<TalkbackTrace> getTraces() {
		return traces;
	}

	/**
	 * @return the regions
	 */
	public List<CodeRegion> getRegions() {
		return regions;
	}

	/**
	 * @return the enumerations
	 */
	public List<Enumeration> getEnumerations() {
		return enumerations;
	}

	/**
	 * @param inputText the inputText to set
	 */
	public void setInputText(String inputText) {
		this.inputText = inputText;
	}

	/**
	 * @return the inputText
	 */
	public String getInputText() {
		return inputText;
	}
	
}
