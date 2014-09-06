package it.albertus.cycles;

import it.albertus.cycles.model.Bike;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyclesMod {
	
	private static final Logger log = LoggerFactory.getLogger( CyclesMod.class );
	
	/*
	 * Little-endian: Il byte piu' significativo (msB) e' alla posizione dispari.
	 * 
	 * 0-1: numero di marce del cambio (solo lsB)
	 * 
	 * 2-3: regime red mark (stessa scala del regime limitatore, il motore si rompe quando regime >= regime red mark per piu' di qualche secondo)
	 * 	    msB default: 125, 250: 0x2F (47); 500: 0x31 (49).
	 * 
	 * 4-5: regime limitatore (il contagiri si blocca ma la moto conserva le prestazioni di accelerazione a quel regime, quindi la coppia a quel regime deve essere azzerata)
	 *    msB  | RPM
	 *    0x27: 10000, 
	 *    0x28: 10200, 
	 *    0x29: 10500, 
	 *    0x2A: 10600,
	 *    0x2B: 10800, 
	 *    0x2C: 11300, 
	 *    0x2D: 11800,
	 *    0x2E: 12000, 
	 *    0x2F: 12500, 
	 *    0x30: 12800, 
	 *    0x31: 13000, 
	 *    0x32: 13100, 
	 *    0x35: 14000, 
	 *    0x36: 14100, default (54)
	 *    0x37: 14200, 
	 *    0x38: 14300, 
	 *    0x39: 14500, 
	 *    0x3A: 15000)
	 *    
	 * 6-7: periodo di grazia su red mark (valore alto: il motore si rompe dopo piu' tempo. Per valori msB >=80 si rompe subito)
	 * 
	 * 8-9: soglia di slittamento in sterzata (valore alto: slitta meno)
	 * 
	 * 10-11: ?
	 * 
	 * 12-13: velocita' di frenata
	 * 
	 * 14-15: ?
	 * 
	 * 16-17: soglia di testacoda (valore basso: testacoda piu' probabile. Per valori msB >=80 testacoda sicuro)
	 * 
	 * 18-19: ?
	 * 
	 * 20-21: ?
	 * 
	 * (23), 25, 27, 29, 31, 33, 35: rapporti cambio (N), 1, 2, 3, 4, 5, 6; solo byte piu' significativo.
	 * 
	 * 36-147: curva di coppia (max regime considerato: 14500 RPM)
	 * 
	 */
	
//	private static final String PATH = "c:\\users\\alberto\\desktop";

	public static void main( String... args ) throws IOException, InstantiationException, IllegalAccessException {
		InputStream is = CyclesMod.class.getResourceAsStream( "/BIKES.INF" );
		log.info( "File BIKES.INF originale aperto." );
				
		byte[] inf125 = new byte[ Bike.LENGTH ];
		byte[] inf250 = new byte[ Bike.LENGTH ];
		byte[] inf500 = new byte[ Bike.LENGTH ];
		
		is.read( inf125 );
		is.read( inf250 );
		is.read( inf500 );
		log.info( "File BIKES.INF originale letto." );
		
		is.close();
		
//		monitor(inf125, inf250, inf500);
		
		Bike bike125 = new Bike( inf125 );
		Bike bike250 = new Bike( inf250 );
		Bike bike500 = new Bike( inf500 );
		log.info( "File BIKES.INF originale elaborato." );
		
		FileBikesInf file = new FileBikesInf( bike125, bike250, bike500 );
		
		
		// Inizio modifiche...

		// Fine modifiche.
		
		
		// File in uscita
		log.info( "Preparazione nuovo file BIKES.INF..." );
		FileOutputStream fos = new FileOutputStream( "D:\\Documents\\Dropbox\\Personale\\DOS\\CYCLES\\BIKES.INF" );
		fos.write( file.toByteArray() );
		fos.flush();
		fos.close();
		log.info( "Nuovo file BIKES.INF scritto." );
	}

	private static void monitor(byte[] inf125, byte[] inf250, byte[] inf500) {
		// 125
		for ( int i = 0; i < 148; i++ ) {
			System.out.print( StringUtils.leftPad( new Integer( i ).toString(), 3, ' ' ) + "|");
		}
		System.out.println();
		for ( int i = 0; i < 148*4; i++ ) {
			System.out.print( "-" );
		}
		System.out.println();
		for ( byte b : inf125 ) {
			System.out.print( StringUtils.leftPad( new Integer(b & 0xFF).toString(), 3, ' ' ) + "|");
		}
		
		System.out.println("\n\n");
		
		// 250
		for ( int i = 0; i < 148; i++ ) {
			System.out.print( StringUtils.leftPad( new Integer( i ).toString(), 3, ' ' ) + "|");
		}
		System.out.println();
		for ( int i = 0; i < 148*4; i++ ) {
			System.out.print( "-" );
		}
		System.out.println();
		for ( byte b : inf250 ) {
			System.out.print( StringUtils.leftPad( new Integer(b & 0xFF).toString(), 3, ' ' ) + "|");
		}
		
		System.out.println("\n\n");
		
		// 500
		for ( int i = 0; i < 148; i++ ) {
			System.out.print( StringUtils.leftPad( new Integer( i ).toString(), 3, ' ' ) + "|");
		}
		System.out.println();
		for ( int i = 0; i < 148*4; i++ ) {
			System.out.print( "-" );
		}
		System.out.println();
		for ( byte b : inf500 ) {
			System.out.print( StringUtils.leftPad( new Integer(b & 0xFF).toString(), 3, ' ' ) + "|");
		}
	}
	
}