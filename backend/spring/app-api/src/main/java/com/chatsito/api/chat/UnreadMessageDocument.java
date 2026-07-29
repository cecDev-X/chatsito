package com.chatsito.api.chat;

import com.chatsito.compat.LegacyFields;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "UnReadedMsg")
public class UnreadMessageDocument {
    @Id
    private ObjectId id;
    private String mainUserid;
    private String otherUserid;
    private int numOfUnreadedMessages;
    @Field(LegacyFields.UNREAD_READ)
    private boolean read;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getMainUserid() {
        return mainUserid;
    }

    public void setMainUserid(String mainUserid) {
        this.mainUserid = mainUserid;
    }

    public String getOtherUserid() {
        return otherUserid;
    }

    public void setOtherUserid(String otherUserid) {
        this.otherUserid = otherUserid;
    }

    public int getNumOfUnreadedMessages() {
        return numOfUnreadedMessages;
    }

    public void setNumOfUnreadedMessages(int numOfUnreadedMessages) {
        this.numOfUnreadedMessages = numOfUnreadedMessages;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
