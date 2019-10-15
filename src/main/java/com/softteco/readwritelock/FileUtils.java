package com.softteco.readwritelock;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

@Slf4j
public class FileUtils {
    private static final String CONFIG_FILE = "config.properties";
    private final static String COMMA_SEPARATOR = ",";
    private static Properties properties;
    private static File config;

    public static String fileToString(final String fileName) {
        return fileToString(fileName, Charset.forName("windows-1251"));
    }

    public static String fileToString(final String fileName, final Charset charset) {
        final File file = new File(fileName);
        try {
            final byte[] buffer = new byte[(int) file.length()];
            new FileInputStream(file).read(buffer);
            return new String(buffer, charset);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(file.getAbsolutePath(), e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void stringToFile(byte[] responseBody, String ubsbank) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(ubsbank);
            out.write(responseBody);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace(System.out);
                }
            }
        }
    }

    public static Properties loadProperties() throws IOException {
        final Properties properties = new Properties();
        final InputStream inputStream = getInputStreamFromPath("classpath:"+CONFIG_FILE);

        properties.load(inputStream);
        inputStream.close();
        return properties;
    }

    public static URL getImage(final String fileName) {
        return FileUtils.class.getResource("/images/" + fileName);
    }

    public static Properties getProperties() {
        if (properties == null) {
            try {
                properties = FileUtils.loadProperties();
            } catch (IOException e) {
                log.error("Can't load properties", e);
            }
        }
        return properties;
    }

    public static String getProperty(final String key) {
        return getProperties().getProperty(key);
    }

    public static String getProperty(final String prefix, final String suffix, final String language) {
        String key = "";

        if (StringUtils.isNotBlank(prefix)) {
            key += prefix;
        }

        if (StringUtils.isNotBlank(suffix)) {
            key += (StringUtils.isNotBlank(key) ? "." : "") + suffix;
        }

        if (StringUtils.isNotBlank(language)) {
            key += (StringUtils.isNotBlank(key) ? "." : "") + language;
        }

        return getProperties().getProperty(key);
    }

    public static List<String> getPropertiesCommaSeparated(final String key) {
        final String propertyFullStr = FileUtils.getProperties().getProperty(key);
        final List<String> properties = new ArrayList<String>();

        if (StringUtils.isNotBlank(propertyFullStr)) {
            for (String property : StringUtils.split(propertyFullStr, COMMA_SEPARATOR)) {
                properties.add(StringUtils.trim(property));
            }
        }
        return properties;
    }


    public static InputStream getInputStreamFromPath(String path) throws IOException {
        InputStream is;
        String protocol = path.replaceFirst("^(\\w+):.+$", "$1").toLowerCase();
        if ("http".equals(protocol) || "https".equals(protocol)) {
            HttpURLConnection connection = (HttpURLConnection) new URL(path).openConnection();
            int code = connection.getResponseCode();
            if (code >= 400) throw new IOException("Server returned error code #" + code);
            is = connection.getInputStream();
            String contentEncoding = connection.getContentEncoding();
            if (contentEncoding != null && contentEncoding.equalsIgnoreCase("gzip"))
                is = new GZIPInputStream(is);
        } else if ("file".equals(protocol)) {
            is = new URL(path).openStream();
        } else if ("classpath".equals(protocol)) {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path.replaceFirst("^\\w+:", ""));
        } else {
            throw new IOException("Missed or unsupported protocol in path '" + path + "'");
        }
        return is;
    }

    public static String getFilesPathCanonical(String filePath) throws IOException {
        if (filePath != null) {
            filePath = filePath.startsWith("~")
                    ? System.getProperty("user.home") + File.separator + filePath.substring(2)
                    : filePath;
        }
        return new File(filePath).getCanonicalPath();
    }

    public static File createTempDirectory()
            throws IOException
    {
        final File temp;

        temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

        if(!(temp.delete()))
        {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if(!(temp.mkdir()))
        {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }

        return (temp);
    }

    public static File createFileForMonitor(String folderPath, byte[] data, String fileName) throws IOException {
        File file = new File(folderPath + "/" + fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream fielOs = null;
        try {
            fielOs = new FileOutputStream(file);
            fielOs.write(data);
            fielOs.flush();
        } finally {
            if (fielOs != null) {
                fielOs.close();
            }
        }
        return file;
    }

}
