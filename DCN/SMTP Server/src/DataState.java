class DataState implements State {
    @Override
    public State handleInput(String input, SMTPSession session) {
        if (input.toUpperCase().equals("DATA")) {
            session.sendResponse("354 Start mail input; end with <CRLF>.<CRLF>");
            return new MessageState();
        }
        session.sendResponse("500 Error: command not recognized");
        return this;
    }

    @Override
    public String getPrompt() {
        return "";
    }
}
