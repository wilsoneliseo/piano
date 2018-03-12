package principal;

import javax.swing.JFrame;
import ui.Juego;

public class Principal {

    public static void main(String[] args) {
//		(new ConexionConsola()).correr(args);;
        Juego j = new Juego();
        j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        j.setVisible(true);

    }

}
