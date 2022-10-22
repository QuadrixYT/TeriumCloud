package cloud.terium.cloudsystem;

import cloud.terium.cloudsystem.manager.*;
import cloud.terium.cloudsystem.module.ModuleManager;
import cloud.terium.cloudsystem.networking.DefaultTeriumNetworking;
import cloud.terium.cloudsystem.service.ServiceManager;
import cloud.terium.cloudsystem.service.group.ServiceGroupManager;
import cloud.terium.cloudsystem.template.TemplateManager;
import cloud.terium.cloudsystem.utils.CloudUtils;
import cloud.terium.teriumapi.console.LogType;
import cloud.terium.cloudsystem.utils.logger.Logger;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import sun.misc.Signal;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class Terium {

    private static Terium terium;
    private final CloudUtils cloudUtils;
    private ConfigManager configManager;
    private final CommandManager commandManager;
    private final ConsoleManager consoleManager;
    private final ScreenManager screenManager;
    private final ServiceManager serviceManager;
    private final ServiceGroupManager serviceGroupManager;
    private final ModuleManager moduleManager;
    private final DefaultTeriumNetworking defaultTeriumNetworking;

    public static void main(String[] args) {
        new Terium();
    }

    public Terium() {
        // System.setProperty("org.jline.terminal.dumb", "true");

        terium = this;
        this.cloudUtils = new CloudUtils();
        this.configManager = new ConfigManager();

        this.cloudUtils.checkLicense();
        Logger.log(cloudUtils.getStartMessage());

        if (cloudUtils.getSetupState() == null) {
            Logger.log(("[" + DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()) + "\u001B[0m] " + LogType.INFO.getPrefix() + "Trying to start Terium..."));

            this.commandManager = new CommandManager();
            this.consoleManager = new ConsoleManager(commandManager);
            this.screenManager = new ScreenManager();
            this.serviceManager = new ServiceManager();
            this.serviceGroupManager = new ServiceGroupManager();
            this.moduleManager = new ModuleManager();
            this.defaultTeriumNetworking = new DefaultTeriumNetworking(configManager);

            new TemplateManager();
            Signal.handle(new Signal("INT"), signal -> {
                cloudUtils.setRunning(false);
                cloudUtils.shutdownCloud();
            });

            Logger.log("Successfully started Terium.", LogType.INFO);
            serviceManager.startServiceCheck();
            return;
        }

        this.consoleManager = null;
        this.commandManager = null;
        this.moduleManager = null;
        this.serviceManager = null;
        this.serviceGroupManager = new ServiceGroupManager();
        this.screenManager = null;
        this.defaultTeriumNetworking = null;

        new SetupManager();

        Signal.handle(new Signal("INT"), signal -> {
            Logger.log("Trying to stop the cloud...", LogType.SETUP);
            try {
                FileUtils.forceDelete(new File("config.json"));
                Thread.sleep(1000);
            } catch (Exception ignored) {
            }
            System.exit(0);
        });
    }

    public static Terium getTerium() {
        return terium;
    }

    public String getVersion() {
        return "1.0-SNAPSHOT";
    }
}