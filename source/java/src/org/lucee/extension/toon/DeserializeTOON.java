package org.lucee.extension.toon;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.cedarsoftware.io.JsonIo;
import com.cedarsoftware.io.ReadOptions;
import com.cedarsoftware.io.ReadOptionsBuilder;

import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;

/**
 * DeserializeTOON - Deserialize TOON (Token-Oriented Object Notation) to CFML data.
 *
 * TOON is an LLM-optimized format that uses ~40-50% fewer tokens than JSON.
 * See https://toonformat.dev for format specification.
 *
 * Usage:
 *   result = DeserializeTOON( toonString )
 *   result = DeserializeTOON( filePath )
 *   result = DeserializeTOON( source, { charset: "UTF-8" } )
 */
public class DeserializeTOON extends BIF {

	private static final long serialVersionUID = 1L;

	public static Object call( PageContext pc, String source ) throws PageException {
		return call( pc, source, null );
	}

	public static Object call( PageContext pc, String source, Struct options ) throws PageException {
		// Set thread context classloader to json-io's classloader so it can find its resources
		ClassLoader originalCL = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader( JsonIo.class.getClassLoader() );
		try {
			CFMLEngine eng = CFMLEngineFactory.getInstance();

			if ( source == null || source.trim().isEmpty() ) {
				throw eng.getExceptionUtil()
					.createFunctionException( pc, "DeserializeTOON", 1, "source", "Source cannot be empty", null );
			}

			String toonContent = source.trim();

			// Check if source is a file path
			if ( isFilePath( toonContent ) ) {
				Charset charset = StandardCharsets.UTF_8;

				if ( options != null && options.size() > 0 ) {
					Object charsetVal = options.get( "charset", null );
					if ( charsetVal != null ) {
						charset = Charset.forName( eng.getCastUtil().toString( charsetVal ) );
					}
				}

				toonContent = readFile( toonContent, charset );
			}

			// Parse TOON to Map (class-independent)
			Object result = JsonIo.fromToonToMaps( toonContent ).asClass( null );

			// Convert Java types back to CFML types
			return toCFMLObject( eng, result );
		}
		catch ( PageException pe ) {
			throw pe;
		}
		catch ( Exception e ) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException( e );
		}
		finally {
			Thread.currentThread().setContextClassLoader( originalCL );
		}
	}

	/**
	 * Check if the source looks like a file path rather than TOON content
	 */
	private static boolean isFilePath( String source ) {
		// TOON content starts with specific markers or looks like data
		// File paths contain path separators or file extensions
		if ( source.startsWith( "{" ) || source.startsWith( "[" ) || source.startsWith( "(" ) ) {
			return false;
		}

		// Check for common file path patterns
		if ( source.contains( "/" ) || source.contains( "\\" ) ) {
			// Could be a path - check if file exists
			File file = new File( source );
			return file.exists() && file.isFile();
		}

		// Check for .toon extension
		if ( source.toLowerCase().endsWith( ".toon" ) ) {
			File file = new File( source );
			return file.exists() && file.isFile();
		}

		return false;
	}

	/**
	 * Read file contents
	 */
	private static String readFile( String filePath, Charset charset ) throws IOException {
		return Files.readString( Path.of( filePath ), charset );
	}

	/**
	 * Convert Java result back to CFML types
	 */
	private static Object toCFMLObject( CFMLEngine eng, Object result ) throws PageException {
		if ( result == null ) {
			return "";
		}
		// Simple types pass through
		if ( result instanceof String || result instanceof Number || result instanceof Boolean ) {
			return result;
		}
		// Already a CFML type
		if ( result instanceof Struct || result instanceof Array ) {
			return result;
		}
		// Java Map - convert to Struct
		if ( result instanceof Map ) {
			Struct struct = eng.getCreationUtil().createStruct();
			Map<?, ?> map = (Map<?, ?>) result;
			for ( Map.Entry<?, ?> entry : map.entrySet() ) {
				struct.set( String.valueOf( entry.getKey() ), toCFMLObject( eng, entry.getValue() ) );
			}
			return struct;
		}
		// Java List - convert to Array
		if ( result instanceof List ) {
			Array arr = eng.getCreationUtil().createArray();
			List<?> list = (List<?>) result;
			for ( Object item : list ) {
				arr.append( toCFMLObject( eng, item ) );
			}
			return arr;
		}
		// Java array
		if ( result.getClass().isArray() ) {
			Array arr = eng.getCreationUtil().createArray();
			Object[] objArr = (Object[]) result;
			for ( Object item : objArr ) {
				arr.append( toCFMLObject( eng, item ) );
			}
			return arr;
		}
		// Fallback
		return result;
	}

	@Override
	public Object invoke( PageContext pc, Object[] args ) throws PageException {
		CFMLEngine eng = CFMLEngineFactory.getInstance();

		if ( args.length < 1 ) {
			throw eng.getExceptionUtil()
				.createFunctionException( pc, "DeserializeTOON", 1, "source", "TOON source is required", null );
		}

		String source = eng.getCastUtil().toString( args[0] );
		Struct options = null;

		if ( args.length >= 2 && args[1] != null ) {
			options = eng.getCastUtil().toStruct( args[1] );
		}

		return call( pc, source, options );
	}
}
