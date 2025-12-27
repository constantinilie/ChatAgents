package usv.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class ChatWindow extends JFrame {
  private static final long serialVersionUID = 1L;

  private final DefaultListModel<String> usersModel = new DefaultListModel<String>();
  private final JList<String> usersList = new JList<String>(usersModel);

  private final JTextArea chatArea = new JTextArea();
  private final JTextArea inputArea = new JTextArea(3, 30);
  private final JButton sendBtn = new JButton("Send");

  public ChatWindow(String nick) {
    super("Chat - " + nick);

    chatArea.setEditable(false);
    chatArea.setLineWrap(true);
    chatArea.setWrapStyleWord(true);

    inputArea.setLineWrap(true);
    inputArea.setWrapStyleWord(true);

    JScrollPane usersScroll = new JScrollPane(usersList);
    usersScroll.setPreferredSize(new Dimension(160, 300));

    JScrollPane chatScroll = new JScrollPane(chatArea);
    JScrollPane inputScroll = new JScrollPane(inputArea);

    JPanel bottom = new JPanel(new BorderLayout(8, 8));
    bottom.add(inputScroll, BorderLayout.CENTER);
    bottom.add(sendBtn, BorderLayout.EAST);

    JPanel right = new JPanel(new BorderLayout(8, 8));
    right.add(chatScroll, BorderLayout.CENTER);
    right.add(bottom, BorderLayout.SOUTH);

    setLayout(new BorderLayout(8, 8));
    add(usersScroll, BorderLayout.WEST);
    add(right, BorderLayout.CENTER);

    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setSize(700, 450);
    setLocationRelativeTo(null);
  }

  public void onUserSelected(javax.swing.event.ListSelectionListener l) {
	  usersList.addListSelectionListener(l);
	}
  
  public void setChatText(String text) {
	  chatArea.setText(text);
	}

  public void setUsers(List<String> nicks) {
    usersModel.clear();
    for (String n : nicks) usersModel.addElement(n);
  }

  public String getSelectedUser() {
    return usersList.getSelectedValue();
  }

  public String getInputTextAndClear() {
    String t = inputArea.getText();
    inputArea.setText("");
    return t;
  }

  public void appendLine(String line) {
    chatArea.append(line + "\n");
  }

  public void onSend(ActionListener al) {
    sendBtn.addActionListener(al);
  }
}
