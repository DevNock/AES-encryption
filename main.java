/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Sergey
 */
public class main {
    public static void main(String[] args)
    {
        String key = "some_key";
        String message = "aaadddaaaeGSFdfse dfdDfsdv";
        System.out.println(message);
        
        byte[] encrypted = AES.encryptECB(message.getBytes(), key.getBytes());
        System.out.println("AES: " + new String(encrypted));
        byte[] decrypted = AES.decryptECB(encrypted, key.getBytes());
        System.out.println("AES: " + new String(decrypted));
        
        encrypted = AES.encryptCBC(message.getBytes(), key.getBytes());
        System.out.println("AES: " + new String(encrypted));
        decrypted = AES.decryptCBC(encrypted, key.getBytes());
        System.out.println("CBC: " +new String(decrypted));
        
        encrypted = AES.encryptCFB(message.getBytes(), key.getBytes());
        System.out.println("AES: " + new String(encrypted));
        decrypted = AES.decryptCFB(encrypted, key.getBytes());
        System.out.println("CFB: " +new String(decrypted));
        
        encrypted = AES.encryptOFB(message.getBytes(), key.getBytes());
        System.out.println("AES: " + new String(encrypted));
        decrypted = AES.decryptOFB(encrypted, key.getBytes());
        System.out.println("OFB: " +new String(decrypted));
        
    }
}
