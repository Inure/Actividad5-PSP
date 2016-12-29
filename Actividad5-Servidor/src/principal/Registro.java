
package principal;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author C.RipPer
 * @date 29-dic-2016
 */
public class Registro {
    
    private String fichero = "./validacion/usuarios.txt";
    File f = new File (fichero);
    FileWriter escritor;;
    
    public Registro (String usuario){
        
        
        try {
            escritor = new FileWriter (f, true);
            escritor.write(usuario);
            escritor.flush();
            escritor.close();
            
        } catch (IOException ex) {
            System.err.println(ex);
            Logger.getLogger(Registro.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
