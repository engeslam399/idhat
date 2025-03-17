package com.egomaa.demo.idehat.sax;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Service
public class XmlParserService {
    private final ResourceLoader resourceLoader;

    public XmlParserService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public Map<String, List<String>> parseXmlFile() throws Exception {
        // Load the XML file from the resources folder
        Resource resource = resourceLoader.getResource("classpath:config.xml");
        System.out.println("Resource exists: " + resource.exists()); // Debugging

        if (!resource.exists()) {
            throw new RuntimeException("File not found: config.xml");
        }

        InputStream inputStream = resource.getInputStream();

        // Create SAXParser and parse the file
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        XmlHandler handler = new XmlHandler();
        saxParser.parse(inputStream, handler);

        // Return the results
        return handler.getResults();
    }
}