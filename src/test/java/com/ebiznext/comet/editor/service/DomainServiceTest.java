package com.ebiznext.comet.editor.service;

import com.ebiznext.comet.schema.model.Domain;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.*;

public class DomainServiceTest {

    private final Logger log = LoggerFactory.getLogger(DomainServiceTest.class);

    private String samplesDir = org.apache.hadoop.fs.Path.CUR_DIR +org.apache.hadoop.fs.Path.SEPARATOR + "src/test/resources/samples/";
    private String defaultWorkingDir = org.apache.hadoop.fs.Path.CUR_DIR +org.apache.hadoop.fs.Path.SEPARATOR + "src/test/resources/samples/generated_test_results/";


    private DomainService service;

    @Before
    public void setUp() throws IOException {
         service = new DomainService(defaultWorkingDir);
        // copy dream.yml from samples to workingDir
        String filename = "dream.yml";
        if(!Files.exists(Paths.get(defaultWorkingDir+filename)))
            Files.copy(Paths.get(samplesDir+filename), Paths.get(defaultWorkingDir+filename));

        if(!Files.exists(Paths.get(defaultWorkingDir+"test.json")))
            Files.copy(Paths.get(samplesDir+filename), Paths.get(defaultWorkingDir+"test.json"));

        if(!Files.exists(Paths.get(defaultWorkingDir+"types.yml")))
            Files.copy(Paths.get(samplesDir+filename), Paths.get(defaultWorkingDir+"types.yml"));

    }

    @Test
    public void test_loadAsClass() throws IOException {
        String filename = "dream.yml";
        Domain domain = service.loadAsClass(filename);
        assertNotNull("dream", domain.name());
    }

    @Test
    public void test_loadAsJson() throws IOException, URISyntaxException {
        String filename = "dream.yml";
        JsonNode json = service.loadAsJson(filename);
        assertNotNull(json);
        System.out.println(json);

        assertNotNull("dream", json.get("name"));
    }

    @Test
    public void test_writeAsJson() throws IOException {
        String filename = "dream.yml";
        JsonNode json = service.loadAsJson(filename);
        assertNotNull(json);
        System.out.println(json);
        assertNotNull("dream", json.get("name"));
        service.saveAsJson("dream.json", json);
    }


    @Test
    public void test_saveDomain_withSmallTestFile() throws IOException {
        String filename = "test";
        JsonNode json = service.loadAsJson(filename+".json");
        boolean res = service.saveDomainInStaging("small_test_file", json);
        assertTrue(res);
    }

    @Test
    public void test_saveDomain_withBigDomainFile() throws IOException {
        String filename = "dream";
        JsonNode domain = service.getDomain(filename);
        boolean res = service.saveDomainInStaging("big_domain_file", domain);
        assertTrue(res);
    }


    @Test
    public void test_listAllDomainsFile() {

        List<String> filenames = service.listAllDomainsFile();
        assertNotNull(filenames);
        assertTrue(!filenames.isEmpty());
        assertEquals(2, filenames.size());
        assertTrue(filenames.stream().anyMatch(s -> "dream".equalsIgnoreCase(s)));
    }

    @Test
    public void test_getGeneralMetadata() throws IOException {
        JsonNode metadata = service.getGeneralMetadata("dream");
        assertNotNull(metadata);
        log.debug(metadata.toString());
        assertEquals("FILE",metadata.get("mode").textValue());
        assertEquals("DSV",metadata.get("format").textValue());
    }

    @Test
    public void test_getSchemaNames() throws IOException {
        List<String> schemaNames = service.getSchemaNames("dream");
        assertNotNull(schemaNames);
        assertFalse(schemaNames.isEmpty());
        assertEquals(2, schemaNames.size());
        assertTrue(schemaNames.contains("segment"));
        assertTrue(schemaNames.contains("client"));
    }

    @Test
    public void test_getSchema() throws IOException {

        String expectedSchemaName = "client";
        JsonNode schemaNode = service.getSchema("dream", expectedSchemaName);

        assertNotNull(schemaNode);
        assertEquals(expectedSchemaName, schemaNode.get("name").asText());
        assertEquals("OneClient_Contact.*", schemaNode.get("pattern").asText());
        assertEquals(82, schemaNode.get("attributes").size());
    }

    @Test
    public void test_publish() throws IOException {
        String domainName = "dream";

        JsonNode domain = service.getDomain(domainName);
        service.saveDomainInStaging("",domain);

        boolean res = service.publish(domainName);
        assertTrue(res);
    }

    @Test
    public void test_delete() {
        String domainName = "dream";
        boolean res = service.delete(domainName);
        assertTrue(res);
    }

    @Test
    public void test_validate() throws IOException {
        String filename = "dream.yml";
        JsonNode json = service.loadAsJson(filename);
        String domainContent = json.toString();
        assertNotNull(domainContent);
        assertFalse(domainContent.isEmpty());
        log.debug("domainContent="+domainContent);
        String typeContent = service.loadAsJson("types.yml").toString();
        assertNotNull(typeContent);
        assertFalse(typeContent.isEmpty());
        log.debug("typeContent="+typeContent);
        service.validate(domainContent, typeContent);

    }
}
