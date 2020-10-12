/*
 * *******************************************************************************
 *  * Copyright (c) 2018-2020 Edgeworx, Inc.
 *  *
 *  * This program and the accompanying materials are made available under the
 *  * terms of the Eclipse Public License v. 2.0 which is available at
 *  * http://www.eclipse.org/legal/epl-2.0
 *  *
 *  * SPDX-License-Identifier: EPL-2.0
 *  *******************************************************************************
 *
 */
package org.eclipse.iofog.logging;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * formats logs 
 * [MM/dd/yyyy hh:mm:ss.SSS] [WARN/INFO] [MODULE] : Message
 * 
 * @author saeid
 *
 */
public class LogFormatter extends Formatter {
	public String format(LogRecord record) {
		final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		JsonBuilderFactory factory = Json.createBuilderFactory(null);
		JsonObjectBuilder jsonObjectBuilder = factory.createObjectBuilder();
		jsonObjectBuilder.add("timestamp", df.format(System.currentTimeMillis()));
		jsonObjectBuilder.add("level", record.getLevel().toString());
		jsonObjectBuilder.add("thread", record.getSourceClassName());
		jsonObjectBuilder.add("module", record.getSourceMethodName());
		jsonObjectBuilder.add("message", record.getMessage());
		if (record.getThrown() != null) {
			jsonObjectBuilder.add("exception_message", record.getThrown().getLocalizedMessage());
			jsonObjectBuilder.add("stacktrace", record.getThrown().getStackTrace().toString());
		}
		return jsonObjectBuilder.build().toString().concat("\n");
	}

}
