import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class RafMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			
			byte[] lol = getBytesFromFile(new File("./res/vueltaSA.raf"));
			int index = getThFromArray(lol);
			
			// Salida
			BufferedWriter out = new BufferedWriter(new FileWriter("./res/csv_"+System.currentTimeMillis()+".txt"));
			
			for (int i = 0; i<index; i++) {
				
				// Distancia de indice
				float distancia = leerFloatDinamico(lol, i, 48);
				
				// Marcha 0=R 1=N 2=1 ...
				String marcha = leerCampoDinamico(lol, i, 20, 1);
				int marcha_int = Integer.parseInt(marcha);
				marcha_int--;
				
				// Revoluciones
				float rads = leerFloatDinamico(lol,i,44);
				float rpm = (rads/(2*(float)Math.PI))*60;
				
				// Velocidad
				float velocidad_ms = leerFloatDinamico(lol, i, 24);
				float velocidad_kmh = velocidad_ms*18/5.0f;
				
				// Posici—n acelerador
				float pacelerador = 100*leerFloatDinamico(lol, i, 0);
				// Posici—n freno
				float pfreno = 100*leerFloatDinamico(lol, i, 4);
				// Posici—n embrague
				float pembrague = 100*leerFloatDinamico(lol, i, 12);
				// Posicion freno de mano
				float pfmano = 100*leerFloatDinamico(lol, i, 16);
				// Posici—n de la direcci—n
				float direccion_rad = leerFloatDinamico(lol, i, 8);
				float direccion_grad = (direccion_rad*180)/3.141592f;
				
				// FUERZAS G
				// Fuerza G Lateral
				int lateralG_raw = leerCharDinamico(lol, i, 21);
				float lateralG = 100*lateralG_raw/20.0f;
				int frontalG_raw = leerCharDinamico(lol, i, 22);
				float frontalG = 100*frontalG_raw/20.0f;
				int superiorG_raw = leerCharDinamico(lol, i, 23);
				float superiorG = 100*superiorG_raw/20.0f;
				
				System.out.println("at " + thOfSecondToString(i) +" ("+distancia+"m) car was at " + marcha +" gear and engine was "+ rpm + " rpm @ " + velocidad_kmh + " km/h");
				System.out.println("[A:"+pacelerador+" B:"+pfreno+" C:"+pembrague+" HB:"+pfmano+" STEER:"+direccion_grad+"¼]");
				System.out.println("G FORCE [SIDE: "+lateralG+"G FORWARD: "+frontalG+"G SUPERIOR: "+superiorG+"G ]");
				
				out.write(i+","
						+Math.round(distancia)+","
						+marcha_int+","+Math.round(rpm)+","
						+Math.round(velocidad_kmh)+","
						+pacelerador+","
						+pfreno+","
						+pembrague+","
						+lateralG+","
						+frontalG+","
						+superiorG+"\r\n");
			}
			
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static String leerCampoDinamico (byte[] contenido,int millisecond, int offset, int size) {
		
		int realoffset = 1024+(192*millisecond)+offset;
		
		StringBuffer cadena = new StringBuffer();
		for (int i = realoffset; i < (realoffset+size); i++) {
			cadena.append(Integer.toHexString(contenido[i]));
		}
		
		return cadena.toString();
		
	}

	public static float leerFloatDinamico (byte[] contenido,int millisecond, int offset) {
		
		int realoffset = 1024+(192*millisecond)+offset;
		int size = 4;
		
		byte[] buffer = new byte[4];
		                         
		for (int i = realoffset; i < (realoffset+size); i++) {
			buffer[i-realoffset] = contenido[i];
		}
		
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.put(buffer);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		float valor = bb.getFloat(0);
		
		return valor;
	}
	
	public static int leerCharDinamico (byte[] contenido,int millisecond, int offset) {
		
		int realoffset = 1024+(192*millisecond)+offset;
		int ret = contenido[realoffset];
		return ret;
		
	}
	
	public static String thOfSecondToString(int th) {
		int decimas = th % 10;
		int segundos = (th / 10) % 60;
		int minutos = (th / 600);
		
		return minutos+":"+segundos+"."+decimas;
	}
	
	public static byte[] getBytesFromFile(File file) throws IOException {
	    InputStream is = new FileInputStream(file);

	    // Get the size of the file
	    long length = file.length();

	    // You cannot create an array using a long type.
	    // It needs to be an int type.
	    // Before converting to an int type, check
	    // to ensure that file is not larger than Integer.MAX_VALUE.
	    if (length > Integer.MAX_VALUE) {
	        // File is too large
	    }

	    // Create the byte array to hold the data
	    byte[] bytes = new byte[(int)length];

	    // Read in the bytes
	    int offset = 0;
	    int numRead = 0;
	    while (offset < bytes.length
	           && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	        offset += numRead;
	    }

	    // Ensure all the bytes have been read in
	    if (offset < bytes.length) {
	        throw new IOException("Could not completely read file "+file.getName());
	    }

	    // Close the input stream and return bytes
	    is.close();
	    return bytes;
	}
	
	public static int getThFromArray(byte[] contenido) {
		
		int nbytes = contenido.length;
		
		return (nbytes-1024)/192;
	
	}

}
