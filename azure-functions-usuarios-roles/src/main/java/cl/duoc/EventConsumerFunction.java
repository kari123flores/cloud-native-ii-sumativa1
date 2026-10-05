package cl.duoc;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

public class EventConsumerFunction {

    @FunctionName("EventConsumer")
    public void recibirEvento(
            @EventGridTrigger(
                    name = "evento")
            String evento,
            final ExecutionContext context) {

        context.getLogger().info("Evento recibido desde Azure Event Grid:");

        context.getLogger().info(evento);
    }
}