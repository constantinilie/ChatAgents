package usv.gui;

import usv.chat.ConversationStore;

import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class ChatController {

  private final ChatWindow win;
  private final ConversationStore convs;
  private final ChatCallbacks cb;

  public ChatController(ChatWindow win, ConversationStore convs, ChatCallbacks cb) {
    this.win = win;
    this.convs = convs;
    this.cb = cb;

    wire();
  }

  public void show() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() { win.setVisible(true); }
    });
  }

  public void setUsers(final List<String> users) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() { win.setUsers(users); }
    });
  }

  public void appendLine(final String line) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() { win.appendLine(line); }
    });
  }

  public void showConversation(final String peer) {
    final String text = convs.getConversationText(peer);
    SwingUtilities.invokeLater(new Runnable() {
      public void run() { win.setChatText(text); }
    });
  }

  private void wire() {
    // When selecting a user, show only that conversation
    win.onUserSelected(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        String sel = win.getSelectedUser();
        convs.setActivePeer(sel);

        showConversation(sel);

        if (cb != null) cb.onPeerSelected(sel);
      }
    });

    // Send button
    win.onSend(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String to = win.getSelectedUser();
        String text = win.getInputTextAndClear();

        if (to == null || to.trim().isEmpty()) {
          appendLine("[system] Select a user first.");
          return;
        }
        if (text == null || text.trim().isEmpty()) return;

        if (cb != null) cb.onSend(to, text);
      }
    });

    // Close -> callback
    win.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        if (cb != null) cb.onWindowClosed();
      }
      @Override
      public void windowClosing(WindowEvent e) {
        if (cb != null) cb.onWindowClosed();
      }
    });
  }
}
