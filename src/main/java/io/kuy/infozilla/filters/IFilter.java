/**
 * IFilter.java
 * This file is part of the infoZilla framework and tool.
 */
package io.kuy.infozilla.filters;

import java.util.List;

/**
 * This interface describes the method interface for every infoZilla Filter.
 * @author Nicolas Bettenburg
 *
 */
public interface IFilter {

	public List<?> runFilter(String inputText); 
	public String getOutputText();
	
}
