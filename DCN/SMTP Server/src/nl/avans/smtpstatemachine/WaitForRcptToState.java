package nl.avans.smtpstatemachine;

import nl.avans.SmtpContext;

public class WaitForRcptToState implements SmtpStateInf {
    SmtpContext context;

    public WaitForRcptToState(SmtpContext context) {
        this.context = context;
    }

    @Override
    public void Handle(String data) {
        if (data.toUpperCase().startsWith("RCPT TO:")) {
            String email = extractEmail(data);
            if (email != null) {
                context.AddRecipient(email);
                context.SendData("250 Ok");
                context.SetNewState(new WaitForRcptToOrDataState(context));
                return;
            }
            context.SendData("501 Syntax error in parameters or arguments");
            return;
        }
        if (data.toUpperCase().equals("QUIT")) {
            context.SendData("221 Bye");
            context.DisconnectSocket();
            return;
        }
        context.SendData("503 Bad sequence of commands");
    }

    private String extractEmail(String data) {
        int start = data.indexOf('<');
        int end = data.indexOf('>');
        if (start >= 0 && end > start) {
            return data.substring(start + 1, end);
        }
        return null;
    }
}