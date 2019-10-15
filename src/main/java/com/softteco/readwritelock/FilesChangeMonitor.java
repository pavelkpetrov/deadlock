package com.softteco.readwritelock;

import java.io.IOException;
import java.util.Map;

public interface FilesChangeMonitor<T> {
    void init() throws Exception;
    void init(String pathToMonitor) throws IOException;
    void start() throws Exception;
    void stop() throws Exception;
    boolean isStart() throws Exception;
    Map<String, T> getData();
    boolean addNewFolderDataEventListener(NewFolderDataEventListener listener);

    interface NewFolderDataEventListener<T> {
        void onNewData(Map<String, T> newData);
    }
}
