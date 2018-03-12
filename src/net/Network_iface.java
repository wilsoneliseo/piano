package net;

/**
 * El {@code _iface} en el nombre de la clase es en referencia a <i>interface</i>.
 * La instancia de la clase que implemente esta interfaz, va como parametro en el
 * constructor de {@link net.Network}.
 * @author wegt
 *
 */
public interface Network_iface {

	/**
	 * Para manejar los mensajes que van ocurriendo en {@link net.Network}.
	 * @param id es el identificador de la conexión.
	 * @param text el texto del mensaje.
	 */
	public void writeLog(int id, String text);

	/**
	 * Para manejar los mensajes entrantes. Estos mensajes son antes tratados por
	 * {@link net.Network.SerialReader#run()}, quien posteriormente hace las llamadas
	 * ha este método.
	 * @param id id es el identificador de la conexión.
	 * @param numBytes es el número de bytes, o bien elementos, contenidos en message.
	 * @param message es el mensaje recibido.
	 */
	public void parseInput(int id, int numBytes, int[] message);

	/**
	 * Para deshacer la conexión.
	 * @param id es el identificador de la conexión.
	 */
	public void networkDisconnected(int id);
}
