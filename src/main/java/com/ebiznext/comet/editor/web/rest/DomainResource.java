package com.ebiznext.comet.editor.web.rest;

import com.ebiznext.comet.editor.service.DomainService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/domains")
public class DomainResource {

    @Autowired
    private DomainService service;

    @GetMapping
    public List<String> listAll(){
        return service.listAllDomainsFile();
    }

    @GetMapping("/{domainName}")
    public JsonNode getDomain(@PathVariable String domainName) throws IOException {
        return service.getDomain(domainName);
    }

    @GetMapping("/{domainName}/metadata")
    public JsonNode getDomainMetadata(@PathVariable String domainName) throws IOException {
        return service.getGeneralMetadata(domainName);
    }

    @GetMapping("/{domainName}/schemas")
    public List<String> getDomainSchemas(@PathVariable String domainName) throws IOException {
        return service.getSchemaNames(domainName);
    }

    @GetMapping("/{domainName}/schema/{schemaName}")
    public JsonNode getDomainSchema(@PathVariable String domainName, @PathVariable String schemaName) throws IOException {
        return service.getSchema(domainName, schemaName);
    }

    @PostMapping("/{domainName}/_publish")
    public void publish(@PathVariable String domainName) throws IOException {
        service.publish(domainName);
    }

    @DeleteMapping("/{domainName}")
    public void delete(@PathVariable String domainName){
        service.delete(domainName);
    }

    @PutMapping("/{domainName}")
    public void saveDomain(@PathVariable String domainName, @RequestBody JsonNode domain){
        service.saveDomainInStaging("", domain);
    }

    @PutMapping("/{domainName}/schema/{schemaName}")
    public void saveSchema(@PathVariable String domainName, @PathVariable String schemaName,@RequestBody JsonNode schema){
        service.saveSchemaInStaging("",domainName, schema);
    }


}
