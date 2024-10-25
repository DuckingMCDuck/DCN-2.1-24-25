class MessageState implements State {
    private StringBuilder messageContent = new StringBuilder();

    @Override
    public State handleInput(String input, SMTPSession session) {
        if (input.equals(".")) {
            // Message is complete
            session.setMessageContent(messageContent.toString());
            System.out.println("\nReceived Email:");
            System.out.println("From: " + session.getSender());
            System.out.println("To: " + String.join(", ", session.getRecipients()));
            System.out.println("Content:\n" + messageContent.toString());
            session.sendResponse("250 Message accepted for delivery");
            return new QuitState();
        }
        messageContent.append(input).append("\n");
        return this;
    }

    @Override
    public String getPrompt() {
        return "";
    }
}