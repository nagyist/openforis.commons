package org.openforis.commons.io.csv;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.openforis.commons.io.flat.FlatDataStream;
import org.openforis.commons.io.flat.FlatRecord;

/**
 * 
 * @author G. Miceli
 *
 */
public class CsvLine implements FlatRecord {
	
	private static final String NA = "NA";
	private List<String> columnNames;
	private String[] line;
	private CsvReader csvReader;
	
	CsvLine(CsvReader csvReader, String[] line) {
		this.columnNames = csvReader.getColumnNames();
		this.csvReader = csvReader;
		this.line = line;
	}
	
	public String[] getLine() {
		return line;
	}

	private String toString(String txt) {
		if ( txt == null || txt.trim().isEmpty() || NA.equals(txt) ) { 		
			return null;
		} else {
			return txt;
		}
	}
	
	private Integer toInteger(String val) {
		return isNullValue(val) ? null : Double.valueOf(val).intValue();
	}

	private Double toDouble(String val) {
		return isNullValue(val) ? null : Double.valueOf(val);
	}
	
	private Long toLong(String val) {
		return isNullValue(val) ? null : Long.valueOf(val);
	}
	
	private boolean isNullValue(String val) {
		return val == null || val.isEmpty() || NA.equals(val);
	}

	private Boolean toBoolean(String val) {
		if ( isNullValue(val) ) {
			return null;
		} else if ( val.equals("1") || 
					val.equalsIgnoreCase("T") || 
					val.equalsIgnoreCase("Y") || 
					val.equalsIgnoreCase("true") ){
			return true;
		} else if ( val.equals("0") || 
					val.equalsIgnoreCase("F") || 
					val.equalsIgnoreCase("N") || 
					val.equalsIgnoreCase("false") ){
			return false;
		} else {
			throw new NumberFormatException("'"+val+"' is not a valid boolean value");
		}
	}

	public Integer getColumnIndex(String column) {
		if ( column == null ) {
			throw new IllegalStateException("Column headers not yet read");
		}
		return columnNames.indexOf(column);
	}

	private Date toDate(String val) {
		try {
			return isNullValue(val) ? null : csvReader.getDateFormat().parse(val);
		} catch (ParseException e) {
			throw DateFormatException.forInputString(val);
		}
	}

	public List<String> getColumnNames() {
		return columnNames;
	}

	@Override
	public Object[] toArray() {
		return line;
	}

	@Override
	public String[] toStringArray() {
		return line;
	}

	@Override
	public FlatDataStream getFlatDataStream() {		
		return csvReader;
	}

	@Override
	public List<String> getFieldNames() {
		return csvReader.getColumnNames();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getValue(int idx, Class<T> type) {
		if (line.length <= idx) {
			return null;
		}
		String value = line[idx];
		if ( type.isAssignableFrom(Integer.class) ) {
			return (T) toInteger(value);
		} else if ( type.isAssignableFrom(Long.class) ) {
			return (T) toLong(value);
		} else if ( type.isAssignableFrom(Double.class) ) {
			return (T) toDouble(value);
		} else if ( type.isAssignableFrom(Boolean.class) ) {
			return (T) toBoolean(value);
		} else if ( type.isAssignableFrom(String.class) ) {
			return (T) toString(value);
		} else if ( type.isAssignableFrom(Date.class) ) {
			return (T) toDate(value);
		} else {
			throw new IllegalArgumentException("Unsupported type "+type);
		}
	}

	@Override
	public <T> T getValue(String column, Class<T> type) {
		Integer idx = getColumnIndex(column);
		return idx == null ? null : getValue(idx, type);
	}

	@Override
	public boolean isMissing(int idx) {
		return line[idx] == null || line[idx].equals(NA) || line[idx].trim().isEmpty(); 
	}

	@Override
	public boolean isMissing(String column) {
		Integer idx = getColumnIndex(column);
		return idx == null || isMissing(idx);
	}
	
	@Override
	public String toString() {
		return Arrays.toString(line);
	}
	
	@Override
	public <T> T getValue(String column, Class<T> type, T defaultValue) {
		try {
			T val = getValue(column, type);
			if ( val == null ) {
				return defaultValue;
			}
			return val;
		} catch ( DateFormatException e ) {
			return defaultValue;
		} catch ( NumberFormatException e ) {
			return defaultValue;
		}
	}
}
 