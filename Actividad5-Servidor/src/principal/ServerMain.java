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
    private static final int port = 3000;
    private static final String address = "127.0.0.1";
    //Variables IN/OUT (E/S)
    static InetSocketAddress addr;
    DataInputStream flujo_entrada;
    DataOutputStream flujo_salida;
    Socket skClient;
    //Variables del LOGGER
    static Logger LOGGER = Logger.getLogger("ServidorLog");;
    static FileHandler fh;
    //Variables de trabajo
    static int nCli = 0;
    int op1, contador = 0;
    boolean validado = false, siNO = false, existe = false;
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
    
    private void enviarN(int num){
        try {
            flujo_salida.writeInt(num);
            flujo_salida.flush();
        } catch (IOException ex) {
            System.err.println(ex);
            Logger.getLogger(ServerMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void enviarT(String texto){
        try {
            flujo_salida.writeUTF(texto);
            flujo_salida.flush();
        } catch (IOException ex) {
            System.err.println(ex);
            Logger.getLogger(ServerMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void enviarB(boolean siNO){
        try {
            flujo_salida.writeBoolean(siNO);
            flujo_salida.flush();
        } catch (IOException ex) {
            System.err.println(ex);
            Logger.getLogger(ServerMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
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
            System.out.println("-> Escuchando el puerto "+port);
            addr = new InetSocketAddress(address, port);
            skServer.bind(addr);
            
            //Activamos el LOGGER y asignamos archivo
            fh = new FileHandler("./log/estado.log", true); //Quiero el log en archivo
            fh.setLevel(Level.ALL); //Como estoy probando, activo el registrar todos los eventos
            LOGGER.setUseParentHandlers(false); //No queremos que muestre la información por pantalla
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);//Formateamos la entrada de información en el archivo
            LOGGER.addHandler(fh); //Comunicamos al logger la existencia del FileHandler (archivo log)
            
            while (true){ //Se ha puesto una conexión sin límite
                //Se aceptan conexiones
                Socket skCliente = skServer.accept();
                nCli++;
                System.out.println("-> Cliente conectado número "+nCli);
                LOGGER.log(Level.INFO, "Cliente conectado");
                
                //Se abre un hilo por cada cliente
                new ServerMain(skCliente).start();
            }
        } catch (IOException ex){
            System.err.println(ex);
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
            op1 = flujo_entrada.readInt();
            switch (op1){
                case 1:
                    System.out.println("-> Registro de usuario nuevo");
                    break;
                case 2:
                    System.out.println("-> Login usuario registrado");
                    
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
                                LOGGER.log(Level.INFO, "Usuario registrado "+ usuario);
                                //Primero seleccionamos la carpeta según tipo de usuario
                                if (user.getTipo()==2){
                                    directorio = "./admin";
                                } else {
                                    directorio = "./usuario";
                                }
                            } else {
                                enviarN(1);
                                LOGGER.log(Level.WARNING, "->Intento de Log fallido ("+ 
                                        usuario + skClient.getRemoteSocketAddress()+")");
                            }
 
                        } else {
                            System.out.println("-> El usuario ha cerrado la sesión.");
                            contador = 4;
                        }

                        if (contador == 3){
                            System.out.println("-> Demasiados intentos.");
                        }
                        
                    } while ((!validado) & (contador < 3));
                    
                    if (contador == 3){
                        enviarN(3);
                    }
                    
                    //Una vez validado comenzamos el tratamiento de ficheros
                    //Primero enviamos sólo el listado
                    if (validado) {
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
                    }
                    break;
                case 3:
                    System.out.println("-> El usuario ha salido del programa");
                    cierreConexion();
                    break;
            }
            
            //Comenzamos la validación de los ficheros para enviarlo
            if (validado){
                //Iniciamos el contador de nuevo para la validación de archivos
                contador = 0;
                do {                   
                    contador++;
                    
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
                            enviarB(true); //Indicamos que sí existe y salimos del bucle
                            System.out.println("-> El archivo existe.");
                        } else {
                            contador++;
                            enviarB(false); //Indicamos que no existe
                            System.out.println("-> El archivo no existe.");
                            
                            if (contador<3){
                                System.out.println("-> Introduzca otro nombre de archivo.");
                            }
                            if (contador == 3){
                                System.out.println("-> Demasiados intentos.");
                            }
                        }
                        
                    } else {
                        contador = 4; //Para salir del bucle
                        System.out.println("-> El usuario no quiere archivo.");
                    }
                } while ((!existe)&(contador<3));
                
                if (existe){
                    File archivo = new File (fichero);
                    int tam = (int) archivo.length();

                    //Enviamos el tamaño del archivo
                    enviarN(tam);

                    byte [] buffer = new byte [tam];
                    FileInputStream entradaFichero = new FileInputStream(archivo.getName());
                    BufferedInputStream bufferEntrada = new BufferedInputStream(entradaFichero);
                    bufferEntrada.read(buffer);

                    for (int i = 0; i < tam; i++) {
                        flujo_salida.write(buffer[i]);
                        flujo_salida.flush();
                    }
                    System.out.println("-> Enviado archivo solicitado");
                }
                System.out.println("-> Cerramos la conexión");
                cierreConexion();
            }
            
        } catch (IOException ex) {
            Logger.getLogger(ServerMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
