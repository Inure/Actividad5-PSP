/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package principal;

/**
 * @author C.RipPer
 * @date 26-dic-2016
 */
public class ValidarUsuario {
    private String usuario;
    private String contrasena;
    
    String [] usuarios = {"pruebass", "ceripper", "tercerod"};
    String [] contrasenas = {"prueba", "123456", "3dam"};
    
    public ValidarUsuario(String usuario, String contra){
        this.usuario = usuario;
        this.contrasena = contra;
    }
    
    public boolean comprobacion (){
        boolean validado = false;
        
        for (int i = 0; i < usuarios.length; i++) {
            
            if ((usuarios[i].matches(usuario)) && (contrasenas[i].matches(contrasena))) {
                validado = true;
            }
            
        }
        
        return validado;
    }

}
