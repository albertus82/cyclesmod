package it.albertus.cycles.resources;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Messages {
	
	private static final ResourceBundle messages = ResourceBundle.getBundle( Messages.class.getName() );
	
	public static String get( final String key, final Object... params ) {
		List<String> stringParams = new ArrayList<String>( params.length );
		for ( Object param : params ) {
			stringParams.add( param.toString() );
		}
		return MessageFormat.format( messages.getString( key ), stringParams.toArray() );
	}
}