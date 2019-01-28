package com.ebiznext.comet.editor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Comet Editor.
 * <p>
 * Properties are configured in the application.yml file.
 * See {@link io.github.jhipster.config.JHipsterProperties} for a good example.
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {


    private String directory;

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

}
