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

@Service
@RequiredArgsConstructor
public class GridFSImageStorageService {

    private static final String UPLOAD_DATE_FIELD = "uploadDate";

    private final GridFSBucket gridFSBucket;

    public void storeImage(String filename, InputStream content) {
        GridFSUploadOptions options = new GridFSUploadOptions();
        gridFSBucket.uploadFromStream(filename, content, options);
    }

    public List<GridFSFile> getAllImages() {
        return gridFSBucket.find().sort(Sorts.ascending(UPLOAD_DATE_FIELD)).into(new ArrayList<>());
    }

    public GridFSFile getImageByFilename(String filename) {
        return gridFSBucket.find(eq("filename", filename)).first();
    }

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

    public void deleteImage(String filename) {
        GridFSFile file = getImageByFilename(filename);
        if (file != null) {
            gridFSBucket.delete(file.getObjectId());
        }
    }

}
