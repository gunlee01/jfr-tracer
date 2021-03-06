/*
 * Copyright (c) 2018, Marcus Hirt
 * 
 * jfr-tracer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jfr-tracer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jfr-tracer. If not, see <http://www.gnu.org/licenses/>.
 */
package se.hirt.jmc.opentracing.jfr;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.StackTrace;

import io.opentracing.Span;
import se.hirt.jmc.opentracing.ContextExtractor;
import se.hirt.jmc.opentracing.extractors.ExtractorRegistry;

/**
 * This is the JDK 9 or later implementation of the JfrEmitter.
 * 
 * @author Marcus Hirt
 */
public class JfrSpanEmitterImpl extends AbstractJfrSpanEmitterImpl {
	private Jdk9SpanEvent currentEvent;

	@Label("Span Event")
	@Description("Open tracing event corresponding to a span.")
	@Category("Open Tracing")
	@StackTrace(false)
	private static class Jdk9SpanEvent extends Event {
		@Label("Operation Name")
		@Description("The operation name for the span")
		private String operationName;
		
		@Label("Trace Id")
		@Description("The trace id for the span")
		private String traceId;

		@Label("Span Id")
		@Description("The id of the parent span")
		private String spanId;

		@Label("Parent Id")
		@Description("The id of the parent span")
		private String parentId;
	}
	
	private static class EndEventCommand implements Runnable {
		private final Jdk9SpanEvent event;

		public EndEventCommand(Jdk9SpanEvent event) {
			this.event = event;
		}

		@Override
		public void run() {
			event.commit();
		}
	}

	private static class BeginEventCommand implements Runnable {
		private final Jdk9SpanEvent event;

		public BeginEventCommand(Jdk9SpanEvent event) {
			this.event = event;
		}

		@Override
		public void run() {
			event.begin();
		}
	}

	JfrSpanEmitterImpl(Span span, ContextExtractor extractor) {
		super(span, extractor);
	}

	@Override
	public void close() throws Exception {
		if (currentEvent != null) {
			EXECUTOR.execute(new EndEventCommand(currentEvent));
			currentEvent = null;
		} else {
			LOGGER.warning("Close without start discovered!");
		}
	}

	@Override
	public void start() {
		currentEvent = new Jdk9SpanEvent();
		if (extractor != null) {
			currentEvent.operationName = extractor.extractOperationName(span);
			currentEvent.traceId = extractor.extractTraceId(span);
			currentEvent.spanId = extractor.extractSpanId(span);
			currentEvent.parentId = extractor.extractParentId(span);
		} else {
			LOGGER.warning(
					"Trying to create event when no valid extractor is available. Create an extractor for your particular open tracing tracer implementation, and register it with the ExtractorRegistry.");
		}
		EXECUTOR.execute(new BeginEventCommand(currentEvent));
	}

	@Override
	public String toString() {
		return "JDK 9+ JFR Emitter for " + extractor.getSupportedTracerType() + "/" + extractor.getSupportedSpanType();
	}
}
