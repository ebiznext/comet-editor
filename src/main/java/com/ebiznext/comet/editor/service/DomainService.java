package com.ebiznext.comet.editor.service;

import com.ebiznext.comet.schema.handlers.HdfsStorageHandler;
import com.ebiznext.comet.schema.handlers.SchemaHandler;
import com.ebiznext.comet.schema.model.Domain;
import com.ebiznext.comet.schema.model.Types;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import scala.collection.Iterator;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Service
public class DomainService extends AbstractLoadAndSaveService {

    private final Logger log = LoggerFactory.getLogger(DomainService.class);

    private final static String STAGING = "staging";
    private final static String SCHEMAS_DIR = "schemas";
    private final static String YAML_EXT = ".yml";
    private final static String JSON_EXT = ".json";

    @Autowired
    public DomainService(@Value("${application.directory}") String workingDir){
        super(workingDir, new HdfsStorageHandler());

        initWorkingDir();
    }



    /**
     * the working dir tree looks like this :
     *
     * working_dir/
     *             .... (published domains and type files)
     *            /staging/
     *              <domain_name>/
     *                  domain.yml
     *                  schema1.yml
     *                  schema2.yml
     *                  ...
     *            /deleted/
     *              ...
     */
    private void initWorkingDir(){
        Path path = new Path(workingDir);
        storageHandler.mkdirs(path);
    }

    /**
     * Load a domain file
     * @param filename
     * @return
     */
    protected Domain loadAsClass(String filename) throws IOException {

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
//        mapper.registerModule(DefaultScalaModule);


        Path path = new Path(workingDir + Path.SEPARATOR + filename);
        String content = storageHandler.read(path);

        //mapper.readTree()
        Domain domain = mapper.readValue(content, Domain.class);

        return domain;

    }




    /**
     * load the published Domain definition
     * @param domainName
     * @return
     * @throws IOException
     */
    public JsonNode getDomain(String domainName) throws IOException {
        return loadAsJson(domainName+YAML_EXT);
    }

    /**
     * load the general metadata from a publised domain definition
     * @param domainName
     * @return
     * @throws IOException
     */
    public JsonNode getGeneralMetadata(String domainName) throws IOException {
        return getDomain(domainName).get("metadata");
    }

    /**
     * Return the existing schemas into a published Domain
     * @param domainName
     * @return
     * @throws IOException
     */
    public List<String> getSchemaNames(String domainName) throws IOException {
        List<String> schemaNames = new ArrayList<>();
        JsonNode schemaArrNode = getDomain(domainName).get("schemas");
        if (schemaArrNode.isArray()) {
            for (final JsonNode schemaNode : schemaArrNode) {
                schemaNames.add(schemaNode.get("name").asText());
            }
        }
        return schemaNames;
    }


    /**
     * Load the schema part of a published Domain
     * @param domainName
     * @param schema
     * @return
     * @throws IOException
     */
    public JsonNode getSchema(String domainName, String schema) throws IOException {
        Objects.requireNonNull(domainName);
        Objects.requireNonNull(schema);

        JsonNode schemaArrNode = getDomain(domainName).get("schemas");
        if (schemaArrNode.isArray()) {
            for (final JsonNode schemaNode : schemaArrNode) {
                if(schema.equals(schemaNode.get("name").asText())){
                    return schemaNode;
                }
            }
        }
        return null;
    }




    /**
     * List all domain files (published and / or in staging)
     */
    public List<String> listAllDomainsFile(){
        List<String> filenames = new ArrayList();
        Path path = new Path(workingDir);
        log.info("listAllDomainsFile : "+path.toString());

        scala.collection.immutable.List<Path> paths = storageHandler.list(path, "yml", LocalDateTime.MIN);
        if(paths.isEmpty())
            log.warn("no domain or YAML files found or empty directory");

//        Decorators.AsJava<java.util.List<Path>> jPaths = scala.collection.JavaConverters.mutableSeqAsJavaListConverter(new scala.collection.mutable.MutableList<>(paths));

        scala.collection.Iterator<Path> it = paths.iterator();
        while(it.hasNext()){

            String filename = it.next().getName();
            String domainName = filename.substring(0, filename.indexOf(YAML_EXT));
            filenames.add(domainName);
        }
        return filenames;
    }

    /**
     * Save a JSON structure into a JSON file
     * @param filename
     * @param content
     * @return
     */
    protected boolean saveAsJson(String filename, JsonNode content){

        return write(filename, content.toString());
    }

    /**
     * Save a JSON structure into a YAML file
     * @param filename
     * @param jsonNode
     * @return
     */
    protected boolean saveAsYAML(String filename, JsonNode jsonNode) {
        log.info("saveAsYAML: "+filename);
        String content = jsonNode.toString();
        try {
            content = new YAMLMapper().writeValueAsString(jsonNode);
            log.debug("yaml = \n " + content);
            return write(filename, content);
        } catch (JsonProcessingException e) {
            log.error("cannot parse jsonNode :" + jsonNode, e);
            return false;
        }
    }

