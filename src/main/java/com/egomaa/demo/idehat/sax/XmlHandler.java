package com.egomaa.demo.idehat.sax;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.util.*;

public class XmlHandler extends DefaultHandler {
    private String currentClass;
    private Map<String, Set<String>> paramValues = new HashMap<>(); // Stores values for each parameter
    private List<Map<String, String>> objects = new ArrayList<>(); // Stores all objects as key-value pairs
    private Map<String, String> currentObject; // Stores the current object being parsed
    private Map<String, List<String>> results = new LinkedHashMap<>(); // Use LinkedHashMap to preserve insertion order

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("class".equals(qName)) {
            currentClass = attributes.getValue("name"); // Get the class name
            paramValues.clear(); // Reset parameter values for the new class
            objects.clear(); // Reset objects for the new class
        } else if ("object".equals(qName)) {
            currentObject = new HashMap<>(); // Initialize a new object
        } else if ("parameter".equals(qName)) {
            String paramName = attributes.getValue("name");
            String paramValue = attributes.getValue("value");
            currentObject.put(paramName, paramValue); // Add parameter to the current object

            // Track values for each parameter
            paramValues.computeIfAbsent(paramName, k -> new HashSet<>()).add(paramValue);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("object".equals(qName)) {
            objects.add(currentObject); // Add the completed object to the list
        } else if ("class".equals(qName)) {
            // Identify the unique parameter for the current class
            String uniqueParam = identifyUniqueParameter();
            if (uniqueParam != null) {
                // Generate the result for the current class
                List<String> classResults = new ArrayList<>();
                for (Map<String, String> object : objects) {
                    String value = object.get(uniqueParam);
                    classResults.add(currentClass + "_" + uniqueParam + "_" + value);
                }
                results.put(currentClass, classResults); // Store the results for the class
            }
        }
    }

    // Method to identify the unique parameter
    private String identifyUniqueParameter() {
        for (Map.Entry<String, Set<String>> entry : paramValues.entrySet()) {
            if (entry.getValue().size() == objects.size()) {
                return entry.getKey(); // This parameter has unique values for all objects
            }
        }
        return null; // No unique parameter found
    }

    // Method to get the results
    public Map<String, List<String>> getResults() {
        return results;
    }
}