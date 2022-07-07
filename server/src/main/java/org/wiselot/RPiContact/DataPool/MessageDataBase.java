package org.wiselot.RPiContact.DataPool;

import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class MessageDataBase extends DataBase{

    private String UUID_STR = "uuid";
    private String sendUUID_STR = "sendUUID";
    private String getUUID_STR = "getUUID";
    private String Text_STR = "Text";
    private String sendTime_STR = "sendTime";

    private static String table;

    public MessageDataBase(String driver,String table) {
        super(driver);
        MessageDataBase.table = table;
    }
    public MessageDataBase(String driver,String table,
                           String UUID_STR,String sendUUID_STR,String getUUID_STR,String Text_STR,String sendTime_STR){
        super(driver);
        MessageDataBase.table = table;
        this.getUUID_STR = getUUID_STR;
        this.sendUUID_STR = sendUUID_STR;
        this.UUID_STR = UUID_STR;
        this.sendTime_STR = sendTime_STR;
        this.Text_STR = Text_STR;
    }

    public Message getMessage(@NotNull UUID uuid) throws SQLException {
        ResultSet resultSet = super.selectObjects(new Object[]{},
                "select * from " + table + " where " + UUID_STR + "=\"" + uuid + "\";");
        resultSet.next();
        return new Message(UUID.fromString(resultSet.getString(UUID_STR)),
                UUID.fromString(resultSet.getString(sendUUID_STR)),
                UUID.fromString(resultSet.getString(getUUID_STR)),
                resultSet.getString(Text_STR),resultSet.getDate(sendTime_STR));
    }

    public ArrayList<Message> getMessage(Date start,Date end){
        return new ArrayList<>();
    }
    public ArrayList<Message> getMessage(AccountDataBase.@NotNull Account account, boolean isSender) throws SQLException {
        ResultSet resultSet = super.selectObjects(new Object[]{},
                "select * from " + table + " where " +
                        (isSender ? sendUUID_STR : getUUID_STR) + "=\"" + account.getUuid() + "\";");
        ArrayList<Message> gets = new ArrayList<>();
        while(resultSet.next()) {
            gets.add(new Message(UUID.fromString(resultSet.getString(UUID_STR)),
                    UUID.fromString(resultSet.getString(sendUUID_STR)),
                    UUID.fromString(resultSet.getString(getUUID_STR)),
                    resultSet.getString(Text_STR), resultSet.getDate(sendTime_STR)));
        }
        return gets;
    }
    public void updateDataBase(MessageDataBase old){
        // 更新数据库(同步数据)
    }

    public class Message{
        private final UUID uuid;
        private final UUID sendUUID;
        private final UUID getUUID;
        private final String Text;
        private final Date sendDate;

        public  Message(UUID uuid,UUID sendUUID,UUID getUUID,String Text,Date sendDate){
            this.uuid = uuid;
            this.getUUID = getUUID;
            this.sendUUID = sendUUID;
            this.Text = Text;
            this.sendDate = sendDate;
        }

        public String getText() {
            return Text;
        }

        public UUID getGetUUID() {
            return getUUID;
        }

        public UUID getSendUUID() {
            return sendUUID;
        }

        public Date getSendDate() {
            return sendDate;
        }

        public UUID getUuid() {
            return uuid;
        }
    }
}
