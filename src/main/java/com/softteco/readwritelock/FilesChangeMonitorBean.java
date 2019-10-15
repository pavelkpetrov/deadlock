package com.softteco.readwritelock;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public abstract class FilesChangeMonitorBean<T> implements FilesChangeMonitor<T> {

    private long interval = TimeUnit.SECONDS.toSeconds(10);

    private FileAlterationObserver observer;
    private FileAlterationMonitor monitor;
    private FileAlterationListener listener;
    private boolean started = false;
    private Lock readLock;
    private Lock writeLock;
    private String pathToMonitorInternal;
    protected List<NewFolderDataEventListener> listeners;

    private Map<String, T> data = new HashMap<String, T>();

    protected FileAlterationObserver createObserver(){
        FileAlterationObserver observer = new FileAlterationObserver(getPathToMonitorInternal());
        return observer;
    }

    protected FileAlterationMonitor createMonitor(){
        FileAlterationMonitor monitor = new FileAlterationMonitor(getInterval());
        return monitor;
    }

    protected FileAlterationListener createListener(){
        FileAlterationListener listener = new FileAlterationListenerAdaptor() {
            @Override
            public void onFileCreate(File file) {
                // code for processing creation event
                FilesChangeMonitorBean.this.onFileChange(file, FileChange.CREATED);
            }

            @Override
            public void onFileDelete(File file) {
                // code for processing deletion event
                FilesChangeMonitorBean.this.onFileChange(file, FileChange.DELETED);
            }

            @Override
            public void onFileChange(File file) {
                // code for processing change event
                FilesChangeMonitorBean.this.onFileChange(file, FileChange.CHANGED);
            }
        };
        return listener;
    }

    public long getInterval(){
        return interval;
    }

    public FileAlterationObserver getObserver() {
        return observer;
    }

    public void setObserver(FileAlterationObserver observer) {
        this.observer = observer;
    }

    public FileAlterationMonitor getMonitor() {
        return monitor;
    }

    public void setMonitor(FileAlterationMonitor monitor) {
        this.monitor = monitor;
    }

    public FileAlterationListener getListener() {
        return listener;
    }

    public void setListener(FileAlterationListener listener) {
        this.listener = listener;
    }

    protected Map<String, T> getDataNoLock(){
        return this.data;
    }

    protected void setDataNoLock(Map<String, T> data){
        this.data = data;
    }

    protected void initLocks(){
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
    }

    protected void onFileChange(File file, FileChange cdc) {
        log.info("Got file change event for a file: {}, action {}", file.getAbsolutePath(), cdc);
        Map<String, T>  dataMapCopy = readWithLock();
        Map<String, T>  actualDataMap = null;
        try {
            actualDataMap = getConvertedOnFileChange(file, cdc, dataMapCopy);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        writeWithLock(actualDataMap);
        fireNewFolderDataEvent(actualDataMap);
    }

    protected Map<String, T> getConvertedOnFileChange(File file, FileChange cdc, Map<String, T>  dataMapCopy) throws Exception{
        log.info("Got file change event for a file: {}, action {}", file.getAbsolutePath(), cdc);
        InputStream fileIs = null;
        try {
            fileIs = getActualFileData(file);
            T actualData = convertToActualDataFormat(fileIs);
            dataMapCopy.put(getDataIdentifier(actualData, file), actualData);
            return dataMapCopy;
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            if (fileIs != null) {
                try {
                    fileIs.close();
                } catch (IOException e) {
                    log.warn(e.getMessage(), e);
                }
            }
        }
    }

    protected String getDataIdentifier(T actualData, File actualFile){
        return actualFile.getName();
    }

    protected void fillInitialData() throws Exception {
        data.clear();
        File fileToMonitor = new File(getPathToMonitorInternal());
        if (fileToMonitor.exists() && fileToMonitor.isDirectory()) {
            List<File> actualFiles = getActualFiles();
            for (File actualFile : actualFiles) {
                data = getConvertedOnFileChange(actualFile, FileChange.CREATED, data);
            }
            setDataNoLock(data);
        }
    }

    @Override
    public void start() throws Exception {
        log.info("try to start monitor for changes the path: {}", getPathToMonitorInternal());
        stop();
        fillInitialData();
        if (!started && monitor != null) {
            monitor.start();
            started = true;
        } else {
            String error = "Monitor is not configured";
            log.error(error);
        }
    }

    @Override
    public void stop() throws Exception {
        log.info("try to stop monitor for changes the path: {}", getPathToMonitorInternal());
        try {
            if (started && monitor != null) {
                monitor.stop();
            } else {
                String error = "Monitor is not configured or  already stopped";
                log.warn(error);
            }
        } finally {
            started = false;
        }
    }

    @Override
    public boolean isStart() throws Exception{
        return started;
    }

    protected List<File> getActualFiles(){
        List<File> result = new ArrayList<File>();
        File folder = new File(getPathToMonitorInternal());
        for (final File f : folder.listFiles()) {
            if (f.isFile()) {
                result.add(f);
            }
        }
        return result;
    }

    protected InputStream getActualFileData(File file) throws FileNotFoundException {
        return new FileInputStream(file);
    }

    protected Map<String, T> readWithLock(){
        readLock.lock();
        try {
            return (Map<String, T>)((HashMap)getDataNoLock()).clone();
        } finally {
            readLock.unlock();
        }
    }

    protected void writeWithLock(Map<String, T> data){
        writeLock.lock();
        try {
            setDataNoLock(data);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void init() throws Exception {
        init(getPathToMonitor());
    }

    @Override
    public void init(String pathToMonitor) throws IOException {
        log.debug("try to init monitor for changes the path: {}", pathToMonitor);
        listeners = new ArrayList<>();
        String monitorPathCanonical = FileUtils.getFilesPathCanonical(pathToMonitor);
        log.debug("try to init monitor for changes the path in canonical view: {}", monitorPathCanonical);
        this.setPathToMonitorInternal(monitorPathCanonical);
        if (StringUtils.isEmpty(getPathToMonitorInternal())) {
            log.error("pathToMonitor empty or null");
//            throw new IllegalArgumentException("pathToMonitor can not be empty or null");
        }
        initLocks();
        File fileToMonitor = new File(getPathToMonitorInternal());
        if (fileToMonitor.exists() && fileToMonitor.isDirectory()) {
            observer = createObserver();
            monitor = createMonitor();
            listener = createListener();
            observer.addListener(listener);
            monitor.addObserver(observer);
        }  else {
            log.error("Monitor for changes the is not exists or not a folder: {}", getPathToMonitorInternal());
        }
    }

    @Override
    public Map<String, T> getData(){
        return readWithLock();
    }

    protected String getPathToMonitorInternal() {
        return pathToMonitorInternal;
    }

    protected void setPathToMonitorInternal(String pathToMonitorInternal) {
        this.pathToMonitorInternal = pathToMonitorInternal;
    }

    @Override
    public boolean addNewFolderDataEventListener(NewFolderDataEventListener listener) {
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
            return true;
        }
        return false;
    }

    protected void fireNewFolderDataEvent(Map<String, T> newData){
        for (NewFolderDataEventListener listener : this.listeners) {
            listener.onNewData(newData);
        }
    }

    protected abstract T convertToActualDataFormat(InputStream fileIs) throws Exception;
    protected abstract String getPathToMonitor() throws Exception;

    public enum FileChange{
        CREATED,
        DELETED,
        CHANGED,
    }

}
