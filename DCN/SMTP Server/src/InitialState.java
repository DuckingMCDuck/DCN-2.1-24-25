class InitialState implements State {
    @Override
    public State handleInput(String input, SMTPSession session) {
        if (input.toUpperCase().startsWith("HELO") || input.toUpperCase().startsWith("EHLO")) {
            session.sendResponse("250 Hello " + input.substring(5).trim() + ", pleased to meet you");
            return new MailFromState();
        }
        session.sendResponse("500 Error: command not recognized");
        return this;
    }

    @Override
    public String getPrompt() {
        return "220 Simple SMTP Server Ready";
    }
}
