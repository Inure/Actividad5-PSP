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

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
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
    //Variables del LOGGER
    static Logger LOGGER = Logger.getLogger("ServidorLog");;
    static FileHandler fh;
    //Variables de trabajo
    Menu menu = new Menu();
    int opcion = 0;
    int contador = 0;
    boolean paso = false, list = false, existe = false, salir = false;
    Pattern patron;
    Matcher mat;
    String usuario, pass, archivo;
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
   
    /**
     * Pequeña función para eliminar líneas del programa principal
     * @param texto 
     */
    private void enviarT(String texto){
        try {
            flujo_salida.writeUTF(texto);
            flujo_salida.flush();
        } catch (IOException ex) {
            System.err.println(ex);
            Logger.getLogger(ClientMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Pequeña función para eliminar lineas del programa principal
     * @param num 
     */
    private void enviarN (int num){
        try {
            flujo_salida.writeInt(num);
            flujo_salida.flush();
        } catch (IOException ex) {
            System.err.println(ex);
            Logger.getLogger(ClientMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Pequeña función para eliminar lineas del programa principal
     * @param siNO 
     */
    private void enviarB (boolean siNO){
        try {
            flujo_salida.writeBoolean(siNO);
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
                contador++;
                //Pantalla principal
                menu.principal();
                System.out.print("Introduzca la opción deseada: ");
                opcion = entrada.nextInt();
        
                switch (opcion){
                    case 1:
                        menu.espacioBlanco();
                        System.out.println("Ha elegido registrar su usuario.");
                        paso = true;
                        break;
                    case 2:
                        menu.espacioBlanco();
                        System.out.println("Ha elegido introducir su usuario.");
                        paso = true;
                        break;
                    case 3:
                        menu.espacioBlanco();
                        System.out.println("**Espero que haya disfrutado de la "
                                + "experiencia, por favor, vuelva pronto.**");
                        menu.espacioBlanco();
                        paso = true;
                        salir = true;
                        break;
                    default:
                        menu.espacioBlanco();
                        System.err.println("Opción incorrecta.");
                        
                        if (contador == 3){
                            System.err.println("-> Demasiados intentos");
                            paso = true;
                            salir = true;
                        } else {
                            System.out.println("-> Por favor, vuelva a intentarlo.");
                            menu.espacioBlanco();
                        }
                        break;
                }
            } while (!paso);
            
            if (salir) {
                //Le comunicamos la opción deseada al servidor
                System.out.println("-> Cerramos la conexión.");
                enviarN(3);
                cierreConexion();
            }
            
            if (opcion == 2){
                //Le comunicamos la opción deseada al servidor
                enviarN(opcion);
                
                //Asignamos el patrón del usuario
                patron = Pattern.compile("[a-z]{8}");
                
                //Iniciamos el conteo y variables
                contador = 0;
                opcion = 0;
                paso = false;
                salir = false;
                
                do {
                    contador++;

                    menu.usuario();

                    //Solicitamos el usuario
                    System.out.print("Introduzca su usuario o escribe n/N para salir: ");
                    usuario = entrada.next();

                    if (!usuario.equalsIgnoreCase("n")){
                        
                        //Comprobamos el patron
                        mat = patron.matcher(usuario);

                        if (mat.find()){ //Si coincide el patrón
                            
                            //Solicitamos la contraseña
                            System.out.print("Introduzca su contraseña: ");
                            pass = entrada.next();
                            
                            //Inicializamos contadores
                            contador = 0;

                            //Enviamos los datos
                            enviarT(usuario);
                            Encriptado passE = new Encriptado(pass);
                            String encrip = passE.codificar();
                            enviarT(encrip);

                            //Recibimos la validación
                            opcion = flujo_entrada.readInt();

                            switch (opcion){
                                case 0:
                                    System.out.println("-> Usuario aceptado");
                                    list = true;
                                    paso = true;
                                    break;
                                case 1:
                                    System.err.println("-> Usuario/Contraseña incorrectos");
                                    LOGGER.log(Level.WARNING,"Error en la validación");
                                    break;
                                default:
                                    System.err.println("-> Demasiados intentos.");
                                    System.out.println(" ");
                                    System.out.println("Saliendo....");
                                    LOGGER.log(Level.WARNING, "¡¡Demasiados intentos!!");
                                    paso = true;
                                    break;
                            }

                        } else { //Si no coincide la contraseña
                            System.out.println(" ");
                            System.out.println("-> No concuerda con el patrón (8 letras minúsculas).");
                            LOGGER.log(Level.WARNING, "No concuerda patrón");

                            if (contador < 3){
                                System.out.println("-> Por favor, vuelva a introducir sus datos.");
                            }
                            if (contador == 3){
                                System.out.println("-> Demasiados intentos.");
                                LOGGER.log(Level.WARNING, "¡¡Demasiados intentos!!");
                                enviarT("n");
                                enviarT("n");
                            }
                        }
                    } else {
                        System.out.println("Saliendo..... ");
                        contador = 4;
                        enviarT(usuario);
                        enviarT("salir");
                    }
                    
                } while ((contador < 3) & (!paso));
                
                //Recibimos el listado de archivos
                if (list){
                    //Primero recibimos el número de archivos a leer
                    int numArc = flujo_entrada.readInt();
                    //Recibimos los nombres de los archivos de la carpeta
                    menu.listado();
                    
                    for (int i = 0; i < numArc; i++) {
                        System.out.println("    |   " + flujo_entrada.readUTF());
                    }
                    
                    menu.espacioBlanco();
                    System.out.println("-> Listado terminado");
                    
                    //Iniciamos el contador para los patrones del nombre
                    contador = 0;
                    Thread.sleep(250);
                    
                    do {
                        System.out.print("-> Introduce el nombre del archivo que "
                                + "quieres leer o escribe (n/N) para salir: ");
                        archivo = entrada.next();

                        if (archivo.toUpperCase().matches("N")){
                            System.out.println("Saliendo del programa ....");
                            enviarB(false);

                            //Salimos del bucle
                            existe = false;
                            contador = 4;

                        } else {
                            
                            patron = Pattern.compile("[a-z]{8}.[a-z]{3}");
                            mat = patron.matcher(archivo);
                            
                            if (mat.find()){
                                //Indicamos que sí enviamos archivo
                                enviarB(true);

                                //Enviamos el nombre del archivo que deseamos leer
                                enviarT(archivo);

                                //El servidor nos comunica si existe o no el archivo
                                existe = flujo_entrada.readBoolean();

                                if (existe){
                                    contador = 4;
                                } else {
                                    System.out.println(" ");
                                    System.out.println("** El archivo no existe.");
                                }
                            } else {
                                contador++;
                                System.out.println(" ");
                                System.out.println("-> No concuerda con el patrón.");
                                LOGGER.log(Level.WARNING, "No concuerda patrón del nombre de archivo");

                                if (contador < 3){
                                    System.out.println("-> Por favor, vuelva a introducir sus datos.");
                                }
                                if (contador == 3){
                                    System.out.println("-> Demasiados intentos.");
                                    enviarB(false);
                                    LOGGER.log(Level.WARNING, "¡¡Demasiados intentos!!");
                                }
                            }
                        }

                    } while (contador<3);
                    
                    if (existe){
                        //El servidor nos ha dicho que existe y nos preparamos para
                        //recibirlo
                        String nombreArchivo = "Recibido_"+archivo;

                        //Recibimos el tamaño del buffer para preparar la entrada
                        int tam = flujo_entrada.readInt();

                        //Creamos el flujo de salida
                        FileOutputStream fileOut = new FileOutputStream(nombreArchivo);
                        BufferedOutputStream salidaFichero = new BufferedOutputStream (fileOut);
                        BufferedInputStream entradaBuff = new BufferedInputStream (sClient.getInputStream());

                        //Recibimos el tamaño del archivo y creamos el array 
                        byte [] buffer = new byte[tam];
                        for (int i = 0; i < buffer.length; i++) {
                            buffer[i] = (byte)entradaBuff.read();
                        }

                        //Escribimos el archivo
                        salidaFichero.write(buffer);
                        salidaFichero.flush();
                        System.out.println("-> Recepción finalizada");
                        File fichero = new File (nombreArchivo);
                        Desktop.getDesktop().open(fichero);
                        System.out.println("-> Abrimos el archivo");

                        //Cerramos los flujos de entrada/salida del buffer
                        entradaBuff.close();
                        salidaFichero.close();
                    }
                }
                System.out.println("-> Cerramos conexión.");
                cierreConexion();
            }
            
            if (opcion == 1){
                //Le comunicamos la opción deseada al servidor
                enviarN(opcion);
                
                //Iniciamos contador
                contador = 0;
                paso = false;
                
                //Asignamos el patrón del usuario
                patron = Pattern.compile("[a-z]{8}");
                
                do {      
                    contador++;
                    
                    menu.registro();
                    
                    System.out.print("Introduzca el usuario que desea (8 letras "
                            + "minúsculas) o n/N para salir: ");
                    usuario = entrada.next();
                    
                    if (!usuario.equalsIgnoreCase("n")){
                        
                        //Comprobamos el patron
                        mat = patron.matcher(usuario);

                        if (mat.find()){ //Si coincide el patrón
                            
                            //Solicitamos la contraseña
                            System.out.print("Introduzca su contraseña: ");
                            pass = entrada.next();
                            
                            //Enviamos los datos
                            enviarT(usuario);
                            Encriptado passE = new Encriptado(pass);
                            String encrip = passE.codificar();
                            enviarT(encrip);
                            paso = flujo_entrada.readBoolean();
                            
                            if (!paso){
                                System.out.println("-> Usuario ya registrado.");
                                System.out.println("-> Introduzca otro usuario");
                            }

                        } else {
                                
                            System.out.println(" ");
                            System.out.println("-> No concuerda con el patrón.");
                            LOGGER.log(Level.WARNING, "No concuerda patrón del nombre de usuario (Registro)");

                            if (contador < 3){
                                System.out.println("-> Por favor, vuelva a introducir sus datos.");
                            }
                            if (contador == 3){
                                System.out.println("-> Demasiados intentos.");
                                enviarT("n");
                                enviarT("salir");
                                LOGGER.log(Level.WARNING, "¡¡Demasiados intentos!! (Registro)");
                                paso = true;
                            }
                        }
                    } else {
                        System.out.println("Saliendo....");
                        enviarT("n");
                        enviarT("salir");
                        paso = true;
                    }
                    
                } while (!paso);
                
                System.out.println("-> Cerramos conexión.");
                cierreConexion();
            }   
            
        } catch (IOException | InterruptedException ex) {
            System.err.println(ex);
            Logger.getLogger(ClientMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        try {
            //Activamos el LOGGER y asignamos archivo
            fh = new FileHandler("./log/estado.log", true); //Quiero el log en archivo
            fh.setLevel(Level.ALL); //Como estoy probando, activo el registrar todos los eventos
            LOGGER.setUseParentHandlers(false); //No queremos que muestre la información por pantalla
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);//Formateamos la entrada de información en el archivo
            LOGGER.addHandler(fh); //Comunicamos al logger la existencia del FileHandler (archivo log)
            
            //Llamamos al cliente
            new ClientMain();
            
        } catch (IOException ex) {
            Logger.getLogger(ClientMain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(ClientMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
