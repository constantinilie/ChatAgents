# Multi-Agent Chat (JADE) + LLM Assistant (Pydantic + Gemini)

Acest proiect implementeaza un ecosistem de agenti de tip chat (server-client) folosind platforma **JADE (Java)**, cu interfata grafica (Swing), plus un al treilea agent de tip **asistent/executor** care foloseste un microserviciu **Python (FastAPI + Pydantic)** pentru operatii pe text (translate / correct / rephrase) prin **Gemini API**.

## Cerinta acoperita
- Ecosistem de **chat agents** cu interfata grafica:
  - selectia unui destinatar (nickname)
  - trimitere mesaje
  - receptie si afisare mesaje
- Implementare **centralizata** (server-client)
- Descoperire agenti prin **DF** (Directory Facilitator):
  - clientii descopera `chat-server`
  - clientii descopera `llm-service`
- Salvarea istoricului conversatiilor in fisiere text externe + incarcare la repornire
- Agent asistent/executor bazat pe Pydantic (microserviciu Python)

## Arhitectura (3 agenti)
1) **ChatServerAgent**
   - autentificare (login/register)
   - mentine lista de utilizatori online
   - ruteaza mesajele intre clienti

2) **ChatClientAgent**
   - interfata grafica pentru chat
   - listeaza utilizatorii online
   - gestioneaza conversatiile si logurile local
   - trimite cereri catre LLM agent

3) **ChatLlmAgent** (agent asistent/executor)
   - se inregistreaza in DF cu service type `llm-service`
   - primeste cereri `LLM_REQUEST` de la clienti
   - apeleaza microserviciul Python (FastAPI + Pydantic)
   - intoarce rezultatul catre client (`LLM_RESPONSE`)

## Persistenta conversatiilor
- Conversatiile sunt salvate in folderul `chat_logs/` pe utilizator:
  - exemplu: `chat_logs/ana/andrei.txt`
- La repornirea aplicatiei, istoricul se incarca automat si este afisat cand selectezi utilizatorul din lista.

---

# Pornire rapida

## Cerinte:
- Java instalat si disponibil in PATH (`java -version`)
- Python 3 instalat si disponibil in PATH (`python --version` sau `py -3 --version`)

## **Pornire proiect**
- se deschide fisierul `runWindows.bat`
- se adauga o cheie API Gemini cand apare prompt-ul `Introduceti cheia GEMINI_API_KEY` 
(script-ul .bat verifica daca e java si python instalat, creeaza mediul virtual, instaleaza dependentele, creeaza .env cu cheia oferita, porneste serverul si aplicatia java)

