package sample;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Controller {

    final private FileChooser fileChooser = new FileChooser();
    final private DirectoryChooser directoryChooser = new DirectoryChooser();
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
                result = RecordedTVDAO.insertRecs(file);
                mLogger.printLine(file.getName() + " ---> " + result);
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
                "\nVersion 0.01" +
                "\nFor questions and propositions " +
                "\nplease write to paliydmn@gmail.com", ButtonType.CLOSE);
        alert.show();
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
