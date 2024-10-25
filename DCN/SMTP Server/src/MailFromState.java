class MailFromState implements State {
    @Override
    public State handleInput(String input, SMTPSession session) {
        if (input.toUpperCase().startsWith("MAIL FROM:")) {
            String sender = input.substring(10).trim();
            session.setSender(sender);
            session.sendResponse("250 Sender " + sender + " OK");
            return new RcptToState();
        }
        session.sendResponse("500 Error: command not recognized");
        return this;
    }

    @Override
    public String getPrompt() {
        return "";
    }
}