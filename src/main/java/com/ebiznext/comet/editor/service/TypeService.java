package com.ebiznext.comet.editor.service;

import com.ebiznext.comet.schema.handlers.HdfsStorageHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class TypeService extends AbstractLoadAndSaveService {

    private final Logger log = LoggerFactory.getLogger(TypeService.class);

    public TypeService(String workingDir, HdfsStorageHandler handler) {
        super(workingDir, handler);
    }

    public JsonNode loadDefaultDef() throws IOException {
        return this.loadAsJson("default.yml");
    }

    public JsonNode loadOveridingTypeDef() throws IOException {
        return this.loadAsJson("types.yml");
    }

    /**
     * Load default_types.yml merged with types.yml if it exists
     */
    public JsonNode loadDefinition() throws IOException {
        log.info("load default and types yml definition files as a single json");
        JsonNode defaut = this.loadDefaultDef();
        JsonNode types = this.loadOveridingTypeDef();

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        ObjectReader updater = mapper.readerForUpdating(defaut);
        JsonNode merged = updater.readTree(types.toString());

        /*
        MyBean defaults = objectMapper.readValue(defaultJson, MyBean.class);
        ObjectReader updater = objectMapper.readerForUpdating(defaults);
        MyBean merged = updater.readValue(overridesJson);
        */
        return merged;
    }


    public boolean saveType(JsonNode typeDef){

        //TODO

        return false;
    }



}
