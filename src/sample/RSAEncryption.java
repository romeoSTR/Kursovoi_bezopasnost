package sample;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSAEncryption {
    private KeyPairGenerator keyPairGenerator;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private byte[] someData;

    public RSAEncryption(int lengthOfKey) throws Exception{
        this.keyPairGenerator=KeyPairGenerator.getInstance("RSA");
        this.keyPairGenerator.initialize(lengthOfKey);
    }

    public RSAEncryption(byte[] data){
        this.someData=data;
    }

    public void createNewKeys(){
        KeyPair pair=this.keyPairGenerator.generateKeyPair();
        this.privateKey= pair.getPrivate();
        this.publicKey= pair.getPublic();
    }

    public PrivateKey getPrivateKey(){
        return this.privateKey;
    }

    public PublicKey getPublicKey(){
        return this.publicKey;
    }

    public void setPrivateKey(byte[] key){
        try{
            KeyFactory keyFactory=KeyFactory.getInstance("RSA");
            privateKey=keyFactory.generatePrivate(new PKCS8EncodedKeySpec(key));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setPublicKey(byte[] key){
        try{
            KeyFactory keyFactory=KeyFactory.getInstance("RSA");
            publicKey=keyFactory.generatePublic(new X509EncodedKeySpec(key));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public byte[] cryptData(){
        if(publicKey!=null){
            try {
                Cipher cipher =Cipher.getInstance("RSA/ECB/PKCS1PADDING");
                cipher.init(Cipher.ENCRYPT_MODE,publicKey);
                return cipher.doFinal(someData);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }

    public byte[] decryptData(){
        if(this.privateKey!=null) {
            try {
                Cipher cipher =Cipher.getInstance("RSA/ECB/PKCS1PADDING");
                cipher.init(Cipher.DECRYPT_MODE,privateKey);
                return cipher.doFinal(someData);
            }catch (Exception e ){
                e.printStackTrace();
            }
        }
        return null;
    }
}
