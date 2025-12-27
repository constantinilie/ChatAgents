package usv.messages;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MsgCodec {

  private MsgCodec() {}

  // Format: TYPE|k=v&k2=v2 (URL-encoded values)
  public static String pack(String type, Map<String, String> kv) {
    StringBuilder sb = new StringBuilder();
    sb.append(type).append("|");
    boolean first = true;
    for (Map.Entry<String, String> e : kv.entrySet()) {
      if (!first) sb.append("&");
      first = false;
      sb.append(url(e.getKey())).append("=").append(url(e.getValue()));
    }
    return sb.toString();
  }

  public static Parsed unpack(String content) {
    if (content == null) return new Parsed("UNKNOWN", Collections.<String, String>emptyMap());
    int idx = content.indexOf('|');
    if (idx < 0) return new Parsed(content.trim(), Collections.<String, String>emptyMap());

    String type = content.substring(0, idx).trim();
    String qs = content.substring(idx + 1);

    Map<String, String> kv = new LinkedHashMap<String, String>();
    if (qs != null && !qs.trim().isEmpty()) {
      String[] parts = qs.split("&");
      for (String p : parts) {
        if (p == null || p.trim().isEmpty()) continue;
        int eq = p.indexOf('=');
        if (eq < 0) {
          kv.put(unurl(p), "");
        } else {
          String k = unurl(p.substring(0, eq));
          String v = unurl(p.substring(eq + 1));
          kv.put(k, v);
        }
      }
    }
    return new Parsed(type, kv);
  }

  private static String url(String s) {
	  try {
	    return URLEncoder.encode(s == null ? "" : s, "UTF-8");
	  } catch (Exception e) {
	    return "";
	  }
	}

	private static String unurl(String s) {
	  try {
	    return URLDecoder.decode(s == null ? "" : s, "UTF-8");
	  } catch (Exception e) {
	    return "";
	  }
	}


  public static final class Parsed {
    private final String type;
    private final Map<String, String> kv;

    public Parsed(String type, Map<String, String> kv) {
      this.type = type;
      this.kv = kv;
    }

    public String type() { return type; }
    public Map<String, String> kv() { return kv; }
  }
}
