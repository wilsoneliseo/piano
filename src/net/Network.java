package net;

import gnu.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
/**
 * 
 * @author wegt
 */
public class Network {

    private InputStream inputStream;
    private OutputStream outputStream;
    /**
     * Estado de la conexión.
     */
    private boolean connected = false;
    /**
     * El hilo usado para recibir el dato de la Interfaz serial.
     */
    private Thread reader;
    private SerialPort serialPort;
    /**
     * Comunicando entre hilos, mostrando el {@link #reader} cuando la conexión
     * ha sido cerrada, así puede {@link Thread#join()}.
     */
    private boolean end = false;

    /**
     * Enlace a la instancia de la clase que implemente
     * {@link net.Network_iface}.
     */
    private Network_iface contact;
    /**
     * Un número pequeño <b>int</b> usado para distinguir entre dos paquetes
     * oonsecutivos. Puede tomar valore entre 0-255. Observe que el dato es
     * enviado para {@link net.Network_iface#parseInput(int, int, int[])} una
     * vez el siguiente 'divider' es identificado.
     *
     * Por defecto, <b>255</b> es usado como divider (a menos que se especifique
     * otro en el constructor).
     *
     * @see net.Network#Network(int, Network_iface, int)
     */
    private int divider;
    /**
     * <b>int</b> identifica un instancia de la clase Network. Cuando solo se
     * tiene una instancia, 'id', es irrelevante. Sin embargo, teniendo más de
     * una conexión abierta (usando más de una instancia de {@link Network}),
     * 'id' ayuda a identificar de que conexión serial vino un mensaje o log.
     */
    private int id;

    private int[] tempBytes;
    int numTempBytes = 0, numTotBytes = 0;

    /**
     * @param id
     * <b>int</b> identifica un instancia específica de la clase Network. Cuando
     * solo se tiene una instancia, {@link #id}, es irrelevante. Sin embargo,
     * teniendo más de una conexión abierta (usando más de una instancia de
     * {@link Network}), 'id' ayuda a identificar de que conexión serial vino un
     * mensaje o log.
     *
     * @param contact Enlace para la instancia de la clase que implemente
     * {@link net.Network_iface}.
     *
     * @param divider
     *
     * Un número pequeño <b>int</b> usado para distinguir entre dos paquetes
     * oonsecutivos. Puede tomar valore entre 0-255. Observe que el dato es
     * enviado para {@link net.Network_iface#parseInput(int, int, int[])} una
     * vez el siguiente 'divider' es identificado.
     */
    public Network(int id, Network_iface contact, int divider) {
        this.contact = contact;
        this.divider = divider;
        if (this.divider > 255) {
            this.divider = 255;
        }
        if (this.divider < 0) {
            this.divider = 0;
        }
        this.id = id;
        tempBytes = new int[1024];
    }

    public Network(int id, Network_iface contact) {
        this(id, contact, 255);
    }

    public Network(Network_iface contact) {
        this(0, contact);
    }

