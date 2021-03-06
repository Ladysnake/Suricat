package ladysnake;

//mport ladysnake.models.*;
import ladysnake.helpers.events.I_Observer;
import ladysnake.helpers.log.Logger;
import ladysnake.models.ModelsManager;
import ladysnake.views.*;
import ladysnake.controllers.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "WeakerAccess"})
public class App {
    ////////////////////////////////////////////////////////////////////////////////////////////
    ////Properties
    ////////////////////////////////////////////////////////////////////////////////////////////
    protected ControllersManager cm;
    protected ModelsManager mm;
    protected ViewsManager vm;

    ////////////////////////////////////////////////////////////////////////////////////////////
    ////Constructors
    ////////////////////////////////////////////////////////////////////////////////////////////
    public App() throws IOException, UnsupportedLookAndFeelException, FontFormatException {
        SharedMenuBar.retrieveJMenuBar(); //First build of the menu
        ExecutionMenuBar.getMenuBar(); //First build of the menu
        Logger.bootstrap(); //First build of the Logger

        this.vm = new ViewsManager(App.TITLE, App.DIMENSION);
        this.vm.setMinimumSize(App.MIN_DIMENSION);
        this.vm.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.vm.setIconImage(Toolkit.getDefaultToolkit().getImage("icon.png"));

        this.mm = null;

        this.cm = new ControllersManager(this.vm, this.mm);
        A_View executionView = new ExecutionView(this.vm);
        A_View homeView = new HomeView(this.vm);

        this.vm
        .addView(App.HOME_VIEW_TAG, homeView)
        .addView(App.EXECUTION_VIEW_TAG, executionView)
        .setCurrentView(App.HOME_VIEW_TAG); //Not necessary -> default behavior

        this.cm.addController(App.EXECUTION_VIEW_TAG, new ExecutionController(executionView, this.cm))
        .addController(App.HOME_VIEW_TAG, new HomeController(homeView, this.cm));

        try {
            vm.setLookAndFeel(LookAndFeelHub.NIMBUS);
//            vm.setLookAndFeel(LookAndFeelHub.MATERIAL);
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
            out("Could not change the look and feel");
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    ////Methods
    ////////////////////////////////////////////////////////////////////////////////////////////
    public void run(){
        vm.display();
    }

    public ModelsManager getModelsManager() { return mm; }
    public ViewsManager getViewsManager() { return vm; }
    public ControllersManager getControllersManager() { return cm; }

    ////////////////////////////////////////////////////////////////////////////////////////////
    ////Class properties
    ////////////////////////////////////////////////////////////////////////////////////////////
    public final static String TITLE = "Suricat";
    public final static int INIT_WIDTH = 900;
    public final static int INIT_HEIGHT = 600;
    public final static int MIN_WIDTH = 821;
    public final static int MIN_HEIGHT = 511;
    public final static Dimension DIMENSION = new Dimension(INIT_WIDTH, INIT_HEIGHT);
    public final static Dimension MIN_DIMENSION = new Dimension(MIN_WIDTH, MIN_HEIGHT);

    public final static String EXECUTION_VIEW_TAG = "execution";
    public final static String HOME_VIEW_TAG = "home";

    public final static String ROBOTO_PATH = "/fonts/Roboto-Light.ttf";
    public final static String ROBOTO_MEDIUM_PATH = "/fonts/Roboto-Medium.ttf";

    ////////////////////////////////////////////////////////////////////////////////////////////
    ////Class methods
    ////////////////////////////////////////////////////////////////////////////////////////////
    protected static App make() throws IOException, UnsupportedLookAndFeelException, FontFormatException {
        return new App();
    }

    protected static void out(Object msg){ System.out.println(msg); }

    public static void main(String[] args) throws IOException, UnsupportedLookAndFeelException, FontFormatException {
        App app = App.make();
        List<String> options = Arrays.stream(args)
        .filter(arg -> arg.startsWith("-"))
        .collect(Collectors.toList());

        if(options.contains("-h") || options.contains("--help") || options.contains("-?")){
            StringBuilder sb = new StringBuilder();
            sb.append("suricat [-h] [-v] [-w] [-e] [input file path]\n");
            sb.append("\n");
            sb.append("-h (or --help, or -?) : Displays this helper message\n");
            sb.append("-v : enables verbose mode (logging to stdout)\n");
            sb.append("-w : displays warning messages (logging to stdout)\n");
            sb.append("-e : displays error messages (logging to stdout)\n");
            System.out.println(sb.toString());
            System.exit(0);
            return;
        }

        List<String> arguments = Arrays.stream(args)
        .filter(arg -> !options.contains(arg))
        .collect(Collectors.toList());

        I_Observer simpleLogObserver = (eventName, optArgs) -> {
            String argString =  Arrays.stream(optArgs)
            .map(o -> ((String) o))
            .collect(Collectors.joining(", "));
            System.out.println(eventName + ": " + argString);
        };

        if(options.contains("-e"))
            Logger.addListener(Logger.ERROR, simpleLogObserver);

        if(options.contains("-w"))
            Logger.addListener(Logger.WARNING, simpleLogObserver);

        if(options.contains("-v"))
            Logger.addListener(Logger.VERBOSE, simpleLogObserver);

        if(arguments.size() > 0){
            String filePath = arguments.get(0);
            File file = new File(filePath);
            HomeController homeController = ((HomeController) app.getControllersManager().getController(App.HOME_VIEW_TAG));

            if(!homeController.fileIsJson(file)){
                Logger.triggerEvent(Logger.ERROR, "<{  " + filePath + "  }>" + " is not a JSON file");
                System.exit(-1);
            }

            homeController.goExecution(file);
        }

        app.run();
    }
}
