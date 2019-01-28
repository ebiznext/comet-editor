package com.ebiznext.comet.editor.service;

import com.ebiznext.comet.schema.handlers.HdfsStorageHandler;
import com.ebiznext.comet.schema.model.Domain;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.hadoop.fs.Path;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

public class DomainServiceTest {

    private final Logger log = LoggerFactory.getLogger(DomainServiceTest.class);

    private DomainService service = new DomainService(Path.CUR_DIR +Path.SEPARATOR + "src/test/resources/samples/generated_test_results/", new HdfsStorageHandler());

    @Test
    public void test_loadAsClass() throws IOException {
        String filename = "dream.yml";
        Domain domain = service.loadAsClass(filename);
        assertNotNull("dream", domain.name());
    }

    @Test
    public void test_loadAsJson() throws IOException {
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
        service.saveAsJson("generated_test_results/dream.json", json);
    }


    @Test
    public void test_saveDomain_withSmallTestFile() throws IOException {
        String filename = "test";
        JsonNode json = service.loadAsJson(filename+".json");
        boolean res = service.saveDomainInStaging("generated_test_results", json);
        assertTrue(res);
    }

    @Test
    public void test_saveDomain_withBigDomainFile() throws IOException {
        String filename = "dream.yml";
        JsonNode domain = service.getDomain(filename);
        boolean res = service.saveDomainInStaging("generated_test_results", domain);
        assertTrue(res);
    }


    @Test
    public void listAllDomainsFile() {

        List<String> filenames = service.listAllDomainsFile();
        assertNotNull(filenames);
        assertTrue(!filenames.isEmpty());
        assertEquals(2, filenames.size());
//        assertTrue(filenames.stream().anyMatch(s -> "toto.txt".equalsIgnoreCase(s)));
        assertTrue(filenames.stream().anyMatch(s -> "dream.yml".equalsIgnoreCase(s)));
        assertTrue(filenames.stream().anyMatch(s -> "types.yml".equalsIgnoreCase(s)));

    }

    @Test
    public void test_getGeneralMetadata() throws IOException {
        JsonNode metadata = service.getGeneralMetadata("dream.yml");
        assertNotNull(metadata);
        log.debug(metadata.toString());
        assertEquals("FILE",metadata.get("mode").textValue());
        assertEquals("DSV",metadata.get("format").textValue());
    }

    @Test
    public void test_getSchemaNames() throws IOException {
        List<String> schemaNames = service.getSchemaNames("dream.yml");
        assertNotNull(schemaNames);
        assertFalse(schemaNames.isEmpty());
        assertEquals(2, schemaNames.size());
        assertTrue(schemaNames.contains("segment"));
        assertTrue(schemaNames.contains("client"));
    }

    @Test
    public void test_getSchema() throws IOException {

        String expectedSchemaName = "client";
        JsonNode schemaNode = service.getSchema("dream.yml", expectedSchemaName);

        assertNotNull(schemaNode);
        assertEquals(expectedSchemaName, schemaNode.get("name").asText());
        assertEquals("OneClient_Contact.*", schemaNode.get("pattern").asText());
        assertEquals(82, schemaNode.get("attributes").size());
    }

    @Test
    public void test_publish() throws IOException {
        String domainName = "dream";
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
        log.info("domainContent="+domainContent);
        String typeContent = service.loadAsJson("types.yml").toString();
        log.info("typeContent="+typeContent);
        service.validate(domainContent, typeContent);

    }
}
