/**
 * CodeRegion.java
 * This file is part of the infoZilla framework and tool.
 */
package io.kuy.infozilla.elements.sourcecode.java;

/**
 * The <code>CodeRegion</code> class represents source code structural elements.
 * Each <code>CodeRegion</code> records the <code>start</code> and <code>end</code> 
 * positions in the original input text, as well as the type of the source code element detected.
 * @author Nicolas Bettenburg
 * @see de.unisb.cs.st.infoZilla.Filters.FilterSourceCodeJAVA
 * @see de.unisb.cs.st.infoZilla.Filters.FilterChain
 * @see de.unisb.cs.st.infoZilla.Ressources.Java_CodeDB.txt
 * @see de.unisb.cs.st.infoZilla.Ressources.Java_Keywords.txt
 */
public class CodeRegion implements Comparable<CodeRegion>{
	
	/** Stores the start position of the source code region in the original input text	 */
	public int start=0;
	
	/** Stores the end position of the source code region in the original input text */
	public int end=0;
	
	/** Stores the textual representation of the source code region */
	public String text;
	
	/** Stores the type of source code region as defined in de.unisb.cs.st.infoZilla.Ressources.Java_CodeDB.txt */
	public String keyword;
	
	/**
	 * Standard Constructor
	 * @param start start position of code region
	 * @param end end position of code region
	 * @param keyword type of code region
	 * @param text textual representation
	 */
	public CodeRegion(int start, int end, String keyword, String text) {
		super();
		this.start = start;
		this.end = end;
		this.keyword = keyword;
		this.text = text;
	}
	
	/**
	 * Copy Constructor
	 * @param that another <code>CodeRegion</code> object to copy from.
	 */
	public CodeRegion(CodeRegion that) {
		super();
		this.start = Integer.valueOf(that.start);
		this.end = Integer.valueOf(that.end);
		this.keyword = new String(that.keyword);
		this.text = new String(that.text);
	}
	
	
	public int compareTo(CodeRegion that) {
		if (this.start < that.start) return -1;
		if (this.start > that.start) return +1;
		return 0;
	}
}
