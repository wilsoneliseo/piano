package principal;

import java.awt.Component;

import org.jfugue.parser.ParserListenerAdapter;
import org.jfugue.theory.Note;

import net.Network;
import net.Network_iface;

/**
 *
 * @author wegt
 */
public class NotasEnOctava extends ParserListenerAdapter implements Network_iface {

    private Component frame = null;
    public final String namePort;
    public final int speed;
    private static final int DIVIDER = 255;
    private static Network network;
    public static final int ID_CONNECTION = 1;
    private static final int SIZE_PACK = 2;
    private int _package[];
    public MiNota lista_notas[];
    private static final int SIZE_LISTA_NOTAS = 1000;
    public int index_lista_notas;

    private static NotasEnOctava instance = null;

    public static NotasEnOctava getInstance() {
        synchronized (NotasEnOctava.class) {
            if (instance == null) {
                instance = new NotasEnOctava("COM3", 9600);
            }
        }
        return instance;
    }

    //Constructor privado, para garantizar que la unica via de acceso sea el 
    //metodo getInstance().
    private NotasEnOctava(String namePort, int speed) {
        this.namePort = namePort;
        this.speed = speed;
        _package = new int[SIZE_PACK];
        lista_notas = new MiNota[SIZE_LISTA_NOTAS];
        index_lista_notas = 0;
    }

    public void connect() {
        if (frame != null) {
            network = new net.Network(ID_CONNECTION, new NotasEnOctava(namePort, speed), DIVIDER);

            // conectando el puerto 
            if (network.connect(namePort, speed)) {
                javax.swing.JOptionPane.showMessageDialog(frame, "Conexion serial fue exitosa.",
                        "Exito", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            } else {
                javax.swing.JOptionPane.showMessageDialog(frame,
                        "Lo siento, fue encontrado un error de conexión\\n",
                        "Desconexión", javax.swing.JOptionPane.WARNING_MESSAGE);
                System.exit(1);
            }
        } else {
            this.writeLog(ID_CONNECTION, "NotasEnOctava.connect(): No se pudo realizar "
                    + "la conexion serial, dado que no se ha establecido el objeto "
                    + "Component.");
            this.networkDisconnected(ID_CONNECTION);
        }
    }

    @Override
    public void onNoteParsed(Note note) {

//		String[] common = Note.NOTE_NAMES_COMMON;
        if (note.isRest()) {
            return;
        }
//		System.out.println(common[note.getPositionInOctave()]+" "+note.getValue()+" Frecuencia: "+Note.getFrequencyForNote(note.getValue())+" Duracion: "+note.getDuration());        
        if (index_lista_notas < SIZE_LISTA_NOTAS) {
            lista_notas[index_lista_notas] = new MiNota(
                    Note.getFrequencyForNote(note.getValue()),
                    note.getDuration());
            index_lista_notas++;
        } else
            ;
    }

    @Override
    public void writeLog(int id, String text) {
        System.out.println("   log:  |" + text + "|");

    }

    @Override
    public void parseInput(int id, int numBytes, int[] message) {
        String strMsj = new String(message, 0, numBytes);
        System.out.println(strMsj);
    }

    public void parseOutput() {
//		System.out.println("(frecuency: "+frequency+","+"duration: "+duration);

        /*		int hz_integer=(int) frequency;
		double hz_decimals=(frequency-(double) hz_integer)*100;
		_package[0]=hz_integer;
		_package[1]=(int) hz_decimals;*/
        for (int i = 0; i < SIZE_LISTA_NOTAS; i++) {

            int hz_integer = (int) lista_notas[i].getFrecuencia();
            _package[0] = hz_integer;

            int dur_integer = (int) (lista_notas[i].getDuracion() * 1000);
            _package[1] = dur_integer;

            System.out.println("(frecuency: " + hz_integer + "," + "duration: " + dur_integer);

            try {
                network.writeSerial(SIZE_PACK, _package);
            } catch (NullPointerException e) {
                writeLog(ID_CONNECTION, "No se ha iniciado instancia de Network.");
                networkDisconnected(ID_CONNECTION);
            }

        }
    }

    @Override
    public void networkDisconnected(int id) {
        System.exit(0);
    }

    public void setFrame(Component frame) {
        this.frame = frame;
    }

    /**
     * Para enviar un mensaje a arduino. No agrega el divisor {@value #DIVIDER}, al final
     * del mensaje, por lo que no es recomendable cuando se quiere enviar mensajes de
     * diferentes tipos, como es el caso de este proyecto. Se incluyo solo para 
     * testear.
     * @param msj 
     */
    public void enviarDatoArduino(String msj) {
        network.writeSerial(msj);
    }

    public void enviarDatoArduino(int inp_num) {
        if ((inp_num > 255) || (inp_num < 0)) {
            writeLog(ID_CONNECTION, "El número "+inp_num+" debe estar entre 0-254"
                    + "para enviarlo a Arduino.");
            System.out.println("el número que ingresó no es válido");
        } else {
            int temp[] = {inp_num};
            network.writeSerial(1, temp);            
        }
    }

}
