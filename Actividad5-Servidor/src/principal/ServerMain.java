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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * @author C.RipPer
 * @date 26-dic-2016
 */
public class ServerMain extends Thread{
    
    //*************************************
    //Datos de la conexión
    private static final int puerto = 3000;
    private static final String direccion = "127.0.0.1";
    //Variables IN/OUT (E/S)
    static InetSocketAddress addr;
    DataInputStream flujo_entrada;
    DataOutputStream flujo_salida;
    Socket skClient;
    //Variables del LOGGER
    private static final Logger LOGGER = Logger.getLogger("ServidorLog");;
    private static FileHandler fh;
    //Variables de trabajo
    static int nCli = 0;
    int opcion, contador = 0;
    boolean validado = false, siNO = false, existe = false, paso = false, salir = false;
    boolean reg = false;
    String usuario, pass, directorio, fichero;
    File [] listaArchivos;
    //*************************************

    /**
     * Pequeña función que se usa sólo para cerrar las conexiones E/S.
     */
    private void cierreConexion(){

        try {
            flujo_entrada.close();
            flujo_salida.close();
            skClient.close();
        } catch (IOException ex) {
            System.err.println(ex);
            //Logger.getLogger(ServerMain.class.getName()).LOGGER(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Pequeña función para eliminar lineas del programa principal
     * @param num 
     */
    private void enviarN(int num){
        try {
            flujo_salida.writeInt(num);
            flujo_salida.flush();
        } catch (IOException ex) {
            System.err.println(ex);
            Logger.getLogger(ServerMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Pequeña función para eliminar lineas del programa principal
     * @param texto 
     */
    private void enviarT(String texto){
        try {
            flujo_salida.writeUTF(texto);
            flujo_salida.flush();
        } catch (IOException ex) {
            System.err.println(ex);
            Logger.getLogger(ServerMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Pequeña función para eliminar lineas del programa principal
     * @param siNO 
     */
    private void enviarB(boolean siNO){
        try {
            flujo_salida.writeBoolean(siNO);
            flujo_salida.flush();
        } catch (IOException ex) {
            System.err.println(ex);
            Logger.getLogger(ServerMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Pequeña función que nos da el directorio al que está autorizado el usuario
     * @param num
     * @return 
     */
    private String directorioUsuario(int num){
        String dir;
        switch(num){
            case 1:
                dir = "./usuario";
                break;
            case 2:
                dir = "./admin";
                break;
            default:
                dir = "./usuario";
                break;
        }
        return dir;
    }
    
    /**
     * Constructor de la clase
     * @param skCliente 
     */
    private ServerMain(Socket skCliente){
        //Constructor
        this.skClient = skCliente;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            //Inicio del servidor en el puerto
            ServerSocket skServer = new ServerSocket ();
            addr = new InetSocketAddress(direccion, puerto);
            skServer.bind(addr);
            
            System.out.println("-> Escuchando el puerto "+puerto);
            
            //Activamos el LOGGER y asignamos archivo
            fh = new FileHandler("./log/estado.log", true); //Quiero el log en archivo
            fh.setLevel(Level.INFO); //Como estoy probando, activo el registrar todos los eventos
            LOGGER.setUseParentHandlers(false); //No queremos que muestre la información por pantalla
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);//Formateamos la entrada de información en el archivo
            LOGGER.addHandler(fh); //Comunicamos al logger la existencia del FileHandler (archivo log)
            
            while (true){ //Se ha puesto una conexión sin límite
                //Se aceptan conexiones
                nCli++;
                Socket skCliente = skServer.accept();
                System.out.println("-> Cliente conectado número "+nCli);
                
                LOGGER.log(Level.INFO, "Cliente conectado");
                
                //Se abre un hilo por cada cliente
                new ServerMain(skCliente).start();
            }
        } catch (IOException ex){
            System.err.println(ex);
            LOGGER.log(Level.WARNING, "Error detectado {0}", ex);
        }
    }

    @Override
    public void run(){
          //Tareas
    
        try {
            //Creamos los flujos de entrada y salida
            flujo_entrada = new DataInputStream (skClient.getInputStream());
            flujo_salida = new DataOutputStream (skClient.getOutputStream());
            
            //Recibimos la opción dada por el cliente
            opcion = flujo_entrada.readInt();
            
            switch (opcion){
                case 1:
                    System.out.println("-> Registro de usuario nuevo");
                    reg = true;
                    break;
                case 2:
                    System.out.println("-> El usuario desea logear");
                    paso = true;
                    break;
                case 3:
                    System.out.println("-> El usuario ha salido del programa");
                    salir = true;
                    break;
            }
            
            if (paso){
                do {
                    contador++;
                    usuario = flujo_entrada.readUTF();
                    pass = flujo_entrada.readUTF();

                    if (!usuario.equalsIgnoreCase("n")){
                        ValidarUsuario user = new ValidarUsuario (usuario, pass);
                        validado = user.comprobacion();

                        if (validado){
                            enviarN(0);
                            System.out.println("-> Login correcto.");
                            LOGGER.log(Level.INFO, "Usuario registrado {0}", usuario);

                            //Seleccionamos la carpeta según tipo de usuario
                            directorio = directorioUsuario(user.getTipo());

                        } else {
                            System.err.println("-> Login incorrecto.");
                            enviarN(1);
                            LOGGER.log(Level.WARNING, "->Intento de Log fallido"
                                    + " ({0}{1})", new Object[]{usuario, skClient.getRemoteSocketAddress()});
                        }

                    } else {
                        System.out.println("-> El usuario ha cerrado la sesión.");
                        contador = 4; //Para salir del bucle
                        salir = true; 
                    }

                    if (contador == 3){
                        System.err.println("-> Demasiados intentos.");
                        //La idea sería identificar la IP del que se intenta registrar, pero
                        //aún no he localizado una manera de solucionarlo.
                        LOGGER.log(Level.WARNING, "El login se ha anulado por "
                                + "demasiados intentos {0}", skClient.getRemoteSocketAddress());
                        enviarN(3);
                        salir = true;
                    }
                } while ((!validado) & (contador < 3));
            }
            
            //Comenzamos la validación de los ficheros para enviarlo
            if (validado){
                
                //Enviamos el listado de archivos de la carpeta correspondiente
                //Conseguimos el listado de archivos
                File busqueda = new File (directorio);
                listaArchivos = busqueda.listFiles();

                //Comunicamos el número de archivos
                enviarN(listaArchivos.length);
                //Enviamos el listado de archivos
                System.out.println(" ");
                System.out.println("-> Enviamos la lista de archivos");
                for (File archivo:listaArchivos){
                    enviarT(archivo.getName());
                }
                //Iniciamos el contador de nuevo para la validación de archivos
                contador = 0;
                do {                   
                    siNO = flujo_entrada.readBoolean();
                    //Si quiere el archivo
                    if (siNO){
                        fichero = flujo_entrada.readUTF();//Recibimos el nombre
                        System.out.println(" ");
                        System.out.println("Archivo solicitado "+fichero);

                        for(File elemento:listaArchivos){
                            if (elemento.getName().matches(fichero)){
                                existe = true;
                            }  
                        }
                        
                        if (existe){
                            enviarN(0); //Indicamos que sí existe y salimos del bucle
                            System.out.println("-> El archivo existe.");
                            salir = true;
                        } else {
                            contador++;
                            
                            System.err.println("-> El archivo no existe.");
                            
                            if (contador < 3){
                                enviarN(1); //Indicamos que no existe
                                System.out.println("-> Introduzca otro nombre de archivo.");
                            }
                            if (contador == 3){
                                System.out.println("-> Demasiados intentos.");
                                LOGGER.log(Level.WARNING, "¡¡Demasiados intentos!! (Envío anulado)");
                                enviarN(3); //Indicamos demasiados intentos
                                salir = true;
                            }
                        }
                        
                    } else {
                        contador = 4; //Para salir del bucle
                        System.out.println("-> El usuario no quiere archivo.");
                        salir = true;
                    }
                } while ((!existe)&(contador<3));
                
                if (existe){
                    File archivo = new File (directorio, fichero);
                    int tam = (int) archivo.length();

                    //Enviamos el tamaño del archivo
                    enviarN(tam);

                    byte [] buffer = new byte [tam];
                    FileInputStream entradaFichero = new FileInputStream(archivo.getPath());
                    BufferedInputStream bufferEntrada = new BufferedInputStream(entradaFichero);
                    bufferEntrada.read(buffer);

                    for (int i = 0; i < tam; i++) {
                        flujo_salida.write(buffer[i]);
                        flujo_salida.flush();
                    }
                    System.out.println("-> Enviado archivo solicitado");
                    LOGGER.log(Level.INFO, "Enviado archivo {0}", archivo.getName());
                }
            }
            
            if (reg){
                
                //Inicializo el paso y el contador
                paso = false;
                contador = 0;
                
                do {
                    contador++; 
                    
                    //Leemos los datos enviados por el cliente
                    usuario = flujo_entrada.readUTF();
                    pass = flujo_entrada.readUTF();

                    if (!usuario.equalsIgnoreCase("n")){

                        ValidarUsuario user = new ValidarUsuario (usuario, pass);
                        validado = user.comprobacionRegistro();

                        if (validado){ //Si hay un user con ese nombre se valida (true)
                            
                            if (contador <3){
                                enviarN(1);
                                System.out.println("-> Ya existe el usuario");
                            } 
                            
                        } else {
                            String registro = usuario + ";" + pass + ";" + "1" + "\n";
                            Registro nuevo = new Registro (registro);
                            LOGGER.log(Level.INFO, "Nuevo usuario registrado {0}", usuario);
                            enviarN(0);
                            //Salimos del bucle y cerramos conexiones
                            paso = true;
                            salir = true;
                        }
                    } else {
                        System.out.println("-> Anulado el registro");
                        paso = true;
                        salir = true;
                    }
                    
                } while ((contador <3)&(!paso));
                
                if (contador == 3){
                    enviarN(3);
                    System.err.println("-> Demasiados intentos.");
                    LOGGER.log(Level.WARNING, "¡¡Demasiados intentos!! (Registro anulado)");
                    salir = true;
                }
            }
            
            if (salir){
                System.out.println("-> Cerramos la conexión");
                cierreConexion();
            }
            
        } catch (IOException ex) {
            Logger.getLogger(ServerMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
