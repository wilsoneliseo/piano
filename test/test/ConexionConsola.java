package test;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;
import net.Network;
import net.Network_iface;

/**
 * Realiza la conexión serial con Arduino. Realiza la conexión serial. La
 * conexión se realiza con los valores por default de arduino, es decir, a
 * {@value #speed} baudios, de 8 bis, no paridad.
 * 
 * @author wegt
 * @see Network#connect(String, int)
 */
public class ConexionConsola implements Network_iface {
	public static int speed = 9600;
	private static Network network;
	private static boolean resend_active = false;
	private static final int divider=255;
	    

	/**
	 * Interacci�n en consola. Lista los puertos seriales disponibles. Se establece 
	 * el puerto de la conexi�n, pregunta que datos enviar. Desde que inicia se
	 * crea un hilo para estar revisando si hay datos de entrada. Si los hay lo
	 * muestra en consola.  
	 * 
	 * @param args arreglo de string de un elemento que establece la velocidad 
	 * de conexi�n. Si no se le pasa nada se toma {@value #speed}.
	 */
	public void correr(String[] args) {

		network = new net.Network(0, new ConexionConsola(), divider);

		// leyendo velocidad
		if (args.length > 0) {
			try {
				speed = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				System.out.println("la velocidad debe ser un entero\n");
				System.exit(1);
			}
		}

		// inicializando reader de la linea de comandos
		int i, inp_num = 0;
		String input;
		BufferedReader in_stream = new BufferedReader(new InputStreamReader(System.in));

		// obtiendo una lista de puertos disponibles
		Vector<String> ports = network.getPortList();

		// eligiendo el puerto a conectar
		System.out.println();
		if (ports.size() > 0) {
			System.out.println("Los siguientes puertos seriales han sido detectados:\n");
		} else {
			System.out.println("lo siento, no fueron encontrados puertos seriales en su computadora.\n");
			System.exit(0);
		}
		for (i = 0; i < ports.size(); ++i) {
			System.out.println("    " + Integer.toString(i + 1) + ":  " + ports.elementAt(i));
		}
		boolean valid_answer = false;
		while (!valid_answer) {
			System.out.println("ingrese el id (1,2,...) de la conexion para conectarse: ");
			try {
				input = in_stream.readLine();
				inp_num = Integer.parseInt(input);
				if ((inp_num < 1) || (inp_num >= ports.size() + 1)) {
					System.out.println("su intrada no es valida");
				} else {
					valid_answer = true;
				}
			} catch (NumberFormatException ex) {
				System.out.println("por favor ingrese el numero correcto");
			} catch (IOException e) {
				System.out.println("ocurrio un error de entrada\n");
				System.exit(1);
			}
		}

		// conectando el puerto seleccionado
		if (network.connect(ports.elementAt(inp_num - 1), speed)) {
			System.out.println();
		} else {
			System.out.println("lo siento, fue encontrado un error de conexion\n");
			System.exit(1);
		}

		// preguntando si el usuario quiere reflejar el tr�fico
		System.out.println("�Quieres que esta herramienta devuelva todos los mensajes recibidos?");
		valid_answer = false;
		while (!valid_answer) {
			System.out.println("'y' para si o  'n' para no: ");
			try {
				input = in_stream.readLine();
				if (input.equals("y")) {
					resend_active = true;
					valid_answer = true;
				} else if (input.equals("n")) {
					valid_answer = true;
				} else if (input.equals("q")) {
					System.out.println("ejemplo terminado\n");
					System.exit(0);
				}
			} catch (IOException e) {
				System.out.println("hubo un error de entrada\n");
				System.exit(1);
			}
		}

		// leer en n�meros (bytes) para ser enviados a trav�s del puerto serie
		System.out.println("escriba 'q' para finalizar el ejemplo");
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
			}
			System.out.println("\ningrese un número entre 0 y 254 para enviar ('q' para salir):");
			try {
				input = in_stream.readLine();
				if (input.equals("q")) {
					System.out.println("ejemplo terminado\n");
					network.disconnect();
					System.exit(0);
				}
				inp_num = Integer.parseInt(input);
				if ((inp_num > 255) || (inp_num < 0)) {
					System.out.println("el número que ingresó no es válido");
				} else {
					int temp[] = { inp_num };
					network.writeSerial(1, temp);
					System.out.println("enviar " + inp_num + " sobre el puerto serie");
				}
			} catch (NumberFormatException ex) {
				System.out.println("por favor ingrese un número correcto");
			} catch (IOException e) {
				System.out.println("hubo un error de entrada");
			}
		}

	}

	@Override
	public void networkDisconnected(int id) {
		System.exit(0);
	}

	@Override
	public void writeLog(int id, String text) {
		System.out.println("   log:  |" + text + "|");
	}

	@Override
	public void parseInput(int id, int numBytes, int[] message) {
		if (resend_active) {
			network.writeSerial(numBytes, message);
			System.out.print("recibido y devuelto el siguiente mensaje: ");
		} else {
			System.out.print("recibió el siguiente mensaje: ");
		}
		// System.out.print(message[0]);
		// for (int i = 1; i < numBytes; ++i) {//no se usa message.length porque es de
		// tama�o 1024 y el mensaje no necesariamente ocupa eso.
		// System.out.print(", ");
		// System.out.print(message[i]);
		// }

		String strMsj = new String(message, 0, numBytes - 1);
		System.out.print(strMsj);

		System.out.println();

	}

}
