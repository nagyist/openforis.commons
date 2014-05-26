package org.openforis.commons.io.csv;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.List;

import org.openforis.commons.io.OpenForisIOUtils;
import org.openforis.commons.io.flat.FlatDataStream;
import org.openforis.commons.io.flat.FlatRecord;

import au.com.bytecode.opencsv.CSVWriter;
import static au.com.bytecode.opencsv.CSVWriter.*;

/**
 * @author G. Miceli
 * @author S. Ricci
 */
public class CsvWriter extends CsvProcessor implements Closeable {
	private CSVWriter csvWriter;
	private long linesWritten; 
	private boolean headersWritten;
	
	public CsvWriter(Writer writer) {
		this(writer, ',', NO_QUOTE_CHARACTER);
	}
	
	public CsvWriter(Writer writer, char separator, char quotechar) {
		csvWriter = new CSVWriter(writer, separator, quotechar);
		linesWritten = 0;
		headersWritten = false;
	}

	/**
	 * Constructs the writer using the specified {@link OutputStream} to write the CSV file.
	 * The default charset encoding will be UTF_8
	 */
	public CsvWriter(OutputStream out) throws UnsupportedEncodingException {
		this(out, OpenForisIOUtils.UTF_8);
	}

	/**
	 * Constructs the writer useing the specified {@link OutputStream} and the specified charset encoding to write the CSV file.
	 */
	public CsvWriter(OutputStream out, String charsetName) throws UnsupportedEncodingException {
		this(new BufferedWriter(new OutputStreamWriter(out, charsetName)));
	}

	public void writeAll(FlatDataStream in) throws IOException {
		FlatRecord r = in.nextRecord();
		if ( r == null ) {
			return;
		}
    	List<String> fieldNames = in.getFieldNames();
    	String[] headers = fieldNames.toArray(new String[fieldNames.size()]);
    	writeHeaders(headers);
		
		while ( r != null ) {
			writeNext(r);
			r = in.nextRecord();
		}
	}

	public void flush() throws IOException {
		csvWriter.flush();
	}
	
	@Override
	public void close() throws IOException {
		csvWriter.close();
	}
	
	public void writeNext(FlatRecord r) {
		String[] line = r.toStringArray();
		writeNext(line);
	}

	public void writeNext(String[] line) {
		csvWriter.writeNext(line);
		linesWritten++;
	}

	public void writeHeaders(String[] headers) {
		if ( headersWritten ) {
			throw new IllegalStateException("Headers already written");
		}
		setColumnNames(headers);
    	csvWriter.writeNext(headers);
    	headersWritten = true;
	}

	public long getLinesWritten() {
		return linesWritten;
	}
	
	public boolean isHeadersWritten() {
		return headersWritten;
	}
}
