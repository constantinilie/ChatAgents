package usv.server;

import jade.lang.acl.ACLMessage;
import usv.storage.UserStore;

import java.util.Map;

public class AuthService {

  private final UserStore store;
  private final OnlineRegistry online;
  private final ServerResponder out;

  public AuthService(UserStore store, OnlineRegistry online, ServerResponder out) {
    this.store = store;
    this.online = online;
    this.out = out;
  }

  public void handleAuth(ACLMessage msg, Map<String, String> kv) {
    String op = safe(kv.get("op"));
    String user = safe(kv.get("user"));
    String pass = safe(kv.get("pass"));

    if (user.isEmpty() || pass.isEmpty() || op.isEmpty()) {
      out.replyAuthFail(msg, "missing_fields");
      return;
    }

    if (user.contains(":") || user.contains("\n") || user.contains("\r")) {
      out.replyAuthFail(msg, "invalid_username");
      return;
    }

    if (pass.contains(":") || pass.contains("\n") || pass.contains("\r")) {
      out.replyAuthFail(msg, "invalid_password");
      return;
    }

    if ("login".equalsIgnoreCase(op)) {
      String stored = store.getPassword(user);
      if (stored == null) {
        out.replyAuthFail(msg, "no_such_user");
        return;
      }
      if (!stored.equals(pass)) {
        out.replyAuthFail(msg, "bad_credentials");
        return;
      }

      online.putOnline(user, msg.getSender());
      out.replyAuthOk(msg, user);
      out.broadcastUserList(online);
      return;
    }

    if ("register".equalsIgnoreCase(op)) {
      if (store.exists(user)) {
        out.replyAuthFail(msg, "user_exists");
        return;
      }

      store.put(user, pass);
      store.appendToFile(user, pass);

      online.putOnline(user, msg.getSender());
      out.replyAuthOk(msg, user);
      out.broadcastUserList(online);
      return;
    }

    out.replyAuthFail(msg, "unknown_op");
  }

  private String safe(String s) {
    return s == null ? "" : s.trim();
  }
}
