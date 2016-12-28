package principal;

/**
 * @author C.RipPer
 * @date 28-dic-2016
 */
public class Menu {
    
    //Menus principales    
    public void principal(){
        System.out.println("********************************************");
        System.out.println("*      M E N U       P R I N C I P A L     *");
        System.out.println("********************************************");
        System.out.println("**      1 -> Registro de usuario          **");
        System.out.println("**      2 -> Login usuario registrado     **");
        System.out.println("**      3 -> Salir                        **");
        System.out.println("********************************************");
    }
    
    public void usuario(){
        espacioBlanco();
        System.out.println("********************************************");
        System.out.println("*  U S U A R I O      R E G I S T R A D O  *");
        System.out.println("********************************************");
    }
    
    public void registro() {
        espacioBlanco();
        System.out.println("********************************************");
        System.out.println("*     R E G I S T R O    U S U A R I O     *");
        System.out.println("********************************************");
    }
    
    //Menus secundarios
    public void listado(){
        espacioBlanco();
        System.out.println("    LISTADO DE ARCHIVOS - CARPETA SERVIDOR");
    }
    
    public void espacioBlanco(){
        System.out.println(" ");
        System.out.println(" ");
        System.out.println(" ");
    }
    

}
