package com.yola.ftptest;

import java.util.concurrent.Callable;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.validator.GenericValidator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;

public class FtpDirMaker implements Callable<File[]> {

  private static final String server = "foo";
  private static final String username = "ftptest";
  private static final String password = "s3cr3t";
  private static final String directory = "/upload/";
  private FTPClient ftpClient = null;
  private File dir = null;

  public FtpDirMaker(File dir) {
    this.ftpClient = new FTPClient();
    this.dir = dir;
  }
  @Override
  public File[] call() throws Exception {

    this.ftpClient.setControlEncoding("UTF-8");
    this.ftpClient.connect(server);
    this.ftpClient.setConnectTimeout(5000);
    this.ftpClient.enterLocalPassiveMode();
    this.ftpClient.login(username, password);
    this.ftpClient.setKeepAlive(true);
    this.ftpClient.enterLocalPassiveMode();
    this.ftpClient.makeDirectory("upload/" + this.dir.getPath());
    this.ftpClient.disconnect();
    File[] filesList = this.dir.listFiles();
    return filesList;
    }

} 
