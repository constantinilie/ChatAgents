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
import usv.llm.LlmActions;

public class ChatController {

  private final ChatWindow win;
  private final ConversationStore convs;
  private final ChatCallbacks cb;
  private final LlmActions llm;
  private String lastInputBeforeLlm = null;


  public ChatController(ChatWindow win, ConversationStore convs, ChatCallbacks cb, LlmActions llm) {
    this.win = win;
    this.convs = convs;
    this.cb = cb;
    this.llm = llm;
    
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
        // Reset undo when sending message
        lastInputBeforeLlm = null;
        win.setUndoEnabled(false);
        
        if (cb != null) cb.onSend(to, text);
      }
    });
    
 // Undo button
    win.onUndo(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (lastInputBeforeLlm == null) {
          win.setUndoEnabled(false);
          return;
        }
        win.setInputText(lastInputBeforeLlm);
        lastInputBeforeLlm = null;
        win.setUndoEnabled(false);
      }
    });

    // Correct
    win.onCorrect(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        runLlmAndReplaceInput("correct");
      }
    });

    // Rephrase
    win.onRephrase(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        runLlmAndReplaceInput("rephrase");
      }
    });

    // Translate
    win.onTranslate(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        runLlmAndReplaceInput("translate");
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
  
  private void runLlmAndReplaceInput(final String op) {
	  if (llm == null) {
	    appendLine("[system] LLM not configured.");
	    return;
	  }

	  final String current = win.getInputText();
	  if (current == null || current.trim().isEmpty()) return;

	  // Save for undo (single step)
	  lastInputBeforeLlm = current;
	  win.setUndoEnabled(true);

	  new Thread(new Runnable() {
	    public void run() {
	      try {
	        final String out;
	        if ("translate".equals(op)) {
	          String tgt = win.getTranslateTargetLangCode();
	          out = llm.translate(current, tgt);
	        } else {
	          out = llm.run(op, current);
	        }

	        SwingUtilities.invokeLater(new Runnable() {
	          public void run() { win.setInputText(out); }
	        });

	      } catch (final Exception ex) {
	        SwingUtilities.invokeLater(new Runnable() {
	          public void run() { appendLine("[system] LLM error: " + ex.getMessage()); }
	        });
	      }
	    }
	  }, "llm-" + op).start();
	}

  

}
