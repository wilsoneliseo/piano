package ui;

import java.io.File;
import java.util.ArrayList;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

/**
 *
 * @author wegt
 */
public class ComboModel extends AbstractListModel implements ComboBoxModel {

    private static final String STR_PATH_DIRECTORIO = "C:\\Users\\wegt\\Documents\\NetBeansProjects\\piano\\midi";
    private static final String STR_MSJ_BIENVENIDA="Elegir midi...";
    ArrayList archivos = new ArrayList();
    String selection = STR_MSJ_BIENVENIDA;

    public ComboModel() {
        File f = new File(STR_PATH_DIRECTORIO);
        if (f.exists()) {
            File[] ficheros = f.listFiles();
            for (int x=0;x<ficheros.length;x++){
                archivos.add(ficheros[x].getName());
            }
        } else {
        }
    }

    @Override
    public int getSize() {
        return archivos.size();
    }

    @Override
    public Object getElementAt(int index) {
        return archivos.get(index);
    }

    @Override
    public void setSelectedItem(Object anItem) {
        selection = (String) anItem; // to select and register an
    }

    @Override
    public Object getSelectedItem() {
        return selection; // to add the selection to the combo box            
    }

}
