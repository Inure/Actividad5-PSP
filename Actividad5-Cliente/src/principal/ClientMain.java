/*
    Actividad 5 - PSP
    Crear una aplicación que realice los siguientes pasos:
    -> Solicitar el nombre del usuario que va a utilizar la aplicación. El login
tiene una longitud de 8 caracteres y está compuesto únicamente por letras 
minúsculas. Cuando se pase al servidor hay que encriptarlo.
    -> Solicitar al usuario el nombre de un fichero que quiere mostrar. El nombre
del fichero es como máximo de 8 caracteres y tiene una extensión de 3 caracteres.
    ->Visualizar en pantalla el contenido del fichero.
    Es importante tener en cuenta que se tiene que realizar una validación de los
datos de entrada y llevar un registro de la actividad del programa.
*/

package principal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author C.RipPer
 * @date 26-dic-2016
 */
public class ClientMain {
    
    //*********************************************
    //Datos de conexión
    private static final String HOST = "localhost";
    private static final int Puerto = 3000;
    //Variables IN/OUT (E/S)
    DataInputStream flujo_entrada;
    DataOutputStream flujo_salida;
    Scanner entrada = new Scanner(System.in);
    Socket sClient;
    //Variables de trabajo
    int op1 = 0;
    int op2;
    int contador1 = 0;
    boolean paso = false;
    Pattern patron;
    Matcher mat;
    String usuario, pass;
    //*********************************************
    
    /**
     * Pequeña función que se usa sólo para cerrar las conexiones E/S.
     */
    private void cierreConexion(){

        try {
            flujo_entrada.close();
            flujo_salida.close();
            sClient.close();
        } catch (IOException ex) {
            System.err.println(ex);
            //Logger.getLogger(ServerMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void menuPrincipal(){
        System.out.println("********************************************");
        System.out.println("*      M E N U       P R I N C I P A L     *");
        System.out.println("********************************************");
        System.out.println("**      1 -> Registro de usuario          **");
        System.out.println("**      2 -> Login usuario registrado     **");
        System.out.println("**      3 -> Salir                        **");
        System.out.println("********************************************");
    }
    
    private void menuUsuario(){
        System.out.println(" ");
        System.out.println("********************************************");
        System.out.println("*  U S U A R I O      R E G I S T R A D O  *");
        System.out.println("********************************************");
    }
    
    private void menuRegistro() {
        System.out.println(" ");
        System.out.println(" ");
        System.out.println(" ");
        System.out.println("********************************************");
        System.out.println("*     R E G I S T R O    U S U A R I O     *");
        System.out.println("********************************************");
    }
    
    private void enviarT(String texto){
        try {
            flujo_salida.writeUTF(texto);
            flujo_salida.flush();
        } catch (IOException ex) {
            System.err.println(ex);
            Logger.getLogger(ClientMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void enviarN (int num){
        try {
            flujo_salida.writeInt(num);
            flujo_salida.flush();
        } catch (IOException ex) {
            System.err.println(ex);
            Logger.getLogger(ClientMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public ClientMain(){
        try {
            sClient = new Socket (HOST, Puerto);
            
            //Creando los flujos
            flujo_entrada = new DataInputStream(sClient.getInputStream());
            flujo_salida = new DataOutputStream(sClient.getOutputStream());
            
            do {
                contador1++;
                //Pantalla principal
                menuPrincipal();
                System.out.print("Introduzca la opción deseada: ");
                op1 = entrada.nextInt();
        
                switch (op1){
                    case 1:
                        System.out.println("Ha elegido registrar su usuario.");
                        paso = true;
                        break;
                    case 2:
                        System.out.println(" ");
                        System.out.println("Ha elegido introducir su usuario.");
                        paso = true;
                        break;
                    case 3:
                        System.out.println("**Espero que haya disfrutado de la "
                                + "experiencia, por favor, vuelva pronto.**");
                        paso = true;
                        break;
                    default:
                        System.out.println(" ");
                        System.out.println("Opción incorrecta.");
                        if (contador1 == 3){
                            System.out.println("-> Demasiados intentos");
                            paso = true;
                            op1 = 3;
                        } else {
                            System.out.println("Por favor, vuelva a intentarlo.");
                            System.out.println(" ");
                        }
                        break;
                }
            } while (!paso);
            
            if (op1 == 3) {
                //Le comunicamos la opción deseada al servidor
                System.out.println("-> Cerramos la conexión.");
                enviarN(op1);
                cierreConexion();
            }
            
            if (op1 == 2){
                
                patron = Pattern.compile("[a-z]{8}");
                contador1 = 0;
                paso = false;
                
                //Le comunicamos la opción deseada al servidor
                enviarN(op1);
                
                do {
                    contador1++;

                    menuUsuario();

                    //Solicitamos el usuario
                    System.out.print("Introduzca su usuario o escribe n/N para salir: ");
                    usuario = entrada.next();

                    if (!usuario.equalsIgnoreCase("n")){
                        //Solicitamos la contraseña
                        System.out.print("Introduzca su contraseña: ");
                        pass = entrada.next();


                        //Comprobamos el patron
                        mat = patron.matcher(usuario);

                        if (mat.find()){
                            //Inicializamos contadores

                            contador1 = 0;
                            //Enviamos los datos
                            enviarT(usuario);
                            enviarT(pass);

                            //Recibimos la validación
                            op2 = flujo_entrada.readInt();

                            switch (op2){
                                case 0:
                                    System.out.println("-> Usuario aceptado");
                                    paso = true;
                                    break;
                                case 1:
                                    System.out.println("-> Usuario/Contraseña incorrectos");
                                    break;
                                default:
                                    System.out.println("-> Demasiados intentos.");
                                    System.out.println(" ");
                                    System.out.println("Saliendo....");
                                    paso = true;
                                    break;
                            }

                        } else {
                            System.out.println(" ");
                            System.out.println("-> No concuerda con el patrón (8 letras minúsculas).");

                            if (contador1 < 3){
                                System.out.println("-> Por favor, vuelva a introducir sus datos.");
                            }
                            if (contador1 == 3){
                                System.out.println("-> Demasiados intentos.");
                            }
                        }
                    } else {
                        System.out.println("Saliendo..... ");
                        contador1 = 4;
                        enviarT(usuario);
                        enviarT("salir");
                    }
                    
                } while ((contador1 < 3) & (!paso));
                
                System.out.println("-> Cerramos conexión.");
                cierreConexion();
            }
            
            if (op1 == 1){
                //Le comunicamos la opción deseada al servidor
                enviarN(op1);
                
                menuRegistro();
                System.out.println("Introduzca el usuario que desea (8 letras minúsculas): ");
                System.out.println("Introduzca su contraseña: ");
                cierreConexion();
            }
            
        } catch (IOException ex) {
            System.err.println(ex);
            Logger.getLogger(ClientMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new ClientMain();
    }
    
}
