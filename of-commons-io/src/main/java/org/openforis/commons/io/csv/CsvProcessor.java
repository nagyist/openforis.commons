package org.openforis.commons.io.csv;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author G. Miceli
 */
public abstract class CsvProcessor {

	private DateFormat dateFormat;
	private Map<String, Integer> columns;
	
	public DateFormat getDateFormat() {
		if ( dateFormat == null ) {
			setDateFormat("yyyy-MM-dd");
		}
		return dateFormat;
	}

	public void setDateFormat(DateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}

	public void setDateFormat(String pattern) {
		this.dateFormat = new SimpleDateFormat(pattern);
	}

	public List<String> getColumnNames() {
		return Collections.unmodifiableList(new ArrayList<String>(columns.keySet()));
	}
	
	Map<String, Integer> getColumnIndices() {
		return Collections.unmodifiableMap(columns);
	}
	
	protected void setColumnNames(String[] headers) {
		columns = new LinkedHashMap<String, Integer>();
		for (int i = 0; i < headers.length; i++) {
			String header = headers[i];
			if ( header == null || header.trim().isEmpty() ) {
				throw new IllegalArgumentException("Empty column heading at index: " + i);
			}
			if ( columns.containsKey(header) ) {
				throw new IllegalArgumentException("Duplicate header: " + header);
			}
			columns.put(header, i);
		}
	}
}