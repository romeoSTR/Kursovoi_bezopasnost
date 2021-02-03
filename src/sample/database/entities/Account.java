package sample.database.entities;

public class Account {
    public int id;
    public String login;
    public String password;
    public String public_key;
    public String private_key;

    public Account(int id, String login, String password,String public_key,String private_key) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.public_key=public_key;
        this.private_key=private_key;
    }
}
