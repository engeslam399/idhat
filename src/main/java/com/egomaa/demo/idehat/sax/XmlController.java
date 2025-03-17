package com.egomaa.demo.idehat.sax;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class XmlController {
    private final XmlParserService xmlParserService;

    public XmlController(XmlParserService xmlParserService) {
        this.xmlParserService = xmlParserService;
    }

    @GetMapping("/parse-xml")
    public Map<String, List<String>> parseXml() {
        try {
            return xmlParserService.parseXmlFile(); // No need to pass the file name
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse XML file", e);
        }
    }
}