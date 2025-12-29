package usv.llm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LlmHttpClient {

  private final String baseUrl; // e.g. http://127.0.0.1:8000

  public LlmHttpClient(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  // Minimal call used by your project:
  // op: "correct" | "rephrase" | "translate"
  public String transform(String op, String text, String sourceLang, String targetLang) throws IOException {
    // default params; you can expose them if you want
    return transform(op, text, sourceLang, targetLang, 0.2, 1024);
  }

  public String transform(String op, String text, String sourceLang, String targetLang,
                          double temperature, int maxOutputTokens) throws IOException {

    String endpoint = baseUrl + "/v1/transform";
    HttpURLConnection con = (HttpURLConnection) new URL(endpoint).openConnection();
    con.setRequestMethod("POST");
    con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
    con.setDoOutput(true);
    con.setConnectTimeout(10_000);
    con.setReadTimeout(90_000);

    String json = buildJson(op, text, sourceLang, targetLang, temperature, maxOutputTokens);

    OutputStream os = null;
    try {
      os = con.getOutputStream();
      os.write(json.getBytes("UTF-8"));
      os.flush();
    } finally {
      if (os != null) try { os.close(); } catch (Exception ignored) {}
    }

    int code = con.getResponseCode();
    InputStream is = (code >= 200 && code < 300) ? con.getInputStream() : con.getErrorStream();
    String body = readAll(is);

    if (code < 200 || code >= 300) {
      throw new IOException("HTTP " + code + ": " + body);
    }

    return extractResult(body);
  }

  private String buildJson(String op, String text, String sourceLang, String targetLang,
                           double temperature, int maxOutputTokens) {

    StringBuilder sb = new StringBuilder();
    sb.append("{");
    sb.append("\"operation\":\"").append(escape(op)).append("\",");
    sb.append("\"text\":\"").append(escape(text)).append("\",");
    sb.append("\"temperature\":").append(temperature).append(",");
    sb.append("\"max_output_tokens\":").append(maxOutputTokens);

    if (sourceLang != null && !sourceLang.trim().isEmpty()) {
      sb.append(",\"source_lang\":\"").append(escape(sourceLang.trim())).append("\"");
    }
    if (targetLang != null && !targetLang.trim().isEmpty()) {
      sb.append(",\"target_lang\":\"").append(escape(targetLang.trim())).append("\"");
    }

    sb.append("}");
    return sb.toString();
  }

  private String readAll(InputStream is) throws IOException {
    if (is == null) return "";
    BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = br.readLine()) != null) sb.append(line);
    return sb.toString();
  }

  // Extract {"result":"..."} from JSON
  // This is minimal and assumes response follows your FastAPI schema.
  private String extractResult(String json) throws IOException {
    if (json == null) return "";

    String key = "\"result\"";
    int k = json.indexOf(key);
    if (k < 0) {
      // if server returns something else, keep raw
      return json;
    }

    int colon = json.indexOf(':', k);
    if (colon < 0) return json;

    int firstQuote = json.indexOf('"', colon + 1);
    if (firstQuote < 0) return json;

    int endQuote = findStringEnd(json, firstQuote + 1);
    if (endQuote < 0) return json;

    String raw = json.substring(firstQuote + 1, endQuote);
    return unescape(raw);
  }

  private int findStringEnd(String s, int start) {
    boolean esc = false;
    for (int i = start; i < s.length(); i++) {
      char c = s.charAt(i);
      if (esc) { esc = false; continue; }
      if (c == '\\') { esc = true; continue; }
      if (c == '"') return i;
    }
    return -1;
  }

  private String escape(String s) {
    if (s == null) return "";
    String out = s;
    out = out.replace("\\", "\\\\");
    out = out.replace("\"", "\\\"");
    out = out.replace("\n", "\\n");
    out = out.replace("\r", "\\r");
    out = out.replace("\t", "\\t");
    return out;
  }

  private String unescape(String s) {
    if (s == null) return "";
    String out = s;
    out = out.replace("\\n", "\n");
    out = out.replace("\\r", "\r");
    out = out.replace("\\t", "\t");
    out = out.replace("\\\"", "\"");
    out = out.replace("\\\\", "\\");
    return out;
  }
}
