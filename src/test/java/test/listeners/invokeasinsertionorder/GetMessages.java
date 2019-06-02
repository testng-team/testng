package test.listeners.invokeasinsertionorder;

import java.util.List;
import org.testng.collections.Lists;

public class GetMessages {
    private static List<String> messages = Lists.newLinkedList();

    public static List<String> getMessages() {
      return messages;
    }
}
