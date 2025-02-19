package com.example.imageservice.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Sorts;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GridFSImageStorageService {

    private static final String UPLOAD_DATE_FIELD = "uploadDate";

    private final GridFSBucket gridFSBucket;

    public ObjectId storeImage(String filename, InputStream content) {
        GridFSUploadOptions options = new GridFSUploadOptions();
        return gridFSBucket.uploadFromStream(filename, content, options);
    }

    public List<GridFSFile> getAllImages() {
        return gridFSBucket.find().sort(Sorts.ascending(UPLOAD_DATE_FIELD)).into(new ArrayList<>());
    }

    public void getImageById(ObjectId id) {
        gridFSBucket.openDownloadStream(id);
    }
}
