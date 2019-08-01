package com.zendesk.connect;

import android.content.Context;

import androidx.annotation.Nullable;

import com.zendesk.logger.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

import javax.inject.Inject;

/**
 * A class to read and write files to the internal storage of the host app.
 */
class FileStorage {

    private static final String LOG_TAG = "FileStorage";

    private final Context context;

    @Inject
    FileStorage(Context context) {
        this.context = context;
    }

    /**
     * Returns an {@link InputStream} of the file, given its file name. If the file doesn't exist
     * then null is returned instead.
     *
     * @param fileName the name of the file to read
     * @return the file as an {@link InputStream}
     */
    @Nullable
    InputStream getFileAsInputStream(String fileName) {
        try {
            return context.openFileInput(fileName);
        } catch (FileNotFoundException exception) {
            return null;
        }
    }

    /**
     * Returns the file as an {@link Object} so it can be casted to its actual class by the caller.
     * If the file doesn't exist or if any error happens then null is returned instead.
     *
     * @param fileName the name of the file to read
     * @return the file as an {@link Object}
     */
    @Nullable
    Object getFileAsObject(String fileName) {
        try (FileInputStream fileInputStream = context.openFileInput(fileName);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {

            return objectInputStream.readObject();
        } catch (IOException | NullPointerException | ClassNotFoundException exception) {
            return null;
        }
    }

    /**
     * Saves the given {@link InputStream} to a file in the internal storage of the host app.
     * If the file already exists then it will be replaced.
     *
     * @param inputStream the {@link InputStream} to be saved
     * @param fileName the name of the new file
     */
    void saveToFile(InputStream inputStream, String fileName) {
        try (FileOutputStream outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer, 0, 1024)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
        } catch (IOException | NullPointerException exception) {
            Logger.w(LOG_TAG, "Could not save to file %s.", exception, fileName);
        }
    }

    /**
     * Saves the given {@link Serializable} object to a file in the internal storage of the host app.
     * If the file already exists then it will be replaced.
     *
     * @param serializable a {@link Serializable} object instance to be saved
     * @param fileName the name of the new file
     */
    void saveToFile(Serializable serializable, String fileName) {
        try (FileOutputStream outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {

            objectOutputStream.writeObject(serializable);
        } catch (IOException exception) {
            Logger.w(LOG_TAG, "Could not save to file %s.", exception, fileName);
        }
    }

    /**
     * Attempts to delete a file from the internal storage of the host app, given the file name.
     *
     * @param fileName the name of the file to be deleted
     * @return true if the file was deleted, false otherwise
     */
    boolean deleteFile(String fileName) {
        return context.deleteFile(fileName);
    }

    /**
     * Checks if a file exist, given its name.
     *
     * @param fileName the name of the file to be checked
     * @return true if the file exists, false otherwise
     */
    boolean exists(String fileName) {
        return Arrays.asList(context.fileList()).contains(fileName);
    }

}
