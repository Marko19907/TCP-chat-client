package no.ntnu.datakomm.chat;

import java.util.Objects;

/**
 * Represents a chat message
 */
public class TextMessage {

    private final String sender;
    private final boolean priv;
    private final String text;

    /**
     * @param sender Username of the sender
     * @param priv When true, message is private
     * @param text Text of the message
     */
    public TextMessage(String sender, boolean priv, String text) {
        this.sender = sender;
        this.priv = priv;
        this.text = text;
    }

    public String getSender() {
        return this.sender;
    }

    public boolean isPrivate() {
        return this.priv;
    }

    public String getText() {
        return this.text;
    }

    @Override
    public String toString() {
        return (this.priv ? "PRIVATE " : "") + " from " + this.sender + ": " + this.text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof final TextMessage message)) {
            return false;
        }
        return this.priv == message.priv
                && this.getSender().contains(message.getSender())
                && this.getText().contains(message.getText());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.sender);
        hash = 71 * hash + (this.priv ? 1 : 0);
        hash = 71 * hash + Objects.hashCode(this.text);
        return hash;
    }

}
