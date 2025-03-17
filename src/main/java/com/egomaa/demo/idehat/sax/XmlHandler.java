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
    private int syntheticIdCounter = 1; // Counter for synthetic keys

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("class".equals(qName)) {
            currentClass = attributes.getValue("name");
            paramValues.clear();
            objects.clear();
            syntheticIdCounter = 1;
        } else if ("object".equals(qName)) {
            currentObject = new HashMap<>();
        } else if ("parameter".equals(qName)) {
            String paramName = attributes.getValue("name");
            String paramValue = attributes.getValue("value");
            currentObject.put(paramName, paramValue);
            paramValues.computeIfAbsent(paramName, k -> new HashSet<>()).add(paramValue);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("object".equals(qName)) {
            objects.add(currentObject);
        } else if ("class".equals(qName)) {
            String uniqueParam = identifyUniqueParameter();
            List<String> classResults = new ArrayList<>();

            if (uniqueParam != null) {
                // Single unique parameter found
                for (Map<String, String> object : objects) {
                    String value = object.get(uniqueParam);
                    classResults.add(currentClass + "_" + uniqueParam + "_" + value);
                }
            } else {
                // Try to find minimum combination of parameters for composite key
                List<String> compositeParams = findMinimumCompositeParameters();
                if (compositeParams != null && !compositeParams.isEmpty()) {
                    for (Map<String, String> object : objects) {
                        String compositeKey = createCompositeKey(object, compositeParams);
                        classResults.add(currentClass + "_" + compositeKey);
                    }
                } else {
                    // Fallback to synthetic keys
                    for (int i = 0; i < objects.size(); i++) {
                        classResults.add(currentClass + "_object_" + syntheticIdCounter++);
                    }
                }
            }
            results.put(currentClass, classResults);
        }
    }

    private String identifyUniqueParameter() {
        for (Map.Entry<String, Set<String>> entry : paramValues.entrySet()) {
            if (entry.getValue().size() == objects.size()) {
                return entry.getKey();
            }
        }
        return null;
    }

    private List<String> findMinimumCompositeParameters() {
        List<String> allParams = new ArrayList<>(paramValues.keySet());

        // Try combinations from 2 parameters up to all parameters
        for (int size = 2; size <= allParams.size(); size++) {
            List<List<String>> combinations = generateCombinations(allParams, size);
            for (List<String> combo : combinations) {
                if (isUniqueCombination(combo)) {
                    return combo; // Return the first valid combination found
                }
            }
        }
        return null; // No unique combination found
    }

    private List<List<String>> generateCombinations(List<String> params, int size) {
        List<List<String>> result = new ArrayList<>();
        generateCombinationsHelper(params, size, 0, new ArrayList<>(), result);
        return result;
    }

    private void generateCombinationsHelper(List<String> params, int size, int start,
                                            List<String> current, List<List<String>> result) {
        if (current.size() == size) {
            result.add(new ArrayList<>(current));
            return;
        }
        for (int i = start; i < params.size(); i++) {
            current.add(params.get(i));
            generateCombinationsHelper(params, size, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    private boolean isUniqueCombination(List<String> params) {
        Set<String> combinations = new HashSet<>();
        for (Map<String, String> object : objects) {
            StringBuilder key = new StringBuilder();
            for (String param : params) {
                String value = object.get(param);
                if (value == null) return false;
                key.append(param).append("_").append(value).append("_");
            }
            String compositeKey = key.substring(0, key.length() - 1);
            if (!combinations.add(compositeKey)) {
                return false; // Duplicate found
            }
        }
        return combinations.size() == objects.size();
    }

    private String createCompositeKey(Map<String, String> object, List<String> params) {
        StringBuilder compositeKey = new StringBuilder();
        for (String param : params) {
            String value = object.get(param);
            compositeKey.append(param).append("_").append(value).append("_");
        }
        return compositeKey.substring(0, compositeKey.length() - 1);
    }

    public Map<String, List<String>> getResults() {
        return results;
    }
}