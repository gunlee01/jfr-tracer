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
package se.hirt.jmc.opentracing;

import java.util.Map;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import se.hirt.jmc.opentracing.jfr.JfrEmitter;
import se.hirt.jmc.opentracing.jfr.JfrEmitterFactory;

/**
 * Wrapper for {@link Span}.
 * 
 * @author Marcus Hirt
 */
final class SpanWrapper implements Span {
	static JfrEmitterFactory EMITTER_FACTORY = new JfrEmitterFactory();

	private final Span delegate;
	private final JfrEmitter spanEmitter;

	SpanWrapper(Span delegate, ContextExtractor extractor) {
		this.delegate = delegate;
		spanEmitter = EMITTER_FACTORY.createSpanEmitter(delegate, extractor);
	}

	@Override
	public SpanContext context() {
		return delegate.context();
	}

	@Override
	public Span setTag(String key, String value) {
		delegate.setTag(key, value);
		return this;
	}

	@Override
	public Span setTag(String key, boolean value) {
		delegate.setTag(key, value);
		return this;
	}

	@Override
	public Span setTag(String key, Number value) {
		delegate.setTag(key, value);
		return this;
	}

	@Override
	public Span log(Map<String, ?> fields) {
		delegate.log(fields);
		return this;
	}

	@Override
	public Span log(long timestampMicroseconds, Map<String, ?> fields) {
		delegate.log(timestampMicroseconds, fields);
		return this;
	}

	@Override
	public Span log(String event) {
		delegate.log(event);
		return this;
	}

	@Override
	public Span log(long timestampMicroseconds, String event) {
		delegate.log(timestampMicroseconds, event);
		return this;
	}

	@Override
	public Span setBaggageItem(String key, String value) {
		delegate.setBaggageItem(key, value);
		return this;
	}

	@Override
	public String getBaggageItem(String key) {
		return delegate.getBaggageItem(key);
	}

	@Override
	public Span setOperationName(String operationName) {
		delegate.setOperationName(operationName);
		return this;
	}

	@Override
	public void finish() {
		delegate.finish();
		try {
			spanEmitter.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void finish(long finishMicros) {
		delegate.finish(finishMicros);
		try {
			spanEmitter.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Span getDelegate() {
		return delegate;
	}

	public void start() {
		spanEmitter.start();
	}
}
