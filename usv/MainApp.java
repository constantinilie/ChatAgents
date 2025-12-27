package usv;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import usv.gui.LoginWindow;

public class MainApp {

  public static void main(String[] args) throws Exception {
    Runtime rt = Runtime.instance();
    Profile p = new ProfileImpl();
    p.setParameter(Profile.GUI, "false"); // optional: JADE RMA window

    final AgentContainer main = rt.createMainContainer(p);

    AgentController server = main.createNewAgent(
        "server",
        "usv.agents.ChatServerAgent",
        new Object[] {}
    );
    server.start();

    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        new LoginWindow(main).setVisible(true);
      }
    });
  }
}
