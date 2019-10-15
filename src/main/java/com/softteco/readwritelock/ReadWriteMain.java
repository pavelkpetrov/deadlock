package com.softteco.readwritelock;

import junit.framework.Assert;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

@Slf4j
public class ReadWriteMain {

    static boolean dataChanged = false;

    public static void main(String[] args) {
        MonitorImpl monitor = new MonitorImpl();

        // write your code here
        dataChanged = false;
        try {
            final File tempDir = FileUtils.createTempDirectory();
            final String fileName = "testFile.txt";
            final String testString = "test data 1";
            final String testString2 = "test data 2";
            FileUtils.createFileForMonitor(tempDir.getAbsolutePath(), testString.getBytes("UTF-8"), fileName);
            monitor.init(tempDir.getAbsolutePath());

            monitor.addNewFolderDataEventListener((FilesChangeMonitor.NewFolderDataEventListener) newData -> dataChanged = true);
            monitor.start();
            Assert.assertTrue("Monitor should be started", monitor.isStart());
            Assert.assertTrue(testString.equals(monitor.getData().values().iterator().next()));
            Thread changeTh = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(100);
                        FileUtils.createFileForMonitor(tempDir.getAbsolutePath(), testString2.getBytes("UTF-8"), fileName);
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            });
            changeTh.start();
            long currentTime = 0l;
            while(true){
                if (currentTime > 1000) {
                    log.info("Max attempts times exide");
                    break;
                }
                if (dataChanged) {
                    log.info("Got data change event");
                    break;
                }
                Thread.sleep(10);
                currentTime++;
            }
            if (dataChanged) {
                Assert.assertTrue(testString2.equals(monitor.getData().values().iterator().next()));
            } else {
                Assert.assertTrue("Data not changed in the monitor as expected", false);
            }
            monitor.stop();
            Assert.assertTrue("Monitor should not be started", !monitor.isStart());

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }



}
