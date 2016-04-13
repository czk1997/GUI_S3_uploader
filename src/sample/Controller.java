package sample;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.elasticmapreduce.model.Application;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class Controller extends Application implements Initializable {
    @FXML
    private TextArea infoArea = new TextArea();
    @FXML
    private TextField infoField = new TextField();
    @FXML
    private Button submit = new Button();
    private File file;
    @FXML
    private ProgressBar progressBar = new ProgressBar();
    @FXML
    private Button StopButton=new Button();
    public Thread thread=new Thread();
    @Override
    public void initialize(URL location, ResourceBundle resources) {


    }

    public void submitLinster() {
        try {
            upload(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void infoLinster() {
        FileChooser fileChooser = new FileChooser();
        file = fileChooser.showOpenDialog(new Stage());
        infoField.setText(file.getAbsolutePath());

    }


    public void upload(File file) throws IOException {
        final String bucketName = "xormoe";
        final String keyName = file.getName();
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    final AmazonS3[] s3client = {new AmazonS3Client(new ProfileCredentialsProvider())};
                    PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, keyName, file);
                    putObjectRequest.setGeneralProgressListener(new progressnow());
                    s3client[0].putObject(putObjectRequest);
                    infoArea.appendText("Update Successful" + "\n");
                    AccessControlList acl = new AccessControlList();
                    acl.grantAllPermissions();
                    infoArea.appendText("The link is:" + "\n");
                    infoArea.appendText("https://s3.amazonaws.com/xormoe/" + keyName + "\n");
                    infoArea.appendText("The Markdown LInk is : \n");
                    infoArea.appendText("[" + keyName + "](" + "https://s3.amazonaws.com/xormoe/" + keyName + ")");
                    String link = "[" + keyName + "](" + "https://s3.amazonaws.com/xormoe/" + keyName + ")";
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    Transferable tText = new StringSelection(link);
                    clipboard.setContents(tText, null);
                    infoArea.appendText("The link has already copy to your clipboard.");

                } catch (AmazonServiceException ase) {
                    infoArea.appendText("Caught an AmazonServiceException, which " +
                            "means your request made it " +
                            "to Amazon S3, but was rejected with an error response" +
                            " for some reason.");
                    infoArea.appendText("Error Message:    " + ase.getMessage() + "\n");

                    infoArea.appendText("HTTP Status Code: " + ase.getStatusCode() + "\n");
                    infoArea.appendText("AWS Error Code:   " + ase.getErrorCode() + "\n");
                    infoArea.appendText("Error Type:       " + ase.getErrorType() + "\n");
                    infoArea.appendText("Request ID:       " + ase.getRequestId() + "\n");
                } catch (AmazonClientException ace) {
                    infoField.appendText("Caught an AmazonClientException, which " +
                            "means the client encountered " +
                            "an internal error while trying to " +
                            "communicate with S3, " +
                            "such as not being able to access the network.");
                    infoField.appendText("Error Message: " + ace.getMessage() + "\n");
                }
                return null;
            }
        };
        //progressBar.progressProperty().bind(task.progressProperty());
        thread=new Thread(task);
        thread.start();


    }
    public void stopButttonLinister(){
        if(thread.isAlive()){
            thread.stop();
        }
    }


    class progressnow implements ProgressListener {
        final long[] sum = {0};
        final long[] total = {0};
        final double[] percent = {0};


        @Override
        public void progressChanged(ProgressEvent progressEvent) {
            sum[0] = sum[0] + progressEvent.getBytesTransferred();
            //System.out.println(sum[0]);
            if (progressEvent.getBytes() > total[0]) {
                total[0] = progressEvent.getBytes();
            }
            //System.out.println(total[0]);
            //System.out.println(sum[0]);
            percent[0] = (double) sum[0] / (double) total[0];
            //System.out.println(percent[0]);
            update(percent[0]);
        }

        public void update(double percent) {
            System.out.println(percent);
            progressBar.setProgress(percent);

        }
    }
}

