
package principal;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author C.RipPer
 * @date 27-dic-2016
 */
public class ValidarUsuario {
    
    private String nombre;
    private String pass;
    private String fichero = ("./validacion/usuarios.txt");
    //Tipo: 1 - user
    //      2 - admin
    private String tipoS;
    private int tipo;
    
    public ValidarUsuario (String nombre, String pass){
        this.nombre = nombre;
        this.pass = pass;
    }
    
    //Función enseñada por Ayaya Perera en la tarea 04 PSP
    public boolean comprobacion (){
        boolean validado = false;
        //Distingue entre mayúsculas y minúsculas, aunque con el patrón de entrada
        //no importa
        Predicate<String> user = linea -> linea.contains(nombre + ";" + pass + ";");
        String usuario = nombre+";"+pass+";";
        
        try (Stream<String> stream = Files.lines(Paths.get(fichero))){
            
            if (stream.anyMatch(user)){
                
                validado = true;
                //De mi cosecha por eso es tan lioso
                //saber si el que se registra es usuario o admin
                try (Stream<String> st = Files.lines(Paths.get(fichero))){
                        tipoS = st
                        .filter(line -> line.contains(usuario))
                        .map(line -> line.split(";"))
                        .map(columns -> columns[2])
                        .findFirst().toString();
                }
                
                //Hago esta distinción porque el tipo me lo pone dentro del Optional[]
                if (tipoS.equals("Optional[2]")){
                    setTipo(2);
                } else {
                    setTipo(1);
                }
            }
            
        } catch (Exception ex){
            System.err.println(ex);
        }
        return validado;
    }

    /**
     * @return the tipo
     */
    public int getTipo() {
        return tipo;
    }

    /**
     * @param tipo the tipo to set
     */
    private void setTipo(int tipo) {
        this.tipo = tipo;
    }
    
    

}
