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

  // LLM buttons
  private final JButton undoBtn = new JButton("Undo");
  private final JButton translateBtn = new JButton("Translate");
  private final JButton correctBtn = new JButton("Correct");
  private final JButton rephraseBtn = new JButton("Rephrase");
  private final JComboBox<String> targetLangCombo =
		    new JComboBox<String>(new String[] {
		        "en - English",
		        "ro - Romanian",
		        "fr - French",
		        "de - German",
		        "es - Spanish",
		        "it - Italian"
		    });

  public ChatWindow(String nick) {
    super("Chat - " + nick);

    chatArea.setEditable(false);
    chatArea.setLineWrap(true);
    chatArea.setWrapStyleWord(true);

    inputArea.setLineWrap(true);
    inputArea.setWrapStyleWord(true);
    
    //Default -> English
    targetLangCombo.setSelectedIndex(0);

    JScrollPane usersScroll = new JScrollPane(usersList);
    usersScroll.setPreferredSize(new Dimension(160, 300));

    JScrollPane chatScroll = new JScrollPane(chatArea);
    JScrollPane inputScroll = new JScrollPane(inputArea);

 // Tools row (LLM actions on left, Undo on right)
    undoBtn.setEnabled(false);

    JPanel tools = new JPanel(new BorderLayout());

    // Left group (Translate/Correct/Rephrase)
    JPanel leftTools = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
    leftTools.add(translateBtn);
    leftTools.add(new JLabel("To:"));
    leftTools.add(targetLangCombo);
    leftTools.add(correctBtn);
    leftTools.add(rephraseBtn);

    // Put left group in center so it stays left, Undo in east
    tools.add(leftTools, BorderLayout.CENTER);
    tools.add(undoBtn, BorderLayout.EAST);


    // Input row (text + send)
    JPanel inputRow = new JPanel(new BorderLayout(8, 8));
    inputRow.add(inputScroll, BorderLayout.CENTER);
    inputRow.add(sendBtn, BorderLayout.EAST);

    // Bottom = tools + inputRow
    JPanel bottom = new JPanel(new BorderLayout(8, 8));
    bottom.add(tools, BorderLayout.NORTH);
    bottom.add(inputRow, BorderLayout.CENTER);

    JPanel right = new JPanel(new BorderLayout(8, 8));
    right.add(chatScroll, BorderLayout.CENTER);
    right.add(bottom, BorderLayout.SOUTH);

    setLayout(new BorderLayout(8, 8));
    add(usersScroll, BorderLayout.WEST);
    add(right, BorderLayout.CENTER);

    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setSize(700, 480);
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

  // Used by Send button
  public String getInputTextAndClear() {
    String t = inputArea.getText();
    inputArea.setText("");
    return t;
  }

  // Used by LLM buttons (does not clear)
  public String getInputText() {
    return inputArea.getText();
  }

  public void setInputText(String s) {
    inputArea.setText(s == null ? "" : s);
  }

  public void appendLine(String line) {
    chatArea.append(line + "\n");
  }

  public void onSend(ActionListener al) {
    sendBtn.addActionListener(al);
  }

  public void onUndo(ActionListener al) {
    undoBtn.addActionListener(al);
  }

  public void onTranslate(ActionListener al) {
    translateBtn.addActionListener(al);
  }

  public void onCorrect(ActionListener al) {
    correctBtn.addActionListener(al);
  }

  public void onRephrase(ActionListener al) {
    rephraseBtn.addActionListener(al);
  }

  public void setUndoEnabled(boolean enabled) {
    undoBtn.setEnabled(enabled);
  }
  
  public String getTranslateTargetLangCode() {
	  Object sel = targetLangCombo.getSelectedItem();
	  if (sel == null) return "en";
	  String s = String.valueOf(sel).trim();
	  int idx = s.indexOf(" - ");
	  if (idx > 0) return s.substring(0, idx).trim();
	  return s;
	}

}
