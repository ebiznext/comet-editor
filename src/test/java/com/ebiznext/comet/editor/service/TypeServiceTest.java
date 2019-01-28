package com.ebiznext.comet.editor.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.hadoop.fs.Path;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.Assert.*;

public class TypeServiceTest {

    private final Logger log = LoggerFactory.getLogger(TypeServiceTest.class);

    private TypeService service;

    @Before
    public void setUp(){
        String workDir = Path.CUR_DIR +Path.SEPARATOR + "src/test/resources/samples/";
        //String workDir = Path.CUR_DIR +Path.SEPARATOR;
        service = new TypeService(workDir);

    }

    @Test
    public void test_loadDefaultDef() throws IOException {
        JsonNode def = service.loadDefaultDef();

        assertNotNull(def);

    }

    @Test
    public void loadOveridingTypeDef() {
    }

    @Test
    public void loadDefinition() {
    }

    @Test
    public void saveType() {
    }
}
