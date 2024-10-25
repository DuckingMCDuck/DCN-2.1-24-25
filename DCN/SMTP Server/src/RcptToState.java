class RcptToState implements State {
    @Override
    public State handleInput(String input, SMTPSession session) {
        if (input.toUpperCase().startsWith("RCPT TO:")) {
            String recipient = input.substring(8).trim();
            session.addRecipient(recipient);
            session.sendResponse("250 Recipient " + recipient + " OK");
            return new DataState();
        }
        session.sendResponse("500 Error: command not recognized");
        return this;
    }

    @Override
    public String getPrompt() {
        return "";
    }
}