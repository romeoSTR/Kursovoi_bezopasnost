package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sample.database.DbHandler;
import sample.database.entities.Account;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

public class Controller {
    public static int currentMessageNumber=0;
    final FileChooser fileChooser=new FileChooser();
    private static ReadEmail readEmail=null;
    public static String currentUser;
    public static Session session;
    public static String currentReadedMessageText;
    public static File public_key_decrypt_file,public_key,private_key,
                        sign;
    public static boolean init=true;
    public static boolean isPublicKey=false,isPrivateKey=false,
                            isPublicKeyDecrypt=false,isSign=false;
    public static Alert alertError = new Alert(Alert.AlertType.ERROR);
    public static Alert alertInfo = new Alert(Alert.AlertType.INFORMATION);
    public static DbHandler dbHandler;
    private AESEncryption aeScrypt;
    @FXML
    Button write_new_message, add_new_account, delete_account,
            add_public_key, add_private_key,decrypt_message,generate_keys,
            prev_message,next_message,update_inbox,import_public,import_sign;
    @FXML
    ChoiceBox current_folder, current_account, set_get_protocol, del_account_picker;
    @FXML
    TextField private_key_field, public_key_field, login_field, password_field,public_key_decrypt,
            sign_decrypt;
    @FXML
    Label messages_count,currentMessage;
    @FXML
    TextArea message_text;
    @FXML
    void initialize() {
        private_key_field.setEditable(false);
        public_key_field.setEditable(false);
        public_key_decrypt.setEditable(false);
        sign_decrypt.setEditable(false);
        alertError.setHeaderText("Произошла ошибка!");
        alertInfo.setHeaderText("Сообщение");
        try {
            dbHandler = DbHandler.getInstance();
            updateAccountChoiceBoxes();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        readEmail=new ReadEmail();
        String[] userKeys=dbHandler.getAccountKeys(currentUser);
        if(userKeys[0]!=null){
            public_key=new File(userKeys[0]);
            public_key_field.setText(public_key.getName());
            isPublicKey=true;
        }
        if(userKeys[1]!=null){
            private_key=new File(userKeys[1]);
            private_key_field.setText(private_key.getName());
            isPrivateKey=true;
        }
        set_get_protocol.getItems().add("Imap");
        set_get_protocol.getItems().add("Pop-3");
        set_get_protocol.getSelectionModel().select("Imap");
        write_new_message.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        try {
                            openMessageWindow("new");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
        update_inbox.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        current_folder.getItems().clear();
                        readEmail=new ReadEmail();
                    }
                }
        );
        add_new_account.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        if (login_field.getText().equalsIgnoreCase("") ||
                                password_field.getText().equalsIgnoreCase("")) {
                            alertError.setContentText("Введите логин и пароль");
                            alertError.showAndWait();
                        } else {
                            Account account = new Account(0, login_field.getText(), password_field.getText(),"","");
                            dbHandler.addAccount(account);
                            alertInfo.setContentText("Аккаунт успешно добавлен");
                            alertInfo.showAndWait();
                            login_field.setText("");
                            password_field.setText("");
                            File userDir=new File(currentUser);
                            updateAccountChoiceBoxes();
                        }
                    }
                }
        );
        delete_account.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        if (del_account_picker.getItems() == null) {
                            alertError.setContentText("Нет аккаунтов для удаления");
                            alertError.showAndWait();
                        } else if (del_account_picker.getSelectionModel().getSelectedItem() != null) {
                            String login = del_account_picker.getSelectionModel().getSelectedItem().toString();
                            int id = dbHandler.getAccountIdByLogin(login);
                            dbHandler.deleteAccount(id);
                            alertInfo.setContentText("Аккаунт " + login + " успешно удален");
                            File userDir=new File(currentUser);
                            userDir.delete();
                            alertInfo.showAndWait();
                        } else {
                            alertError.setContentText("Выберите аккаунт для удаления");
                            alertError.showAndWait();
                        }
                    }
                }
        );
        decrypt_message.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        //расшифровка сообщения
                        if(isPublicKeyDecrypt && isPrivateKey && isSign){
                            //расшифровка
                            System.out.println("Считанное сообщение, зашифрованное:" + currentReadedMessageText);
                            makeMessageDecrypt(currentReadedMessageText);
                        }else{
                            alertError.setContentText("Выберите публичный и приватный ключи и файл ЭЦП");
                            alertError.showAndWait();
                        }
                    }
                }
        );
        prev_message.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        currentMessageNumber+=1;
                        if (current_folder.getSelectionModel().getSelectedItem().toString().equalsIgnoreCase("INBOX"))
                            readEmail.readMessagesFromFolder(current_folder.getSelectionModel().getSelectedItem().toString());
                        else
                            readEmail.readMessagesFromFolder("[Gmail]/"+current_folder.getSelectionModel().getSelectedItem().toString());
                    }
                }
        );
        next_message.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        currentMessageNumber-=1;
                        if (current_folder.getSelectionModel().getSelectedItem().toString().equalsIgnoreCase("INBOX"))
                            readEmail.readMessagesFromFolder(current_folder.getSelectionModel().getSelectedItem().toString());
                        else
                            readEmail.readMessagesFromFolder("[Gmail]/"+current_folder.getSelectionModel().getSelectedItem().toString());
                    }
                }
        );
        current_folder.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                try {
                    if(current_folder.getSelectionModel().getSelectedItem().toString().equalsIgnoreCase("Inbox"))
                        currentMessageNumber=readEmail.readMessagesFromFolder("INBOX");
                    else{
                        String folder=current_folder.getSelectionModel().getSelectedItem().toString();
                        currentMessageNumber=readEmail.readMessagesFromFolder("[Gmail]/"+folder);
                    }
                }catch (NullPointerException e){
                    System.out.println("Успешно очищено");
                }
            }
        });
        current_account.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                updatePropertiesFile(current_account.getSelectionModel().getSelectedItem().toString());
                alertInfo.setContentText("Для продолжения работы необходимо перезапустить приложение");
                alertInfo.showAndWait();
                try{
                    Thread.sleep(1000);
                }catch (Exception e){
                    e.printStackTrace();
                }
                System.exit(1);
            }
        });
        import_public.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        public_key_decrypt_file = fileChooser.showOpenDialog(null);
                        if (public_key_decrypt_file != null) {
                            public_key_decrypt.setText(public_key_decrypt_file.getName());
                            isPublicKeyDecrypt=true;
                        }
                    }
                }
        );
        import_sign.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        sign=fileChooser.showOpenDialog(null);
                        if(sign!=null){
                            sign_decrypt.setText(sign.getName());
                            isSign=true;
                        }
                    }
                }
        );
        add_public_key.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        public_key=fileChooser.showOpenDialog(null);
                        if(public_key!=null){
                            public_key_field.setText(public_key.getName());
                            dbHandler.updatePublicKeyOnAccount(currentUser,public_key.getAbsolutePath());
                            isPublicKey=true;
                        }
                    }
                }
        );
        add_private_key.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        private_key=fileChooser.showOpenDialog(null);
                        if(private_key != null){
                            private_key_field.setText(private_key.getName());
                            dbHandler.updatePrivateKeyOnAccount(private_key.getAbsolutePath());
                            isPrivateKey=true;
                        }
                    }
                }
        );
        generate_keys.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        generateKeys(currentUser);
                    }
                }
        );
    }

    public void makeMessageDecrypt(String text){
        try{
            byte[] signature=new byte[128];
            byte[] textArray= text.getBytes("UTF-8");
            System.arraycopy(textArray,textArray.length-signature.length,
                    signature,0,signature.length);
            byte[] buffer=new byte[textArray.length-128];
            System.arraycopy(textArray,0,buffer,0,buffer.length);
            this.aeScrypt=new AESEncryption(Base64.getEncoder().encodeToString(buffer),AESEncryption.DECRYPTION);
            byte[] publicKey= Files.readAllBytes(public_key_decrypt_file.toPath());
            byte[] privateKey=Files.readAllBytes(private_key.toPath());
            aeScrypt.setCryptKey(publicKey,privateKey);
            String decryptedMessage=aeScrypt.decryptMessage();
            byte[] signKey=Files.readAllBytes(sign.toPath());
            boolean check=this.aeScrypt.verifySignature(signature,signKey);
            if(check) {
                alertInfo.setContentText("Верификация пройдена");
            }else{
                alertInfo.setContentText("Верификация провалена");
            }
            message_text.setText(decryptedMessage);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void generateKeys(String login){
        try{
            RSAEncryption rsAcrypt=new RSAEncryption(1024);
            rsAcrypt.createNewKeys();
            saveKeysAsFiles(login+"/public.key",rsAcrypt.getPublicKey().getEncoded());
            saveKeysAsFiles(login+"/private.key",rsAcrypt.getPrivateKey().getEncoded());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void saveKeysAsFiles(String fileName,byte[] key){
        try(FileOutputStream fos=new FileOutputStream(fileName)){
            byte[] buffer=key;
            fos.write(buffer);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void updatePropertiesFile(String email){
        try (OutputStream output = new FileOutputStream(Main.PROPS_FILE)) {
            Properties prop = new Properties();
            // set the properties value
            prop.setProperty("email", email);
            prop.setProperty("port","465");
            prop.setProperty("server","smtp.gmail.com");
            String[] array=email.split("@");
            prop.setProperty("user",array[0]);
            // save properties to project root folder
            prop.store(output, null);
        } catch (IOException io) {
            io.printStackTrace();
        }

    }

    public void openMessageWindow(String mode) throws Exception {
        Stage primaryStage = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("send_message.fxml"));
        primaryStage.setTitle("Новое сообщение");
        primaryStage.setScene(new Scene(root, 320, 495.0));
        primaryStage.show();
        if (mode.equalsIgnoreCase("new")) {

        } else if (mode.equalsIgnoreCase("edit")) {

        }
    }

    public void updateAccountChoiceBoxes() {
        current_account.getItems().clear();
        del_account_picker.getItems().clear();
        List<Account> accounts = dbHandler.getAllAccounts();
        for (Account account : accounts) {
            current_account.getItems().add(account.login);
            del_account_picker.getItems().add(account.login);
        }
    }



    public class ReadEmail {
        String IMAP_AUTH_EMAIL;
        String IMAP_AUTH_PWD;
        String IMAP_Server = "imap.gmail.com";
        String IMAP_Port = "993";
        Store store;

        public ReadEmail() {
            Properties pr = getPropertiesFromFile();
            IMAP_AUTH_EMAIL = pr.getProperty("email");
            currentUser= pr.getProperty("email");
            current_account.getSelectionModel().select(IMAP_AUTH_EMAIL);
            IMAP_AUTH_PWD = dbHandler.getAccountPassByLogin(IMAP_AUTH_EMAIL);
            Properties properties = new Properties();
            properties.put("mail.debug", "false");
            properties.put("mail.store.protocol", "imaps");
            properties.put("mail.imap.ssl.enable", "true");
            properties.put("mail.imap.port", IMAP_Port);
            properties.put("mail.smtp.host", "smtp.gmail.com");
            properties.put("mail.smtp.port", "465");
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.socketFactory.class",
                    "javax.net.ssl.SSLSocketFactory");
            Authenticator auth = new EmailAuthenticator(IMAP_AUTH_EMAIL,
                    IMAP_AUTH_PWD);
            session = Session.getDefaultInstance(properties, auth);
            session.setDebug(false);
            try {
                store = session.getStore();
                // Подключение к почтовому серверу
                store.connect(IMAP_Server, IMAP_AUTH_EMAIL, IMAP_AUTH_PWD);
                Folder[] folders = store.getDefaultFolder().list("*");
                for (Folder folder : folders) {
                    current_folder.getItems().add(folder.getName());
                }
                current_folder.getSelectionModel().select("INBOX");
                readMessagesFromFolder("INBOX");
            }catch (Exception e){
                e.printStackTrace();
            }
        }


        public int readMessagesFromFolder(String folderName){
            try {
                message_text.setText("");
                Folder inbox = store.getFolder(folderName);
                if(init) {
                    currentMessageNumber = inbox.getMessageCount();
                    init=false;
                }
                // Открываем папку в режиме только для чтения
                inbox.open(Folder.READ_ONLY);
                messages_count.setText("Кол-во сообщений:"+inbox.getMessageCount());
                if (inbox.getMessageCount() == 0)
                    return 0;
                if (currentMessageNumber>inbox.getMessageCount())
                    currentMessageNumber=1;
                if(currentMessageNumber<1)
                    currentMessageNumber=inbox.getMessageCount();
                // Последнее сообщение; первое сообщение под номером 1
                currentMessage.setText(""+(inbox.getMessageCount()-currentMessageNumber+1));
                Message message = inbox.getMessage(currentMessageNumber);
                Multipart mp = (Multipart) message.getContent();
                currentReadedMessageText=getMessageContent(message);
                message_text.setText(getMessageContent(message));
                return inbox.getMessageCount();
            } catch (NoSuchProviderException e) {
                System.err.println(e.getMessage());
            } catch (MessagingException e) {
                System.err.println(e.getMessage());
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
            return 0;
        }

        private String getMessageContent(Message message) throws MessagingException {
            try {
                Object content = message.getContent();
                if (content instanceof Multipart) {
                    StringBuffer messageContent = new StringBuffer();
                    Multipart multipart = (Multipart) content;
                    for (int i = 0; i < multipart.getCount(); i++) {
                        Part part = multipart.getBodyPart(i);
                        if (part.isMimeType("text/plain")) {
                            messageContent.append(part.getContent().toString());
                        }
                    }
                    return messageContent.toString();
                }
                return content.toString();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }


        public Properties getPropertiesFromFile() {
            try {
                InputStream is = new FileInputStream(Main.PROPS_FILE);
                if (is != null) {
                    Reader reader = new InputStreamReader(is, "UTF-8");
                    Properties pr = new Properties();
                    pr.load(reader);
                    return pr;
                }
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
