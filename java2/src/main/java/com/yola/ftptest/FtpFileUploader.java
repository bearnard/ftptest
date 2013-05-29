package com.yola.ftptest;

import java.util.concurrent.Callable;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.validator.GenericValidator;
import java.io.File;
import java.net.URL;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FtpFileUploader implements Callable<Boolean> {

  private String server = "foo";
  private String username = "ftptest";
  private String password = "s3cr3t";
  private String directory = "/upload/";
  private FTPClient ftpClient = null;
  private File file = null;

  public FtpFileUploader(File f, URL uri) {
    this.server = uri.getHost();
    this.username = uri.getUserInfo().split(":")[0];
    this.password = uri.getUserInfo().split(":")[1];
    this.directory = uri.getPath();
    ftpClient = new FTPClient();
    file = f;
  }
  @Override
  public Boolean call() throws Exception {

    this.ftpClient.setControlEncoding("UTF-8");
    this.ftpClient.connect(server);
    this.ftpClient.setConnectTimeout(5000);
    this.ftpClient.enterLocalPassiveMode();
    this.ftpClient.login(username, password);
    this.ftpClient.setKeepAlive(true);
    this.ftpClient.enterLocalPassiveMode();
    this.ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
    //final FTPFile[] listedFiles = ftpClient.listFiles(directory);
    //System.out.println(listedFiles);
    //this.ftpClient.changeWorkingDirectory("/upload");
    InputStream input = new FileInputStream(this.file);
    //this.ftpClient.storeFile(directory + this.file.getName(), input);
    //System.out.println("UPLOADING FILE " + this.file.getPath() + " : " + this.ftpClient.getReplyString());
    this.ftpClient.storeFile(directory + "/" + this.file.getPath(), input);
    input.close();
    ftpClient.disconnect();
    return true;
  }

} 