    /**
     * Save a Domain File
     * => should be saved into the staging directory
     * @param content
     * @return
     */
    public boolean saveDomainInStaging(String rootDir, JsonNode content) {
        Objects.requireNonNull(content);
        Objects.requireNonNull(content.get("name"));

        String domainName = content.get("name").asText();

        //save the complete file
        saveAsYAML(getStagingFilePath(rootDir, domainName,"_full", YAML_EXT), content);

        JsonNode schemaArrNode = content.get("schemas");

        //save the schemas if exists
        if (schemaArrNode!=null && schemaArrNode.isArray()) {
            for (final JsonNode schemaNode : schemaArrNode) {
                //String schemaName = schemaNode.get("name").asText();
                saveSchemaInStaging(rootDir, domainName, schemaNode);
            }
            ((ObjectNode) content).remove("schemas");
        }

        //save the domain-only
        saveAsYAML(getStagingFilePath(rootDir, domainName,"_domain", YAML_EXT), content);

        return true;
    }

    /**
     * Save a schema only into a YAML file in the staging/schemas sub-directory
     * @param domainName
     * @param content
     * @return
     */
    public boolean saveSchemaInStaging(String rootDir, String domainName, JsonNode content) {
        Objects.requireNonNull(domainName);
        Objects.requireNonNull(content);
        Objects.requireNonNull(content.get("name"));
        String schemaName = content.get("name").asText();
        Objects.requireNonNull(schemaName);

        return saveAsYAML(getStagingSchemaFilePath(rootDir, domainName, schemaName, YAML_EXT), content);
    }


    /**
     * Write a file (based on
     * @param filename
     * @param content
     * @return
     */
    private boolean write(String filename, String content){
        try {
            Path path = new Path(workingDir + Path.SEPARATOR + filename);
            storageHandler.write(content, path);
            return true;
        }catch(Throwable t){
            log.error("Unexpected error writing file", t);
            return false;
        }
    }

    /**
     * validate a domain definition against some type definition
     * @param domainContent
     * @param typeContent
     */
    public void validate(String domainContent, String typeContent){
        Objects.requireNonNull(domainContent);
        Objects.requireNonNull(typeContent);

        SchemaHandler schemaHandler = new SchemaHandler(storageHandler);

        /*
        Domain domain = schemaHandler.fromYAML(domainContent);

        Types types = schemaHandler.fromYAML(typeContent);

        domain.checkValidity(types);
        */
    }
    /**
     * Validate first, then publish the file (overwrite the existing version)
     * TODO should we keep the previous version for rollback ?
     * @param domainName
     * @return
     */
    public boolean publish(String domainName) throws IOException {
        log.info("publishing the domain "+domainName);
        String rootDir = workingDir;

        // Load the domain object
        ObjectNode domainObject = (ObjectNode) loadAsJson(getStagingFilePath("", domainName,"_domain",YAML_EXT));
        Objects.requireNonNull(domainObject);

        // List and load all schema objects
        String schemaDir = getStagingSchemaFilePath(rootDir, domainName, "", "");
        log.debug("looking for schema in directory : "+schemaDir);
        scala.collection.immutable.List<Path> schemaPaths = storageHandler.list(new Path("./"+schemaDir), "yml", LocalDateTime.MIN);
        if(schemaPaths.isEmpty())
            throw new RuntimeException("You cannot publish a domain without a schema");


        log.info("loading domain schema objects");
        List<JsonNode> schemaNodes = new ArrayList<>();
        Iterator<Path> it = schemaPaths.iterator();
        while(it.hasNext()){
            schemaNodes.add(loadAsJson(it.next()));
        }

        // Merge the schemas into the domain object
        ArrayNode schemasArr = domainObject.putArray("schemas");
        schemasArr.addAll(schemaNodes);

        // Save as tmp full file (optional)
        //saveAsYAML(getDomainFilePath(rootDir, "_"+domainName+"_"+LocalDate.now().toString(), ".tmp"+YAML_EXT), domainObject);


        // TODO checkValidity handler.
        //validate();

        // if OK, save the new version
        boolean res = saveAsYAML(getDomainFilePath("", domainName+"_"+ LocalDate.now().toString(), YAML_EXT), domainObject);


        return res;
    }

    /**
     * Move the file into the deleted sub-directory and will be evicted in a later date
     * @return
     */
    public boolean delete(String domainName){
        log.info("delete "+domainName);
        Path src = new Path(workingDir+getDomainFilePath("", domainName, YAML_EXT));
        Path dest = new Path(workingDir+getDomainFilePath("", "deleted"+Path.SEPARATOR+domainName, YAML_EXT));
        log.info("move from:\n"+src.toString()+"\nto\n"+dest.toString());
        boolean moveRes = storageHandler.move(src, dest);

        Path stagingPath = new Path(workingDir+getStagingFilePath("", domainName, "", ""));
        log.info("delete staging dir : "+ stagingPath.toString());
        storageHandler.delete(stagingPath);
        return moveRes;
    }

    private String getDomainFilePath(String rootDir, String domainName, String extension){
        return Path.SEPARATOR + rootDir + Path.SEPARATOR + domainName + extension;
    }

    private String getStagingFilePath(String rootDir, String domainName, String filename ,String extension){
        return Path.SEPARATOR + rootDir + Path.SEPARATOR + STAGING + Path.SEPARATOR + domainName + Path.SEPARATOR + filename + extension;
    }

    private String getStagingSchemaFilePath(String rootDir, String domainName, String filename ,String extension){
        return Path.SEPARATOR + rootDir + Path.SEPARATOR + STAGING + Path.SEPARATOR + domainName + Path.SEPARATOR + SCHEMAS_DIR + Path.SEPARATOR + filename + extension;
    }
}
