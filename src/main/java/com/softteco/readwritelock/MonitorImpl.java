package com.softteco.readwritelock;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.StringWriter;

public class MonitorImpl extends FilesChangeMonitorBean<String> {

    @Override
    protected String convertToActualDataFormat(InputStream fileIs) throws Exception {
        StringWriter writer = new StringWriter();
        IOUtils.copy(fileIs, writer, "UTF-8");
        return writer.toString();
    }

    @Override
    protected String getPathToMonitor() throws Exception {
        return FileUtils.loadProperties().getProperty("monitor.path");
    }
}
