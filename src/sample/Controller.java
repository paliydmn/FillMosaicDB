package sample;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Controller {

    final private FileChooser fileChooser = new FileChooser();
    final private DirectoryChooser directoryChooser = new DirectoryChooser();

    private static final String MEDIAINFOEXE = System.getProperty("user.dir") + "\\MediaInfo.exe";
    private static int DEFAULTDURATION = 0;

    @FXML
    public AnchorPane mainAnchor;
    @FXML
    public TextField tfRecPath;
    @FXML
    public TextField tfChooseDbFile;
    @FXML
    public TextArea taLogs;
    @FXML
    public Button btnChooseRecPath;
    @FXML
    public Button btnChooseDbFile;
    @FXML
    public Button btnAddtoDb;
    @FXML
    public Label lbStatus;
    public ProgressIndicator pbar;


    private List<File> fileList;
    private MyLogger mLogger;

    @FXML
    public void initialize() {
        mLogger = new MyLogger(taLogs);
        pbar.setVisible(false);
    }

    /*void copyRecs(File selectedFile){
        Path from = Paths.get(selectedFile.toURI());
        Path to = Paths.get("pathdest\\file.exe");
        CopyOption[] options = new CopyOption[]{
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.COPY_ATTRIBUTES
        };
        try {
            Files.copy(from, to, options);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    public void onChooseRecPath() {
       // calculateDuration();
        Stage stage = (Stage) btnAddtoDb.getScene().getWindow();
        File dir = directoryChooser.showDialog(stage);
        if (dir != null) {
            tfRecPath.setText(dir.getAbsolutePath());
        } else {
            tfRecPath.setText(null);
        }
        getAllFiles(dir);
    }

    private void getAllFiles(File curDir) {
        fileList = new ArrayList<>();
        File[] filesList = curDir.listFiles();
        assert filesList != null;
        for (File f : filesList) {
            if (f.isFile() && f.getName().endsWith(".ts")) {
                System.out.println(f.getName());
                mLogger.printLine(f.getName());
                fileList.add(f);
            }
        }
        if (fileList.size() == 0) {
            lbStatus.setText("There are no any TVMosaic recordings at this folder!");
        } else {
            lbStatus.setText("Found " + fileList.size() + " recordings");
        }
    }

    //get duration of video file in seconds
    int  calculateDuration(File file){

        Runtime rt = Runtime.getRuntime();
        String[] commands = {MEDIAINFOEXE, "--Inform=\"Video;%Duration%\"", file.getAbsolutePath()};
        System.out.println(MEDIAINFOEXE);
        Process proc = null;
        int duration = 0;
        try {
            proc = rt.exec(commands);

        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(proc.getInputStream()));

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(proc.getErrorStream()));

// Read the output from the command
        String s = null;
        while ((s = stdInput.readLine()) != null) {
            if (!s.equals(""))
                duration = Integer.valueOf(s)/1000;
        }

// Read any errors from the attempted command
        while ((s = stdError.readLine()) != null) {
            duration = 0;
        }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return duration;
    }

    public void onChooseDbFile() {
        Stage stage = (Stage) btnAddtoDb.getScene().getWindow();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("DB", "*.db"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            System.out.println(file);
            tfChooseDbFile.setText(file.getAbsolutePath());
            if (file.getName().equals("recorder_database.db")) {
                lbStatus.setText("Found recorder_database.db!");
            } else {
                lbStatus.setText("Please, choose the \"recorder_database.db\" file!");
            }
        }
    }

    public void onApply() {
        Task<Void> insertToDbTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                pbar.setVisible(true);
                startInsertTask();
                return null;
            }
        };

        String dbPath = tfChooseDbFile.getText();
        String recPath = tfRecPath.getText();
        if (dbPath == null || dbPath.equals("")) {
            lbStatus.setText("Please, choose the \"recorder_database.db\" file!");
        } else if (recPath == null || fileList == null || recPath.equals("")) {
            lbStatus.setText("Please, choose the recordings path!");
        } else {
            DBUtilSQLite.setDbPath(dbPath);
            new Thread(insertToDbTask).start();
            lbStatus.setText("Inserting ...");
        }
    }

    private void startInsertTask() {
        String result = "!";
        try {
            // RecordedTVDAO.insertRecs(fileList);
            for (File file : fileList) {
                if (file.getAbsolutePath().contains("'")) {
                    file = new File(file.getAbsolutePath().replace("'", ""));
                }
                //result = RecordedTVDAO.insertRecs(file);
                double duration = calculateDuration(file);
                if(duration != 0){
                    result = RecordedTVDAO.insertRecsWithDuration(file, duration);result = RecordedTVDAO.insertRecsWithDuration(file, duration);
                    mLogger.printLine(file.getName() + " ---> " + result);
                } else if (DEFAULTDURATION != 0){
                    result = RecordedTVDAO.insertRecsWithDuration(file, DEFAULTDURATION);
                    mLogger.printLine(file.getName() + " ---> Warning: Duration is 0 or not calculated, Default Duration is applied" );
                } else {
                    mLogger.printLine(file.getName() + " ---> Error: Duration is 0 or not calculated" );
                }

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        pbar.setVisible(false);
        String finalResult = result;
        Platform.runLater( () -> lbStatus.setText(finalResult));
    }


    public void onExit() {
        Platform.exit();
    }

    public void onClearAll() {
        taLogs.clear();
        tfRecPath.clear();
        tfChooseDbFile.clear();

        lbStatus.setText("Cleared!");
    }

    public void onShowAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Fill Mosaic database " +
                "\nVersion 0.02" +
                "\nFor questions and propositions " +
                "\nplease write to paliydmn@gmail.com"+
                "\n----------------------------------" +
                "\nDuration of ts files are calculated by MediaInfo" +
                "\nhttps://mediaarea.net/fr/MediaInfo",
                ButtonType.CLOSE);
        alert.show();
    }

    public void onSetDefDuration(ActionEvent actionEvent) {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(mainAnchor.getScene().getWindow());
        VBox dialogVbox = new VBox(20);
        dialogVbox.setPadding(new Insets(10, 10, 10, 10));
        dialogVbox.getChildren().add(new Label("Set Default Duration for corrupted TS files (min):"));
        TextField defDuration = new TextField("0");
        dialogVbox.getChildren().add(defDuration);
        Button ok = new Button("OK");
        ok.setAlignment(Pos.BOTTOM_RIGHT);

        dialogVbox.getChildren().add(ok);
        ok.onActionProperty().setValue(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                DEFAULTDURATION = Integer.parseInt(defDuration.getText());
                System.out.println(DEFAULTDURATION);
                if(DEFAULTDURATION == 0){
                    lbStatus.setText("Default duration is " + DEFAULTDURATION + " file will not be added");
                } else {
                    lbStatus.setText("Default duration is " + DEFAULTDURATION );
                }
                dialog.close();
            }
        });

        Scene dialogScene = new Scene(dialogVbox, 250, 120);
        dialog.setScene(dialogScene);
        dialog.show();

    }

/*
private void printLog(String str) {
        Runnable task = new Runnable() {
            public void run() {
                runPrintTask(str);
            }
        };
        Thread backgroundThread = new Thread(task);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }
private void runPrintTask(String str) {
        // Update the Label on the JavaFx Application Thread
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                taLogs.appendText(str + "\n");
            }
        });
    }*/
}
