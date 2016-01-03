package org.testng.remote.strprotocol;


/**
 * String based protocol main interface to be used with remote listeners.
 *
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public interface IStringMessage extends IMessage {
  String getMessageAsString();
}
