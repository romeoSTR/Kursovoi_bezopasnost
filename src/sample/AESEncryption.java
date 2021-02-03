package sample;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;


public class AESEncryption {
    private SecretKey key ;
    private String text;
    private byte[] cryptionKey,resultText;
    private SignatureMessage signatureMessage;
    private final static int KEY_LENGTH = 256;
    public static int ENCRYPTION=1;
    public static int DECRYPTION=2;



    AESEncryption(String text,int actionType){
        key=null;
        cryptionKey=null;
        this.text=text;
        if(actionType==1){
            generateKey();
        }
    }

    //используется в конструкторе при генерации ключа
    private void generateKey(){
        try{
            KeyGenerator keyGenerator=KeyGenerator.getInstance("AES");
            SecureRandom secureRandom=new SecureRandom();
            keyGenerator.init(KEY_LENGTH,secureRandom);
            key=keyGenerator.generateKey();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String cryptMessage(){
        try{
            Cipher cipher=Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE,key);
            return Base64.getEncoder().encodeToString(cipher.doFinal(text.getBytes("UTF-8")));
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public String decryptMessage(){
        if(key!=null){
            try {
                Cipher cipher=Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(Cipher.DECRYPT_MODE,key);
                return new String(cipher.doFinal(Base64.getDecoder().decode(text)));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return "";
    }

    public byte[] getSignatureKey(){
        return this.signatureMessage.getPublicKey().getEncoded();
    }

    public byte[] getCryptionKey(byte [] publicKey){
        return cryptKey(publicKey);
    }

    public void setCryptKey(byte[] cryptKey,byte[] privateKey){
        this.cryptionKey=cryptKey;
        decryptKey(privateKey);
    }


    //используется в методе назначения ключа шифрования setCryptKey
    private void decryptKey(byte[] privateKey){
        RSAEncryption rsAcrypt=new RSAEncryption(this.cryptionKey);
        rsAcrypt.setPrivateKey(privateKey);
        this.key =new SecretKeySpec(rsAcrypt.decryptData(),"AES");
    }

    //используется в методе получения ключа шифрования getCryptionKey
    private byte[] cryptKey(byte[] publicKey){
        RSAEncryption rsAcrypt=new RSAEncryption(key.getEncoded());
        rsAcrypt.setPublicKey(publicKey);
        this.cryptionKey=rsAcrypt.cryptData();
        return this.cryptionKey;
    }

    public byte[] createSignature(){
        if(text!=null){
            try{
                byte[] dataFromFile=this.text.getBytes("UTF-8");
                signatureMessage =new SignatureMessage(dataFromFile,null);
                return signatureMessage.createSignature();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }

    public boolean verifySignature( byte[] digitSign,byte[] publicKey){
        if(text!=null && digitSign !=null){
            try{
                signatureMessage =new SignatureMessage(this.resultText,digitSign);
                signatureMessage.setPublicKey(publicKey);
                return signatureMessage.verifySignature();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }

}
