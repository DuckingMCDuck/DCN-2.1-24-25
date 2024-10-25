package nl.avans.smtpstatemachine;

import nl.avans.SmtpContext;

public class ReceivingDataState implements SmtpStateInf {
    SmtpContext context;

    public ReceivingDataState(SmtpContext context) {
        this.context = context;
    }

    @Override
    public void Handle(String data) {
        if (data.equals(".")) {
            context.SendData("250 Ok: Message accepted for delivery");
            context.SetNewState(new WaitForMailFromState(context));
            return;
        }

        // Handle dot-stuffing: lines starting with a dot should have the dot removed
        String lineToAdd = data.startsWith(".") ? data.substring(1) : data;
        context.AddTextToBody(lineToAdd + "\n");
    }
}