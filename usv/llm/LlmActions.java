package usv.llm;

public interface LlmActions {
  // op: "correct", "rephrase", "translate"
  String run(String op, String inputText) throws Exception;
  String translate(String inputText, String targetLang) throws Exception;
}
