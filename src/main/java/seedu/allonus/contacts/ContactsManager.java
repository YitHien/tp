package seedu.allonus.contacts;

import seedu.allonus.contacts.entry.Contact;
import seedu.allonus.storage.StorageFile;
import seedu.allonus.ui.TextUi;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static seedu.allonus.contacts.ContactParser.getFieldStrings;
import static seedu.allonus.contacts.ContactParser.parseContact;
import static seedu.allonus.contacts.ContactParser.setContactFields;
import static seedu.allonus.ui.TextUi.showToUser;

/**
 * The Contacts Manager and associated methods.
 */
public class ContactsManager {

    public static final String CONTACTS_ENTER_LOG_MESSAGE =
            "Entering Contacts Manager";
    private static final String CONTACTS_WELCOME_MESSAGE =
            "Welcome to Contacts Manager";
    public static final String CONTACTS_EXIT_LOG_MESSAGE =
            "Exiting Contacts Manager";

    public static final String CONTACTS_INVALID_COMMAND_LOG_MESSAGE =
            "Invalid command to Contacts Manager: %s";
    private static final String CONTACTS_INVALID_COMMAND_MESSAGE =
            "Please enter a valid command for the Contacts Manager!\n"
                    + "You can try \"list\", \"add\", or \"rm\"";

    public static final String CONTACTS_ENUMERATE_HEADER = " %d. %s\n";
    private static final String CONTACTS_EMPTY_LIST_MESSAGE =
            "You haven't added any contacts to your list yet!";
    private static final String CONTACTS_LIST_SUCCESS_MESSAGE =
            "Here are the contacts in your list:\n";

    private static final String CONTACTS_REMOVE_SUCCESS_MESSAGE =
            "Noted. I've removed this contact:\n  ";
    private static final String CONTACTS_REMOVE_INVALID_INDEX_MESSAGE =
            "You can only delete with a valid number that's in the list :')";
    private static final String CONTACTS_ADD_SUCCESS_MESSAGE =
            "Got it. I've added this contact:\n  ";
    private static final String CONTACTS_UPDATED_LIST_SIZE_MESSAGE =
            "\nNow you have %d contacts in the list.";

    public static final String CONTACTS_FIND_EMPTY_KEYWORD_MESSAGE =
            "You need to specify the keyword you want to find!";
    public static final String CONTACTS_FIND_MULTIPLE_KEYWORDS_MESSAGE =
            "Please only enter one keyword!";
    public static final String CONTACTS_FIND_NO_MATCHES_MESSAGE =
            "There are no contacts matching this keyword!";
    public static final String CONTACTS_FIND_SUCCESS_MESSAGE =
            "Here are the matching contacts in your list:\n";

    private static final String CONTACTS_EDIT_INVALID_INDEX_MESSAGE =
            "You can only edit with a valid number that's in the list :')";
    private static final String CONTACTS_EDIT_NO_FIELDS_MESSAGE =
            "You need to specify the contact field(s) you want to edit!";
    private static final String CONTACTS_EDIT_SUCCESS_MESSAGE =
            "Okay, I've updated the information of this contact:\n  ";

    private static final Logger logger = Logger.getLogger("");
    private static final int CONTACTS_LIST_MAX_SIZE = 100;
    private static final ArrayList<Contact> contactsList = new ArrayList<>(CONTACTS_LIST_MAX_SIZE);

    private static StorageFile storageFile = new StorageFile();
    private static boolean isModified = false;

    /**
     * Prints a message following a defined format.
     *
     * @param message Message to print.
     */
    public static void printFormat(String message) {
        showToUser(message);
    }

    private static void contactsWelcome() {
        printFormat(CONTACTS_WELCOME_MESSAGE);
        logger.log(Level.FINER, CONTACTS_ENTER_LOG_MESSAGE);
    }

    private static void listContacts() {
        if (contactsList.size() == 0) {
            printFormat(CONTACTS_EMPTY_LIST_MESSAGE);
            return;
        }

        String listAsString = "";
        for (int i = 0; i < contactsList.size(); i++) {
            Contact curr = contactsList.get(i);
            String currEntry = String.format(CONTACTS_ENUMERATE_HEADER, i + 1, curr);
            listAsString = listAsString.concat(currEntry);
        }
        printFormat(CONTACTS_LIST_SUCCESS_MESSAGE + listAsString);
    }

    /**
     * Returns current number of items in contacts list.
     *
     * @return number of items in contacts list.
     */
    public int getContactsCount() {
        return contactsList.size();
    }

    /**
     * Returns logger attribute of this class ContactsManager.
     *
     * @return logger, an instance of class <code>Logger</code>, belonging to this <code>ContactsManager</code> class.
     */
    public Logger getLogger() {
        return logger;
    }


