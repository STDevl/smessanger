package com.example.smesage;

public class Message {
    private final int messageId;
    private final String senderId;
    private final String receiverId;
    private final String messageText;
    private final String timestamp;

    public Message(int messageId, String senderId, String receiverId, String messageText, String timestamp) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageText = messageText;
        this.timestamp = timestamp;
    }

    public int getMessageId() {
        return messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
