package sample;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;

public class SignatureMessage {
    private byte[] someData, signature;
    private RSAEncryption rsAcrypt;
    public SignatureMessage(byte[] data,byte[] sign) throws Exception{
        this.someData=data;
        this.signature=sign;
        rsAcrypt=new RSAEncryption(1024);
    }

    public PrivateKey getPrivateKey(){
        return this.rsAcrypt.getPrivateKey();
    }

    public PublicKey getPublicKey(){
        return this.rsAcrypt.getPublicKey();
    }

    public void setPublicKey(byte[] key){
        rsAcrypt.setPublicKey(key);
    }

    public void setPrivateKey(byte[] key){
        rsAcrypt.setPrivateKey(key);
    }

    public byte[] createSignature(){
        if(someData!=null){
            try{
                Signature signature=Signature.getInstance("SHA256WithRSA");
                SecureRandom secureRandom=new SecureRandom();
                rsAcrypt.createNewKeys();
                signature.initSign(rsAcrypt.getPrivateKey(),secureRandom);
                signature.update(someData);
                return signature.sign();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }

    public boolean verifySignature(){
        if(someData !=null && signature!=null){
            try{
                Signature sign=Signature.getInstance("SHA256WithRSA");
                sign.initVerify(rsAcrypt.getPublicKey());
                sign.update(someData);
                return sign.verify(signature);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }
}