    /**
     * Returns current contacts list.
     *
     * @return contacts list.
     */
    public ArrayList<Contact> getContactsList() {
        return contactsList;
    }

    private static void deleteContact(String userInput) {
        Contact curr;
        try {
            int taskInd = ContactParser.parseNum(userInput);
            curr = contactsList.get(taskInd);
            assert taskInd < contactsList.size();
            contactsList.remove(taskInd);
            assert taskInd >= 0;
            assert taskInd < CONTACTS_LIST_MAX_SIZE;
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            printFormat(CONTACTS_REMOVE_INVALID_INDEX_MESSAGE);
            return;
        }
        printFormat(CONTACTS_REMOVE_SUCCESS_MESSAGE + curr
                + String.format(CONTACTS_UPDATED_LIST_SIZE_MESSAGE, contactsList.size()));
        isModified = true;
    }

    private static void addContact(String userInput, boolean fromCommandLine) {
        Contact contact;
        try {
            contact = parseContact(userInput);
        } catch (InvalidContactField e) {
            printFormat(e.getMessage());
            return;
        }
        contactsList.add(contact);
        if (fromCommandLine) {
            printFormat(CONTACTS_ADD_SUCCESS_MESSAGE + contact
                    + String.format(CONTACTS_UPDATED_LIST_SIZE_MESSAGE, contactsList.size()));
        }
        isModified = true;
    }

    /**
     * Executes <code>addContact</code> method with saved contact entry from data file.
     *
     * @param savedContact the saved contact entry
     */
    public void loadAdd(String savedContact) {
        addContact(savedContact, false);
    }

    /**
     * Prints contacts that contain a certain keyword.
     *
     * @param userInput String of user input to parse.
     */
    private static void findContacts(String userInput) {
        String[] commands = userInput.split(" ");
        if (commands.length == 1) {
            printFormat(CONTACTS_FIND_EMPTY_KEYWORD_MESSAGE);
            return;
        }
        if (commands.length > 2) {
            printFormat(CONTACTS_FIND_MULTIPLE_KEYWORDS_MESSAGE);
            return;
        }
        String keyword = commands[1];

        String listAsString = "";
        for (int i = 0; i < contactsList.size(); i++) {
            Contact curr = contactsList.get(i);
            String contactName = curr.getName().toString();
            if (contactName.contains(keyword)) {
                String currEntry = String.format(CONTACTS_ENUMERATE_HEADER, i + 1, curr);
                listAsString = listAsString.concat(currEntry);
            }
        }
        if (!listAsString.equals("")) {
            printFormat(CONTACTS_FIND_SUCCESS_MESSAGE + listAsString);
        } else {
            printFormat(CONTACTS_FIND_NO_MATCHES_MESSAGE);
        }
    }

    private static void editContact(String userInput) {
        Contact curr;
        try {
            int taskInd = ContactParser.parseNum(userInput);
            curr = contactsList.get(taskInd);
            assert taskInd >= 0;
            assert taskInd <= contactsList.size();
            assert taskInd < CONTACTS_LIST_MAX_SIZE;
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            printFormat(CONTACTS_EDIT_INVALID_INDEX_MESSAGE);
            return;
        }

        ArrayList<String> fieldStrings = getFieldStrings(userInput);
        if (fieldStrings.isEmpty()) {
            printFormat(CONTACTS_EDIT_NO_FIELDS_MESSAGE);
            return;
        }
        try {
            setContactFields(curr, fieldStrings);
        } catch (InvalidContactField e) {
            printFormat(e.getMessage());
        }
        printFormat(CONTACTS_EDIT_SUCCESS_MESSAGE + curr);
        isModified = true;
    }

    /**
     * Handles user inputs and calls methods corresponding
     * to the relevant commands.
     *
     * @param ui An TextUi object for getting user input.
     */
    public static void contactsRunner(TextUi ui) {
        contactsWelcome();
        String userInput;
        while (true) {
            isModified = false;
            userInput = ui.getUserInput();
            if (userInput.equals("menu")) {
                logger.log(Level.FINER, CONTACTS_EXIT_LOG_MESSAGE);
                return;
            } else if (userInput.equals("list")) {
                listContacts();
            } else if (userInput.startsWith("rm")) {
                deleteContact(userInput);
            } else if (userInput.startsWith("add")) {
                addContact(userInput, true);
            } else if (userInput.startsWith("find")) {
                findContacts(userInput);
            } else if (userInput.startsWith("edit")) {
                editContact(userInput);
            } else {
                printFormat(CONTACTS_INVALID_COMMAND_MESSAGE);
                logger.log(Level.FINER, String.format(CONTACTS_INVALID_COMMAND_LOG_MESSAGE, userInput));
            }
            if (isModified) {
                storageFile.saveData();
            }
        }
    }


}
