package org.lucee.extension.toon;

import java.util.List;
import java.util.Map;

import com.cedarsoftware.io.JsonIo;
import com.cedarsoftware.io.WriteOptions;
import com.cedarsoftware.io.WriteOptionsBuilder;

import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;

/**
 * SerializeTOON - Serialize CFML data to TOON (Token-Oriented Object Notation) format.
 *
 * TOON is an LLM-optimized format that uses ~40-50% fewer tokens than JSON.
 * See https://toonformat.dev for format specification.
 *
 * Usage:
 *   result = SerializeTOON( data )
 *   result = SerializeTOON( data, { cycleSupport: false, prettyPrint: true } )
 */
public class SerializeTOON extends BIF {

	private static final long serialVersionUID = 1L;

	public static String call( PageContext pc, Object var ) throws PageException {
		return call( pc, var, null );
	}

	public static String call( PageContext pc, Object var, Struct options ) throws PageException {
		// Set thread context classloader to json-io's classloader so it can find its resources
		ClassLoader originalCL = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader( JsonIo.class.getClassLoader() );
		try {
			CFMLEngine eng = CFMLEngineFactory.getInstance();

			// Build WriteOptions
			WriteOptionsBuilder builder = new WriteOptionsBuilder();

			boolean prettyPrint = false;

			if ( options != null && options.size() > 0 ) {
				// cycleSupport is not supported due to json-io bug with Map/Collection types
				Object cycleSupportVal = options.get( "cycleSupport", null );
				if ( cycleSupportVal != null && eng.getCastUtil().toBooleanValue( cycleSupportVal ) ) {
					throw eng.getExceptionUtil().createFunctionException( pc, "SerializeTOON", 2, "options",
						"cycleSupport option is not currently supported - json-io does not detect cycles in Map/Collection types", null );
				}

				Object prettyPrintVal = options.get( "prettyPrint", null );
				if ( prettyPrintVal != null ) {
					prettyPrint = eng.getCastUtil().toBooleanValue( prettyPrintVal );
				}
			}

			// Disable cycle support - it doesn't work with Map/Collection types
			builder.cycleSupport( false );
			if ( prettyPrint ) {
				builder.prettyPrint( true );
			}

			WriteOptions writeOptions = builder.build();

			// Convert CFML types to Java types for json-io
			Object javaData = toJavaObject( var );

			// Serialize to TOON
			return JsonIo.toToon( javaData, writeOptions );
		}
		catch ( Exception e ) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException( e );
		}
		finally {
			Thread.currentThread().setContextClassLoader( originalCL );
		}
	}

	/**
	 * Convert CFML types to standard Java types for json-io
	 */
	private static Object toJavaObject( Object data ) {
		// CFML Struct implements Map, Array implements List
		// So they should work directly with json-io
		return data;
	}

	@Override
	public Object invoke( PageContext pc, Object[] args ) throws PageException {
		CFMLEngine eng = CFMLEngineFactory.getInstance();

		if ( args.length < 1 ) {
			throw eng.getExceptionUtil()
				.createFunctionException( pc, "SerializeTOON", 1, "var", "Data to serialize is required", null );
		}

		Object var = args[0];
		Struct options = null;

		if ( args.length >= 2 && args[1] != null ) {
			options = eng.getCastUtil().toStruct( args[1] );
		}

		return call( pc, var, options );
	}
}
