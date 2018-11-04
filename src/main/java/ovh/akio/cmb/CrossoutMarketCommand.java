package ovh.akio.cmb;

import ovh.akio.cmb.commands.*;
import ovh.akio.cmb.impl.command.CommandManager;

public class CrossoutMarketCommand extends CommandManager {

    public CrossoutMarketCommand(CrossoutMarketBot bot, String prefix) {
        super(bot, prefix);

        this.registerCommand(new ItemCommand(bot));
        this.registerCommand(new SupportCommand(bot));
        this.registerCommand(new ChangelogCommand(bot));
        this.registerCommand(new WatchCommand(bot));
        this.registerCommand(new UnwatchCommand(bot));
        this.registerCommand(new WatchListCommand(bot));
        this.registerCommand(new StatsCommand(bot));
        this.registerCommand(new HelpCommand(bot));

    }

}
