package com.example.imageservice.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Sorts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

/**
 * Service class for managing image storage in GridFS.
 */
@Service
@RequiredArgsConstructor
public class GridFSImageStorageService {

    private static final String UPLOAD_DATE_FIELD = "uploadDate";

    private final GridFSBucket gridFSBucket;

    /**
     * Stores an image in GridFS.
     *
     * @param filename the name of the image file
     * @param content the InputStream of the image content
     */
    public void storeImage(String filename, InputStream content) {
        GridFSUploadOptions options = new GridFSUploadOptions();
        gridFSBucket.uploadFromStream(filename, content, options);
    }

    /**
     * Retrieves a list of all stored images.
     *
     * @return a list of GridFSFile objects representing the stored images
     */
    public List<GridFSFile> getAllImages() {
        return gridFSBucket.find().sort(Sorts.ascending(UPLOAD_DATE_FIELD)).into(new ArrayList<>());
    }

    /**
     * Retrieves an image by its filename.
     *
     * @param filename the name of the image file
     * @return the GridFSFile object representing the image, or null if not found
     */
    public GridFSFile getImageByFilename(String filename) {
        return gridFSBucket.find(eq("filename", filename)).first();
    }

    /**
     * Retrieves the bytes of an image by its filename.
     *
     * @param filename the name of the image file
     * @return a byte array representing the image content, or null if not found
     */
    public byte[] getImageBytes(String filename) {
        GridFSFile file = getImageByFilename(filename);
        if (file != null) {
            GridFSDownloadStream downloadStream = gridFSBucket.openDownloadStream(file.getObjectId());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = downloadStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            return outputStream.toByteArray();
        }
        return null;
    }

    /**
     * Deletes an image by its filename.
     *
     * @param filename the name of the image file
     */
    public void deleteImage(String filename) {
        GridFSFile file = getImageByFilename(filename);
        if (file != null) {
            gridFSBucket.delete(file.getObjectId());
        }
    }

}
