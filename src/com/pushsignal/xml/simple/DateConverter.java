package com.pushsignal.xml.simple;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

public class DateConverter implements Converter<Date> {

	private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

	public Date read(final InputNode node) {
		Date date = new Date();
		try {
			date = formatter.parse(node.getValue());
		} catch (final ParseException e) {
			e.printStackTrace();
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return date;
	}

	public void write(final OutputNode node, final Date date) {
		node.setValue(formatter.format(date));
	}
}