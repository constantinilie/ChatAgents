package usv;

public class Launcher {
  public static void main(String[] args) {
    jade.Boot.main(new String[] {
        "-gui",
        "server:usv.agents.ChatServerAgent;" +
        "ana:usv.agents.ChatClientAgent(ana);" +
        "andrei:usv.agents.ChatClientAgent(andrei);"+
        "mariusica:usv.agents.ChatClientAgent(mariusica)"
    });
  }
}
