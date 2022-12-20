package cloud.terium.cloudsystem.console;

import cloud.terium.cloudsystem.TeriumCloud;
import cloud.terium.cloudsystem.utils.logger.Logger;
import cloud.terium.teriumapi.console.LogType;
import cloud.terium.teriumapi.console.command.Command;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.AggregateCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.util.Arrays;

@Getter
public class ConsoleManager {

    private final Terminal terminal;
    private final String username;
    private LineReader lineReader;
    private CommandManager commandManager;
    private AggregateCompleter completer;
    private Thread thread;

    @SneakyThrows
    public ConsoleManager(CommandManager commandManager) {
        this.terminal = TerminalBuilder.builder()
                .name("terium-console")
                .system(true)
                .nativeSignals(true)
                .signalHandler(Terminal.SignalHandler.SIG_IGN)
                .build();
        this.username = username();
        this.commandManager = commandManager;

        readConsole();
    }

    public void readConsole() {
        completer = new AggregateCompleter(commandManager.getBuildedCommands().values());

        this.lineReader = LineReaderBuilder.builder()
                .appName("terium-console")
                .terminal(terminal)
                .completer(completer)
                .build();

        this.thread = new Thread(() -> {
            while (true) {
                String input = null;
                Command command;
                if (TeriumCloud.getTerium().getCloudUtils().isRunning()) {
                    try {
                        input = lineReader.readLine("\u001B[36m" + username + "\u001B[0m@terium => ");
                    } catch (EndOfFileException exception) {
                        input = lineReader.readLine("");
                    }
                } else {
                    input = lineReader.readLine("");
                }

                try {
                    if ((command = commandManager.getCommand(input.split(" ")[0])) != null || (command = commandManager.getCommandByAlias(input.split(" ")[0])) != null) {
                        final String[] args = input.split(" ");
                        command.execute(Arrays.copyOfRange(args, 1, args.length));
                    } else {
                        Logger.log("This command doesn't exist!", LogType.ERROR);
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                    Logger.log("terium-cloud found a error: " + exception.getMessage(), LogType.ERROR);
                }
            }
        });
        this.thread.start();
    }

    @SneakyThrows
    private String username() {
        return System.getProperty("user.name");
    }

    public void updateCompleter() {
        this.completer = new AggregateCompleter(commandManager.getBuildedCommands().values());
    }
}