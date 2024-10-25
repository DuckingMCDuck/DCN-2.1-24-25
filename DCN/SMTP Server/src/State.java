interface State {
    State handleInput(String input, SMTPSession session);
    String getPrompt();
}