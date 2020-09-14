package name.julatec.ekonomi.extract.command;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseCommand<T extends BaseCommand<T> > implements Runnable {

    @Autowired
    protected CommandFactory commandFactory;

    protected final Context<?> context;

    protected BaseCommand(Context<?> context) {
        this.context = context;
    }

    protected Logger getLogger() {
        return context.logger;
    }

}