    /**
     * Este método es usado para obtener una lista de todos los puertos seriales
     * disponibles (solo puertos seriales). Uno de los elementos contenidos en
     * vector de retorno {@link Vector} puede ser usado como parametro en
     * {@link #connect(java.lang.String)} o {@link #connect(java.lang.String, int)
     * }
     * para abrir una conexién serial.
     *
     * @return Un {@link Vector} conteniendo {@link String}s mostrando todos los
     * puertos seriales disponibles.
     */
    @SuppressWarnings("unchecked")
    public Vector<String> getPortList() {
        Enumeration<CommPortIdentifier> portList;
        Vector<String> portVect = new Vector<String>();
        portList = CommPortIdentifier.getPortIdentifiers();

        // obteniendo solo puertos seriales
        CommPortIdentifier portId;
        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                portVect.add(portId.getName());
            }
        }
        contact.writeLog(id, "Se encontraron los siguientes puertos:");
        for (int i = 0; i < portVect.size(); i++) {
            contact.writeLog(id, ("   " + (String) portVect.elementAt(i)));
        }

        return portVect;
    }

    public boolean connect(String portName) {
        return connect(portName, 115200);
    }

    /**
     * Abrir una conexión al puerto serial especificado, usando la velocidad
     * especificada. Después de abrir el puerto, los mensajes se pueden enviar
     * usando {@link #writeSerial(String)} y los datos recibidos se empaquetaran
     * en paquetes (ver {@link #divider}) y se reenviaran usando
     * {@link net.Network_iface#parseInput(int, int, int [])}.
     *
     * @param portName El nombre del puerto en el que se debe abrir la conexión
     * (ver {@link #getPortList()}).
     * @param speed La velocidad deseada de la conexión en bps.
     * @return <b>true</b> si la conexión se ha abierto con éxito, <b>false</b>
     * otro caso.
     */
    public boolean connect(String portName, int speed) {
        CommPortIdentifier portIdentifier;
        boolean conn = false;
        try {
            portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
            if (portIdentifier.isCurrentlyOwned()) {
                contact.writeLog(id, "Error: El puerto esta actualmente en uso.");
            } else {
                serialPort = (SerialPort) portIdentifier.open("RTBug_network", 2000);
                serialPort.setSerialPortParams(speed, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);

                inputStream = serialPort.getInputStream();
                outputStream = serialPort.getOutputStream();

                reader = (new Thread(new SerialReader(inputStream)));
                end = false;
                reader.start();
                connected = true;
                contact.writeLog(id, "Conexion en " + portName + " ha sido establecida");
                conn = true;
            }
        } catch (NoSuchPortException e) {
            contact.writeLog(id, "NoSuchPortException: la conexion pudo no haber sido hecha");
            System.out.println(e.getMessage());
        } catch (PortInUseException e) {
            contact.writeLog(id, "PortInUseException: la conexion pudo no haber sido hecha");
            System.out.println(e.getMessage());
        } catch (UnsupportedCommOperationException e) {
            contact.writeLog(id, "UnsupportedCommOperationException: la conexion pudo no haber sido hecha");
            System.out.println(e.getMessage());
        } catch (IOException e) {
            contact.writeLog(id, "IOException: la conexion pudo no haber sido hecha");
            System.out.println(e.getMessage());
        }
        return conn;
    }

    /**
     * Una clase separada para usar como {@link net.Network # reader}. Se
     * ejecuta como un {@link Thread} separado y gestiona los datos entrantes,
     * empaquetándolos usando {@link net.Network#divider} un arreglo de
     * <b>int</b>'s
     * y reenviándolos usando
     * {@link net.Network_iface#parseInput (int, int, int [])}.
     * <p>
     * Una clase separada para usar como {@link net.Network#reader}. Se ejecuta
     * como un {@link Thread} separado y gestiona los datos entrantes,
     * empaquetándolos usando {@link net.Network # divider} en matrices de <b>
     * int </b> y reenviándolos usando
     * {@link net.Network_iface#parseInput(int, int, int [])}.
     */
    private class SerialReader implements Runnable {

        InputStream in;

        public SerialReader(InputStream in) {
            this.in = in;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int len = -1, i, temp;
            try {
                while (!end) {
                    if ((in.available()) > 0) {
                        if ((len = this.in.read(buffer)) > -1) {
                            for (i = 0; i < len; i++) {
                                temp = buffer[i];
                                // ajustar de C-Byte para Java-Byte
                                if (temp < 0) {
                                    temp += 256;
                                }
                                if (temp == divider) {
                                    if (numTempBytes > 0) {
                                        contact.parseInput(id, numTempBytes, tempBytes);
                                    }
                                    numTempBytes = 0;
                                } else {
                                    tempBytes[numTempBytes] = temp;
                                    ++numTempBytes;
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                end = true;
                try {
                    outputStream.close();
                    inputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                serialPort.close();
                connected = false;
                contact.networkDisconnected(id);
                contact.writeLog(id, "Network.SerialReader:La conexión ha sido interrrumpida.");
            }
        }
    }

    /**
     * Función simple para cerrar la conexión sostenida por instancia de esta
     * clase {@link net.Network}. Tambien finaliza el hilo
     * {@link net.Network#reader}.
     *
     * @return <b>true</b> si la conexión llegara a cerrarse, <b>false</b> en
     * otro caso.
     */
    public boolean disconnect() {
        boolean disconn = true;
        end = true;
        try {
            reader.join();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
            disconn = false;
        }
        try {
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            disconn = false;
        }
        serialPort.close();
        connected = false;
        contact.networkDisconnected(id);
        contact.writeLog(id, "connection disconnected");
        return disconn;
    }

    /**
     * @return Si esta instancia de {@link net.Network} tiene actualmente una
     * conexión abierta de no.
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     *
     * Este método está incluido como un legado. Dependiendo del otro lado del
     * puerto serie, puede ser más fácil enviar usando una cadena. Nota: este
     * método no agrega {@link #divider} al final.
     *
     * Si hay una conexión abierta, se puede enviar una {@link String} a través
     * del puerto serie usando esta función. Si no hay conexión disponible, se
     * devuelve
     * <b>false</ b> y se envía un mensaje usando
     * {@link net.Network_iface#writeLog(int, String)}.
     *
     * @param message La {@link String} que se enviará a través de la conexión
     * en serie.
     * @return <b>true</b> si el mensaje puede enviarse, <b>false</b> en caso
     * contrario.
     */
    public boolean writeSerial(String message) {
        boolean success = false;
        if (isConnected()) {
            try {
                outputStream.write(message.getBytes());
                success = true;
            } catch (IOException e) {
                disconnect();
            }
        } else {
            contact.writeLog(id, "Network:writeSerial: Puerto no conectado.");
        }
        return success;
    }

    /**
     * Si hay una conexión abierta, se puede enviar un <b> int </ b> entre 0 y
     * 255 (excepto el {@link net.Network # divider}) a través del puerto serie
     * usando esta función. El mensaje finalizará enviando el
     * {@link net.Network#divider}. Si no hay conexión disponible, se devuelve
     * <b>false</ b> y se envía un mensaje usando
     * {@link net.Network_iface#writeLog(int, String)}.
     *
     * @param numBytes El número de bytes para enviar a través del puerto serie.
     * @param message [] El arreglo de <b>int</b> s que se enviará a travós de
     * la conexión en serie (entre 0 y 256).
     *
     * @return <b>true</b> si el mensaje puede enviarse, <b>false</b> de lo
     * contrario o si uno de los números es igual a {@link Network#divider}.
     */
    public boolean writeSerial(int numBytes, int message[]) {
        boolean success = true;
        int i;
        for (i = 0; i < numBytes; ++i) {
            if (message[i] == divider) {
                success = false;
                break;
            }
        }
        if (success && isConnected()) {
            try {
                for (i = 0; i < numBytes; ++i) {
                    outputStream.write(changeToByte(message[i]));
                }
                outputStream.write(changeToByte(divider));
            } catch (IOException e) {
                success = false;
                disconnect();
            }
        } else if (!success) {
            // el mensaje contiene el divisor
            contact.writeLog(id, "Network.writeSerial:El mensaje contiene el divisor.");
        } else {
            contact.writeLog(id, "Network.writeSerial:Puerto desconectado.");
        }
        return success;
    }

    private byte changeToByte(int num) {
        byte number;
        int temp;
        temp = num;
        if (temp > 255) {
            temp = 255;
        }
        if (temp < 0) {
            temp = 0;
        }
        number = (byte) temp;
        return number;
    }
}
