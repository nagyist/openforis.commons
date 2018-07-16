package org.openforis.commons.io.csv;

import static au.com.bytecode.opencsv.CSVWriter.NO_QUOTE_CHARACTER;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.openforis.commons.io.OpenForisIOUtils;
import org.openforis.commons.io.flat.FlatDataWriter;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * @author G. Miceli
 * @author S. Ricci
 */
public class CsvWriter extends FlatDataWriter {
	
	private static final char COMMA = ',';
	private static final char DEFAULT_SEPARATOR = COMMA;
	
	private CSVWriter csvWriter;
	
	public CsvWriter(Writer writer) {
		this(writer, DEFAULT_SEPARATOR, NO_QUOTE_CHARACTER);
	}
	
	/**
	 * Constructs the writer using the specified {@link OutputStream} to write the CSV file.
	 * The default charset encoding will be UTF_8
	 */
	public CsvWriter(OutputStream out) throws UnsupportedEncodingException {
		this(out, OpenForisIOUtils.UTF_8, DEFAULT_SEPARATOR, NO_QUOTE_CHARACTER);
	}

	/**
	 * Constructs the writer using the specified {@link OutputStream} and the specified charset encoding to write the CSV file.
	 */
	public CsvWriter(OutputStream out, String charsetName) throws UnsupportedEncodingException {
		this(out, charsetName, DEFAULT_SEPARATOR, NO_QUOTE_CHARACTER);
	}
	
	public CsvWriter(OutputStream out, String charsetName, char separator, char quotechar) throws UnsupportedEncodingException {
		this(new BufferedWriter(new OutputStreamWriter(out, charsetName)), separator, quotechar);
	}

	public CsvWriter(Writer writer, char separator, char quoteChar) {
		csvWriter = new CSVWriter(writer, separator, quoteChar);
	}
	
	@Override
	protected void writeNextInternal(Object[] values) {
		String[] stringValues = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			Object val = values[i];
			String stringVal = val == null ? null : val.toString();
			stringValues[i] = stringVal;
		}
		csvWriter.writeNext(stringValues);
	}
	
	@Override
	public void flush() throws IOException {
		csvWriter.flush();
	}
	
	@Override
	public void close() throws IOException {
		csvWriter.close();
	}
}