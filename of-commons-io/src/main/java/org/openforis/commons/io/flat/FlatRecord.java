package org.openforis.commons.io.flat;

import java.util.List;

/**
 * @author G. Miceli
 */
public interface FlatRecord {

	Object[] toArray();

	String[] toStringArray();

	FlatDataStream getFlatDataStream();

	List<String> getFieldNames();

	<T> T getValue(int idx, Class<T> type);
	
	<T> T getValue(String name, Class<T> type);

	boolean isMissing(int idx);
	
	boolean isMissing(String name);
	
	boolean isEmpty();
	
	<T> T getValue(String string, Class<T> type, T object);
}