package sample.database;

import org.sqlite.JDBC;
import sample.database.entities.Account;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DbHandler {
    private static final String connectionStr="jdbc:sqlite:D:/SQLiteStudio/kursovoi";
    private static DbHandler instance=null;
    public static synchronized DbHandler getInstance() throws SQLException {
        if(instance == null){
            instance=new DbHandler();
        }
        return instance;
    }

    private Connection connection;
    private DbHandler() throws SQLException{
        DriverManager.registerDriver(new JDBC());
        this.connection=DriverManager.getConnection(connectionStr);
    }
    
    public List<Account> getAllAccounts(){
        try(Statement statement=this.connection.createStatement()){
            List<Account> accounts=new ArrayList<Account>();
            ResultSet resultSet=statement.executeQuery("Select id,password,login,public_key,private_key from accounts");
            while (resultSet.next()){
                accounts.add(new Account(resultSet.getInt("id"),
                        resultSet.getString("login"),
                        resultSet.getString("password"),
                        resultSet.getString("public_key"),
                        resultSet.getString("private_key")));
            }
            return accounts;
        }catch(SQLException e){
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public void addAccount(Account account){
        try(PreparedStatement ps=this.connection.prepareStatement(
                "INSERT into accounts('login','password') values (?,?)")){
            ps.setObject(1,account.login);
            ps.setObject(2,account.password);
            ps.execute();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void deleteAccount(int id){
        try(PreparedStatement ps=this.connection.prepareStatement(
                "Delete from accounts where id=?")) {
            ps.setObject(1,id);
            ps.execute();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public int getAccountIdByLogin(String login){
        try(PreparedStatement ps=this.connection.prepareStatement(
                "Select id from accounts where login=?")){
            ps.setObject(1,login);
            ResultSet res=ps.executeQuery();
            return res.getInt("id");
        }catch (SQLException e){
            e.printStackTrace();
            return -1;
        }
    }

    public String getAccountPassByLogin(String login){
        try (PreparedStatement ps=this.connection.prepareStatement(
                "Select password from accounts where login=?")){
            ps.setObject(1,login);
            ResultSet res=ps.executeQuery();
            return res.getString("password");
        }catch (SQLException e){
            e.printStackTrace();
            return "null";
        }
    }

    public String[] getAccountKeys(String login) {
        String[] emptyArray = {"", ""};
        try (PreparedStatement ps = this.connection.prepareStatement(
                "Select public_key,private_key from accounts where login=?")) {
            ps.setObject(1, login);
            ResultSet res = ps.executeQuery();
            String[] result = new String[2];
            result[0] = res.getString("public_key");
            result[1] = res.getString("private_key");
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return emptyArray;
        }
    }

    public void updatePublicKeyOnAccount(String login,String public_key){
        try(PreparedStatement ps=this.connection.prepareStatement(
                "Update accounts set public_key=? where login=?")){
            ps.setString(2,login);
            ps.setString(1,public_key);
            ps.execute();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void updatePrivateKeyOnAccount(String private_key){
        try(PreparedStatement ps=this.connection.prepareStatement(
                "Update accounts set private_key=?")){
            ps.setString(1,private_key);
            ps.execute();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
}
