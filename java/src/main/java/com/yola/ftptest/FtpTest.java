package com.yola.ftptest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.io.File;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;

public class FtpTest {

  public static void main(String[] args) {

    int concurrency = 5;

    if(args.length < 2) {
        System.err.println("Usage: ftptest <concurrency> <path>");
        System.exit(1);
    }
    try {
        concurrency = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
        System.err.println("Argument" + " must be an integer");
        System.exit(1);
    }

    String path = args[1];
    Collection<File> listFile = FileUtils.listFilesAndDirs(new File(path), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
    ExecutorService fileExecutor = Executors.newFixedThreadPool(concurrency);
    ExecutorService dirExecutor = Executors.newFixedThreadPool(concurrency);
    List<Future<Boolean>> fileList = new ArrayList<Future<Boolean>>();
    List<Future<File[]>> dirList = new ArrayList<Future<File[]>>();
    for (File f : listFile) {
      if(f.isDirectory()) {
          /*File[] filesList = f.listFiles();
          for (File g : filesList) {
              if(!g.isDirectory()) {
                  Callable<Boolean> file_worker = new FtpFileUploader(g);
                  Future<Boolean> submit_file = fileExecutor.submit(file_worker);
                  list.add(submit_file);
                  System.out.println("UPLOAD: " + g.getPath());
              }
          }*/
          Callable<File[]> dir_worker = new FtpDirMaker(f);
          Future<File[]> submit_dir = dirExecutor.submit(dir_worker);
          System.out.println("MKDIR: " + f.getPath());
          dirList.add(submit_dir);
      }
      else {
          //Callable<Boolean> worker = new FtpDirMaker(f);
          //Future<Boolean> submit_dir = dirExecutor.submit(worker);
          //System.out.println("MKDIR: " + f.getPath());
          /*try {
            //submit_dir.get();
            System.out.println("MKDIR: " + f.getPath());
          } catch (InterruptedException e) {
            e.printStackTrace();
          } catch (ExecutionException e) {
            e.printStackTrace();
          }*/
        
      }
    }
    //File[] toUpload = walkin(new File("tmp"));
    List<File[]> dirResults = new ArrayList<File[]>();
    // Now retrieve the result
    for (Future<File[]> future : dirList) {
      try {
        File[] filesToUpload = future.get();
        dirResults.add(filesToUpload);
        for (File g : filesToUpload) {
            if(!g.isDirectory()) {
                Callable<Boolean> file_worker = new FtpFileUploader(g);
                Future<Boolean> submit_file = fileExecutor.submit(file_worker);
                fileList.add(submit_file);
                System.out.println("UPLOAD: " + g.getPath());
            }
        }

      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
    }
    List<Boolean> fileResults = new ArrayList<Boolean>();
    // Now retrieve the result
    for (Future<Boolean> future : fileList) {
      try {
        System.out.println(future);
        fileResults.add(future.get());
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
    }
    dirExecutor.shutdown();
    fileExecutor.shutdown();
  }


} 
