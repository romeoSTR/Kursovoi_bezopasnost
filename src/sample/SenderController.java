package sample;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Properties;

public class SenderController {
    @FXML
    Button send_message_but, open_file_to_attach;
    @FXML
    TextArea sended_message_text;
    @FXML
    TextField getter,message_topic,sender,attached_files;
    @FXML
    CheckBox cryption;

    private AESEncryption aeScrypt;

    @FXML
    void initialize() {
        sender.setEditable(false);
        sender.setText(Controller.currentUser);
        open_file_to_attach.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                    }
                }
        );
        send_message_but.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        if (!getter.getText().equalsIgnoreCase("")) {
                            String topic = "Без темы";
                            if (!message_topic.getText().equalsIgnoreCase(""))
                                topic = message_topic.getText();
                            SendMessage message = new SendMessage(getter.getText(), topic);
                        }
                    }
                }
        );
    }

    public class SendMessage {
        SendMessage(String getter,String topic) {
            try {
                InputStream is = new FileInputStream(Main.PROPS_FILE);
                if (is != null) {
                    Reader reader = new InputStreamReader(is, "UTF-8");
                    Properties pr = new Properties();
                    pr.load(reader);
                    SendEmail.EMAIL_FROM = pr.getProperty("email");
                    SendEmail.REPLY_TO = pr.getProperty("email");
                    SendEmail.FILE_PATH = Main.PROPS_FILE;
                    String text = sended_message_text.getText();
                    is.close();
                    SendEmail se = new SendEmail(getter, topic);
                    if (cryption.isSelected()) {
                        if(Controller.isPublicKey && Controller.isPrivateKey){
                            String resText=makeTextCrypt(text);
                            se.sendMessage(resText);
                        }else{
                            Controller.alertError.setContentText("Вы не выбрали публичный и приватный ключ в настройках клиента");
                            Controller.alertError.showAndWait();
                        }
                    }else
                        se.sendMessage(text);
                    System.out.println("Сообщение отправлено");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public String makeTextCrypt(String text){
        this.aeScrypt=new AESEncryption(text,AESEncryption.ENCRYPTION);
        String str=this.aeScrypt.cryptMessage();
        byte[] data = Base64.getDecoder().decode(str);
        byte[] signature=this.aeScrypt.createSignature();
        byte[] buf=new byte[data.length+signature.length];
        System.arraycopy(data,0,buf,0,data.length);
        System.arraycopy(signature,0,buf,data.length,signature.length);
        byte[] resultText=buf;
        String result=Base64.getEncoder().encodeToString(resultText);
        sended_message_text.setText(result);
        saveKeys();
        return result;
    }
    private void saveKeys(){
        try{
            File public_key_file=new File("encrypt.key");
            byte[] public_key= Files.readAllBytes(Controller.public_key.getAbsoluteFile().toPath());
            System.out.println("Публичный ключ: "+public_key);
            File messageDir=new File(Controller.currentUser+'/'+Controller.currentMessageNumber);
            messageDir.mkdirs();
            byte[] cryptedPublicKey=aeScrypt.getCryptionKey(public_key);
            System.out.println("Публичный ключ, зашифрованный: "+cryptedPublicKey);
            Controller.saveKeysAsFiles(Controller.currentUser+'/'+messageDir.getName()+'/'+public_key_file.getName(),cryptedPublicKey);
            File sign=new File("sign.key");
            Controller.saveKeysAsFiles(Controller.currentUser+'/'+messageDir.getName()+'/'+sign.getName(),aeScrypt.getSignatureKey());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
