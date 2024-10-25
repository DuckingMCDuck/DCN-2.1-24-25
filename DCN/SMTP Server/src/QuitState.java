class QuitState implements State {
    @Override
    public State handleInput(String input, SMTPSession session) {
        if (input.toUpperCase().equals("QUIT")) {
            session.sendResponse("221 Goodbye");
            session.close();
            return null;
        }
        return new InitialState(); // Reset to initial state for new email
    }

    @Override
    public String getPrompt() {
        return "";
    }
}