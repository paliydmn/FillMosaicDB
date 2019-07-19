package sample;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

class MyLogger {

    private final TextArea tArea_logs;

    MyLogger(TextArea tArea_logs) {
        this.tArea_logs = tArea_logs;
    }

    void printLine(String str) {
        Runnable task = () -> runPrintTask(str);
        Thread backgroundThread = new Thread(task);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    private void runPrintTask(String str) {
        // Update the Label on the JavaFx Application Thread
        Platform.runLater(() -> {
            Date calendar = Calendar.getInstance().getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:S");
            String log = sdf.format(calendar) + "\t" + str + "\n";
            tArea_logs.appendText(log);
        });

    }

        /*Thread thread = new Thread(() -> {
            Platform.runLater(() -> tArea_logs.appendText(str));
        });
        thread.run();
    }*/
}
