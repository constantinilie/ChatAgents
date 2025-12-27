package usv.gui;

public interface ChatCallbacks {
  void onSend(String to, String text);
  void onPeerSelected(String peer);
  void onWindowClosed();
}
