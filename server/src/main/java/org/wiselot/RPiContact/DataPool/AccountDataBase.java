package org.wiselot.RPiContact.DataPool;

import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class AccountDataBase extends DataBase{

    private String table;
    private String NAME_STR = "name";
    private String PASSWD_STR = "password";
    private String UUID_STR = "uuid";

    public AccountDataBase(String driver,String table) {
        super(driver);
        this.table = table;
    }
    public AccountDataBase(String driver,String table,
                           String NAME_STR,String PASSWD_STR,String UUID_STR){
        super(driver);
        this.table = table;
        this.NAME_STR = NAME_STR;
        this.PASSWD_STR = PASSWD_STR;
        this.UUID_STR = UUID_STR;
    }
    public ArrayList<Account> getAccount(String name) throws SQLException {
        ResultSet resultSet = super.selectObjects(new Object[]{},
                "select * from " + table + " where " + NAME_STR + "=" + "\"" + name + "\";");
        ArrayList<Account> accounts = new ArrayList<>();
        while(resultSet.next()){
            accounts.add(new Account(resultSet.getString(NAME_STR),
                    resultSet.getString(PASSWD_STR),
                    UUID.fromString(resultSet.getString(UUID_STR))));
        }
        return accounts;
    }
    public Account getAccount(@NotNull UUID uuid) throws SQLException {
        ResultSet resultSet = super.selectObjects(new Object[]{},
                "select * from " + table + " where " + UUID_STR + " = \"" + uuid.toString() + "\";");
        resultSet.next();
        return new Account(resultSet.getString(NAME_STR),
                resultSet.getString(PASSWD_STR),
                UUID.fromString(resultSet.getString(UUID_STR)));
    }
    /* insert into RPiAccount values ('test','123456',0); */
    public void addAccount(@NotNull Account account) throws SQLException {
        super.updateObjects(new Object[]{account.getName(),account.getPasswd()},
                "insert into " + table + "values(NULL,?,?);");
    }
    public void addAccounts(@NotNull Account @NotNull [] accounts) throws SQLException {
        String sql = "insert into " + table + " values";
        for(Account account : accounts){
            sql.concat("(NULL,"+account.getName()+","+account.getPasswd()+"),");
        }
        sql.substring(0,sql.length()-1);
        sql.concat(";");
        super.updateObjects(new Object[]{},sql);
    }
    public void deleteAccount(@NotNull Account account) throws SQLException {
        super.updateObjects(new Object[]{},
                "delete from " + table + " where " + UUID_STR + "=\"" + account.getUuid().toString() + "\";");
    }
    public void deleteAccounts(@NotNull Account @NotNull [] accounts) throws SQLException {
        String sqlAdd = "(";
        for(Account account : accounts){
            sqlAdd.concat("\"" + account.getUuid().toString() + "\",");
        }
        sqlAdd.substring(0,sqlAdd.length()-1);
        sqlAdd.concat(")");
        super.updateObjects(new Object[]{},
                "delete from " + table + " where " + UUID_STR + "in " + sqlAdd);
    }
    public void updateAccount(@NotNull Account dest, @NotNull Account src) throws SQLException {
        super.updateObjects(new Object[]{table,NAME_STR,src.getName(),PASSWD_STR,src.getPasswd(),UUID_STR,dest.getUuid().toString()},
                "update " + table +
                        "set " + NAME_STR + "=\"" + src.getName() + "\"," +
                        PASSWD_STR + "=\"" + src.getPasswd() + "\" where " + UUID_STR + "=\"" + dest.getUuid() + "\";");
    }
    public void updateAccounts(Account @NotNull [] dest, Account @NotNull [] src) throws SQLException {
        // sql语句实在没耐心干了...
        for(int i=0;i< dest.length;i++){
            updateAccount(dest[i],src[i]);
        }
    }
    public static class Account{
        private String name;
        private String passwd;
        private UUID uuid;
        public Account(String name,String passwd,UUID uuid){
            this.name = name;
            this.passwd = passwd;
            this.uuid = uuid;
        }

        public String getName(){
            return name;
        }

        public String getPasswd(){
            return passwd;
        }

        public UUID getUuid(){
            return uuid;
        }
    }
}