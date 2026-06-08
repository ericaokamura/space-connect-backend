package com.fiap.space_connect.config;

import jakarta.annotation.PostConstruct;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class OrekitConfig {

    @PostConstruct
    public void init() throws Exception {

        File orekitData =
                new File("src/main/resources/regular-data");

        DataProvidersManager manager =
                DataContext.getDefault()
                        .getDataProvidersManager();

        manager.addProvider(
                new DirectoryCrawler(orekitData)
        );
    }
}