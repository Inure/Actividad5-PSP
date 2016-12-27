package principal;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author C.RipPer
 * @date 27-dic-2016
 */
public class Encriptado {
    private String textoLlano;
    private String encriptado;
    
    
    public Encriptado(String texto){
        this.textoLlano = texto;
    }
    
    public String codificar(){
        encriptado = DigestUtils.md5Hex(textoLlano);
        return encriptado;
    }

}
