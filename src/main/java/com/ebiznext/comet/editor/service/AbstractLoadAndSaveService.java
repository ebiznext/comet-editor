package com.ebiznext.comet.editor.service;

import com.ebiznext.comet.schema.handlers.HdfsStorageHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AbstractLoadAndSaveService {

    private final Logger log = LoggerFactory.getLogger(AbstractLoadAndSaveService.class);

    protected String workingDir;
    protected HdfsStorageHandler storageHandler;

    public AbstractLoadAndSaveService(String workingDir, HdfsStorageHandler storageHandler) {
        this.workingDir = workingDir;
        this.storageHandler = storageHandler;
    }

    /**
     * Read a YAML domain file and return it as JSON
     * @param filename
     * @param rootPath
     * @return
     * @throws IOException
     */
    protected JsonNode loadAsJson(String filename, String rootPath) throws IOException {
        Path path = new Path(rootPath + Path.SEPARATOR + filename);
        return loadAsJson(path);
    }

    /**
     * Read a YAML domain file and return it as JSON
     * @param filename
     * @return
     * @throws IOException
     */
    protected JsonNode loadAsJson(String filename) throws IOException {
        log.info("loadAsJson with filename : "+filename);
        return loadAsJson(filename, workingDir);
    }

    /**
     * Read a YAML domain file and return it as JSON
     * @param path
     * @return
     * @throws IOException
     */
    protected JsonNode loadAsJson(Path path) throws IOException {
        log.info("loadAsJson with path : "+path.toString());
        String content = storageHandler.read(path);
        log.debug(content);

        // convert in json
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        JsonNode json = mapper.readTree(content);

        return json;
    }

}
