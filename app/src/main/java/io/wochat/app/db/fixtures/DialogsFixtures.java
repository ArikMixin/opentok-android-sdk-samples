package io.wochat.app.db.fixtures;





import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import io.wochat.app.db.entity.Contact;
import io.wochat.app.db.entity.Dialog;
import io.wochat.app.db.entity.Message;

/*
 * Created by Anton Bevza on 07.09.16.
 */
public final class DialogsFixtures extends FixturesData {
    private DialogsFixtures() {
        throw new AssertionError();
    }

    public static ArrayList<Dialog> getDialogs() {
        ArrayList<Dialog> chats = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -(i * i));
            calendar.add(Calendar.MINUTE, -(i * i));

            chats.add(getDialog(i, calendar.getTime()));
        }

        return chats;
    }

    private static Dialog getDialog(int i, Date lastMessageCreatedAt) {
        ArrayList<Contact> contacts = getContacts();
        Message msg = getMessage(lastMessageCreatedAt);
        return new Dialog(
            getRandomId(),
            contacts.size() > 1 ? groupChatTitles.get(contacts.size() - 2) : contacts.get(0).getName(),
            contacts.size() > 1 ? groupChatImages.get(contacts.size() - 2) : getRandomAvatar(),
            contacts,
            msg.getMessageId(),
            msg.getCreatedAt(),
            msg.getMessageText(),
            i < 3 ? 3 - i : 0);
    }

    private static ArrayList<Contact> getContacts() {
        ArrayList<Contact> contacts = new ArrayList<>();
        int usersCount = 1 + rnd.nextInt(4);

        for (int i = 0; i < usersCount; i++) {
            contacts.add(getContact());
        }

        return contacts;
    }

    private static Contact getContact() {
        return new Contact(
                getRandomId(),
                getRandomName(),
                getRandomAvatar(),
                getRandomBoolean());
    }

    private static Message getMessage(final Date date) {
        return new Message(
                getRandomId(),
                getContact(),
                getRandomMessage(),
                "EN",
                date.getTime());
    }
}
