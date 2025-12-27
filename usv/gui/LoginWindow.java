package usv.gui;

import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

import javax.swing.*;
import java.awt.*;

public class LoginWindow extends JFrame {
  private static final long serialVersionUID = 1L;

  private final AgentContainer container;

  private final JTextField userField = new JTextField(15);
  private final JPasswordField passField = new JPasswordField(15);
  private final JButton loginBtn = new JButton("Login");
  private final JButton regBtn = new JButton("Register");

  private int counter = 0;

  public LoginWindow(AgentContainer c) {
    super("Login");
    this.container = c;

    JPanel p = new JPanel(new GridLayout(3, 2, 8, 8));
    p.add(new JLabel("User:"));
    p.add(userField);
    p.add(new JLabel("Password:"));
    p.add(passField);
    p.add(loginBtn);
    p.add(regBtn);

    setContentPane(p);
    pack();
    setLocationRelativeTo(null);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    loginBtn.addActionListener(e -> spawnClient("login"));
    regBtn.addActionListener(e -> spawnClient("register"));
  }

  private void spawnClient(String op) {
    String user = userField.getText().trim();
    String pass = new String(passField.getPassword());

    if (user.isEmpty() || pass.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Fill user and password");
      return;
    }

    try {
      String agentName = "client-" + user + "-" + (counter++);
      AgentController a = container.createNewAgent(
          agentName,
          "usv.agents.ChatClientAgent",
          new Object[] { user, pass, op }
      );
      a.start();
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, "Could not start agent: " + ex.getMessage());
    }
  }
}
