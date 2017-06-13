/*
 * Copyright 2014-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.meituan.robust.tools.aapt;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public final class AaptUtil {

    private static final String ID_DEFINITION_PREFIX = "@+id/";
    private static final String ITEM_TAG             = "item";

    private static final XPathExpression ANDROID_ID_USAGE = createExpression("//@*[starts-with(., '@') and " + "not(starts-with(., '@+')) and " + "not(starts-with(., '@android:')) and " + "not(starts-with(., '@null'))]");

    private static final XPathExpression ANDROID_ID_DEFINITION = createExpression("//@*[starts-with(., '@+') and " + "not(starts-with(., '@+android:id'))]");

    private static final Map<String, RDotTxtEntry.RType> RESOURCE_TYPES = getResourceTypes();
    private static final List<String>       IGNORED_TAGS   = Arrays.asList("eat-comment", "skip");

    private static XPathExpression createExpression(String expressionStr) {
        try {
            return XPathFactory.newInstance().newXPath().compile(expressionStr);
        } catch (XPathExpressionException e) {
            throw new AaptUtilException(e);
        }
    }

    private static Map<String, RDotTxtEntry.RType> getResourceTypes() {
        Map<String, RDotTxtEntry.RType> types = new HashMap<String, RDotTxtEntry.RType>();
        for (RDotTxtEntry.RType rType : RDotTxtEntry.RType.values()) {
            types.put(rType.toString(), rType);
        }
        types.put("string-array", RDotTxtEntry.RType.ARRAY);
        types.put("integer-array", RDotTxtEntry.RType.ARRAY);
        types.put("declare-styleable", RDotTxtEntry.RType.STYLEABLE);
        return types;
    }

    public static com.meituan.robust.tools.aapt.AaptResourceCollector collectResource(List<String> resourceDirectoryList) {
        return collectResource(resourceDirectoryList, null);
    }

    public static com.meituan.robust.tools.aapt.AaptResourceCollector collectResource(List<String> resourceDirectoryList, Map<RDotTxtEntry.RType, Set<RDotTxtEntry>> rTypeResourceMap) {
        com.meituan.robust.tools.aapt.AaptResourceCollector resourceCollector = new com.meituan.robust.tools.aapt.AaptResourceCollector(rTypeResourceMap);
        List<RDotTxtEntry> references = new ArrayList<RDotTxtEntry>();
        for (String resourceDirectory : resourceDirectoryList) {
            try {
                collectResources(resourceDirectory, resourceCollector);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        for (String resourceDirectory : resourceDirectoryList) {
            try {
                processXmlFilesForIds(resourceDirectory, references, resourceCollector);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return resourceCollector;
    }

    public static void processXmlFilesForIds(String resourceDirectory, List<RDotTxtEntry> references, com.meituan.robust.tools.aapt.AaptResourceCollector resourceCollector) throws Exception {
        List<String> xmlFullFilenameList = com.meituan.robust.tools.aapt.FileUtil.findMatchFile(resourceDirectory, com.meituan.robust.tools.aapt.Constant.Symbol.DOT + com.meituan.robust.tools.aapt.Constant.File.XML);
        if (xmlFullFilenameList != null) {
            for (String xmlFullFilename : xmlFullFilenameList) {
                File xmlFile = new File(xmlFullFilename);
                String parentFullFilename = xmlFile.getParent();
                File parentFile = new File(parentFullFilename);
                if (isAValuesDirectory(parentFile.getName()) || parentFile.getName().startsWith("raw")) {
                    // Ignore files under values* directories and raw*.
                    continue;
                }
                processXmlFile(xmlFullFilename, references, resourceCollector);
            }
        }
    }

    private static void collectResources(String resourceDirectory, com.meituan.robust.tools.aapt.AaptResourceCollector resourceCollector) throws Exception {
        File resourceDirectoryFile = new File(resourceDirectory);
        File[] fileArray = resourceDirectoryFile.listFiles();
        if (fileArray != null) {
            for (File file : fileArray) {
                if (file.isDirectory()) {
                    String directoryName = file.getName();
                    if (directoryName.startsWith("values")) {
                        if (!isAValuesDirectory(directoryName)) {
                            throw new AaptUtilException("'" + directoryName + "' is not a valid values directory.");
                        }
                        processValues(file.getAbsolutePath(), resourceCollector);
                    } else {
                        processFileNamesInDirectory(file.getAbsolutePath(), resourceCollector);
                    }
                }
            }
        }
    }

    /**
     * is a value directory
     *
     * @param directoryName
     * @return boolean
     */
    public static boolean isAValuesDirectory(String directoryName) {
        if (directoryName == null) {
            throw new NullPointerException("directoryName can not be null");
        }
        return directoryName.equals("values") || directoryName.startsWith("values-");
    }

    public static void processFileNamesInDirectory(String resourceDirectory, com.meituan.robust.tools.aapt.AaptResourceCollector resourceCollector) throws IOException {
        File resourceDirectoryFile = new File(resourceDirectory);
        String directoryName = resourceDirectoryFile.getName();
        int dashIndex = directoryName.indexOf('-');
        if (dashIndex != -1) {
            directoryName = directoryName.substring(0, dashIndex);
        }

        if (!RESOURCE_TYPES.containsKey(directoryName)) {
            throw new AaptUtilException(resourceDirectoryFile.getAbsolutePath() + " is not a valid resource sub-directory.");
        }
        File[] fileArray = resourceDirectoryFile.listFiles();
        if (fileArray != null) {
            for (File file : fileArray) {
                if (file.isHidden()) {
                    continue;
                }
                String filename = file.getName();
                int dotIndex = filename.indexOf('.');
                String resourceName = dotIndex != -1 ? filename.substring(0, dotIndex) : filename;

                RDotTxtEntry.RType rType = RESOURCE_TYPES.get(directoryName);
                resourceCollector.addIntResourceIfNotPresent(rType, resourceName);
                com.meituan.robust.tools.aapt.ResourceDirectory resourceDirectoryBean = new com.meituan.robust.tools.aapt.ResourceDirectory(file.getParentFile().getName(), file.getAbsolutePath());
                resourceCollector.addRTypeResourceName(rType, resourceName, null, resourceDirectoryBean);
            }
        }
    }

    public static void processValues(String resourceDirectory, com.meituan.robust.tools.aapt.AaptResourceCollector resourceCollector) throws Exception {
        File resourceDirectoryFile = new File(resourceDirectory);
        File[] fileArray = resourceDirectoryFile.listFiles();
        if (fileArray != null) {
            for (File file : fileArray) {
                if (file.isHidden()) {
                    continue;
                }
                if (!file.isFile()) {
                    // warning
                    continue;
                }
                processValuesFile(file.getAbsolutePath(), resourceCollector);
            }
        }
    }

    public static void processValuesFile(String valuesFullFilename, com.meituan.robust.tools.aapt.AaptResourceCollector resourceCollector) throws Exception {
        Document document = JavaXmlUtil.parse(valuesFullFilename);
        String directoryName = new File(valuesFullFilename).getParentFile().getName();
        Element root = document.getDocumentElement();

        for (Node node = root.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            String resourceType = node.getNodeName();
            if (resourceType.equals(ITEM_TAG)) {
                resourceType = node.getAttributes().getNamedItem("type").getNodeValue();
                if (resourceType.equals("id")) {
                    resourceCollector.addIgnoreId(node.getAttributes().getNamedItem("name").getNodeValue());
                }
            }

            if (IGNORED_TAGS.contains(resourceType)) {
                continue;
            }

            if (!RESOURCE_TYPES.containsKey(resourceType)) {
                throw new AaptUtilException("Invalid resource type '<" + resourceType + ">' in '" + valuesFullFilename + "'.");
            }

            RDotTxtEntry.RType rType = RESOURCE_TYPES.get(resourceType);
            String resourceValue = null;
            switch (rType) {
                case STRING:
                case COLOR:
                case DIMEN:
                case DRAWABLE:
                case BOOL:
                case INTEGER:
                    resourceValue = node.getTextContent().trim();
                    break;
                case ARRAY://has sub item
                case PLURALS://has sub item
                case STYLE://has sub item
                case STYLEABLE://has sub item
                    resourceValue = subNodeToString(node);
                    break;
                case FRACTION://no sub item
                    resourceValue = nodeToString(node, true);
                    break;
                case ATTR://no sub item
                    resourceValue = nodeToString(node, true);
                    break;
            }
            try {
                addToResourceCollector(resourceCollector, new com.meituan.robust.tools.aapt.ResourceDirectory(directoryName, valuesFullFilename), node, rType, resourceValue);
            } catch (Exception e) {
                throw new AaptUtilException(e.getMessage() + ",Process file error:" + valuesFullFilename, e);
            }
        }
    }

    public static void processXmlFile(String xmlFullFilename, List<RDotTxtEntry> references, com.meituan.robust.tools.aapt.AaptResourceCollector resourceCollector) throws IOException, XPathExpressionException {
        Document document = JavaXmlUtil.parse(xmlFullFilename);
        NodeList nodesWithIds = (NodeList) ANDROID_ID_DEFINITION.evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < nodesWithIds.getLength(); i++) {
            String resourceName = nodesWithIds.item(i).getNodeValue();
            if (!resourceName.startsWith(ID_DEFINITION_PREFIX)) {
                throw new AaptUtilException("Invalid definition of a resource: '" + resourceName + "'");
            }

            resourceCollector.addIntResourceIfNotPresent(RDotTxtEntry.RType.ID, resourceName.substring(ID_DEFINITION_PREFIX.length()));
        }

        NodeList nodesUsingIds = (NodeList) ANDROID_ID_USAGE.evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < nodesUsingIds.getLength(); i++) {
            String resourceName = nodesUsingIds.item(i).getNodeValue();
            int slashPosition = resourceName.indexOf('/');
            if (slashPosition < 0) {
                continue;
            }
            String rawRType = resourceName.substring(1, slashPosition);
            String name = resourceName.substring(slashPosition + 1);

            if (name.startsWith("android:")) {
                continue;
            }
            if (!RESOURCE_TYPES.containsKey(rawRType)) {
                throw new AaptUtilException("Invalid reference '" + resourceName + "' in '" + xmlFullFilename + "'");
            }
            RDotTxtEntry.RType rType = RESOURCE_TYPES.get(rawRType);

//if(!resourceCollector.isContainResource(rType, IdType.INT, sanitizeName(resourceCollector, name))){
//throw new AaptUtilException("Not found reference '" + resourceName + "' in '" + xmlFullFilename + "'");
//}
            references.add(new FakeRDotTxtEntry(RDotTxtEntry.IdType.INT, rType, sanitizeName(rType, resourceCollector, name)));
        }
    }

    private static void addToResourceCollector(com.meituan.robust.tools.aapt.AaptResourceCollector resourceCollector, com.meituan.robust.tools.aapt.ResourceDirectory resourceDirectory, Node node, RDotTxtEntry.RType rType, String resourceValue) {
        String resourceName = sanitizeName(rType, resourceCollector, extractNameAttribute(node));
        resourceCollector.addRTypeResourceName(rType, resourceName, resourceValue, resourceDirectory);
        if (rType.equals(RDotTxtEntry.RType.STYLEABLE)) {

            int count = 0;
            for (Node attrNode = node.getFirstChild(); attrNode != null; attrNode = attrNode.getNextSibling()) {
                if (attrNode.getNodeType() != Node.ELEMENT_NODE || !attrNode.getNodeName().equals("attr")) {
                    continue;
                }

                String rawAttrName = extractNameAttribute(attrNode);
                String attrName = sanitizeName(rType, resourceCollector, rawAttrName);
                resourceCollector.addResource(RDotTxtEntry.RType.STYLEABLE, RDotTxtEntry.IdType.INT, String.format("%s_%s", resourceName, attrName), Integer.toString(count++));

                if (!rawAttrName.startsWith("android:")) {
                    resourceCollector.addIntResourceIfNotPresent(RDotTxtEntry.RType.ATTR, attrName);
                    resourceCollector.addRTypeResourceName(RDotTxtEntry.RType.ATTR, rawAttrName, nodeToString(attrNode, true), resourceDirectory);
                }
            }

            resourceCollector.addIntArrayResourceIfNotPresent(rType, resourceName, count);
        } else {
            resourceCollector.addIntResourceIfNotPresent(rType, resourceName);
        }
    }

    private static String sanitizeName(RDotTxtEntry.RType rType, com.meituan.robust.tools.aapt.AaptResourceCollector resourceCollector, String rawName) {
        String sanitizeName = rawName.replaceAll("[.:]", "_");
        resourceCollector.putSanitizeName(rType, sanitizeName, rawName);
        return sanitizeName;
    }

    private static String extractNameAttribute(Node node) {
        return node.getAttributes().getNamedItem("name").getNodeValue();
    }

    /**
     * merge package r type resource map
     *
     * @param packageRTypeResourceMapList
     * @return Map<String, Map<RType,Set<RDotTxtEntry>>>
     */
    public static Map<String, Map<RDotTxtEntry.RType, Set<RDotTxtEntry>>> mergePackageRTypeResourceMap(List<PackageRTypeResourceMap> packageRTypeResourceMapList) {
        Map<String, Map<RDotTxtEntry.RType, Set<RDotTxtEntry>>> packageRTypeResourceMergeMap = new HashMap<String, Map<RDotTxtEntry.RType, Set<RDotTxtEntry>>>();
        Map<String, com.meituan.robust.tools.aapt.AaptResourceCollector> aaptResourceCollectorMap = new HashMap<String, com.meituan.robust.tools.aapt.AaptResourceCollector>();
        for (PackageRTypeResourceMap packageRTypeResourceMap : packageRTypeResourceMapList) {
            String packageName = packageRTypeResourceMap.packageName;
            Map<RDotTxtEntry.RType, Set<RDotTxtEntry>> rTypeResourceMap = packageRTypeResourceMap.rTypeResourceMap;
            com.meituan.robust.tools.aapt.AaptResourceCollector aaptResourceCollector = null;
            if (aaptResourceCollectorMap.containsKey(packageName)) {
                aaptResourceCollector = aaptResourceCollectorMap.get(packageName);
            } else {
                aaptResourceCollector = new com.meituan.robust.tools.aapt.AaptResourceCollector();
                aaptResourceCollectorMap.put(packageName, aaptResourceCollector);
            }
            Iterator<Entry<RDotTxtEntry.RType, Set<RDotTxtEntry>>> iterator = rTypeResourceMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<RDotTxtEntry.RType, Set<RDotTxtEntry>> entry = iterator.next();
                RDotTxtEntry.RType rType = entry.getKey();
                Set<RDotTxtEntry> rDotTxtEntrySet = entry.getValue();
                for (RDotTxtEntry rDotTxtEntry : rDotTxtEntrySet) {
                    if (rDotTxtEntry.idType.equals(RDotTxtEntry.IdType.INT)) {
                        aaptResourceCollector.addIntResourceIfNotPresent(rType, rDotTxtEntry.name);
                    } else if (rDotTxtEntry.idType.equals(RDotTxtEntry.IdType.INT_ARRAY)) {
                        aaptResourceCollector.addResource(rType, rDotTxtEntry.idType, rDotTxtEntry.name, rDotTxtEntry.idValue);
                    }
                }
            }
        }
        Iterator<Entry<String, com.meituan.robust.tools.aapt.AaptResourceCollector>> iterator = aaptResourceCollectorMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, com.meituan.robust.tools.aapt.AaptResourceCollector> entry = iterator.next();
            packageRTypeResourceMergeMap.put(entry.getKey(), entry.getValue().getRTypeResourceMap());
        }
        return packageRTypeResourceMergeMap;
    }

    /**
     * write R.java
     *
     * @param outputDirectory
     * @param packageName
     * @param rTypeResourceMap
     * @param isFinal
     */
    public static void writeRJava(String outputDirectory, String packageName, Map<RDotTxtEntry.RType, Set<RDotTxtEntry>> rTypeResourceMap, boolean isFinal) {
        String outputFullFilename = new File(outputDirectory).getAbsolutePath() + com.meituan.robust.tools.aapt.Constant.Symbol.SLASH_LEFT + (packageName.replace(com.meituan.robust.tools.aapt.Constant.Symbol.DOT, com.meituan.robust.tools.aapt.Constant.Symbol.SLASH_LEFT) + com.meituan.robust.tools.aapt.Constant.Symbol.SLASH_LEFT + "R" + com.meituan.robust.tools.aapt.Constant.Symbol.DOT + com.meituan.robust.tools.aapt.Constant.File.JAVA);
        com.meituan.robust.tools.aapt.FileUtil.createFile(outputFullFilename);
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new FileOutputStream(outputFullFilename));
            writer.format("package %s;\n\n", packageName);
            writer.println("public final class R {\n");
            for (RDotTxtEntry.RType rType : rTypeResourceMap.keySet()) {
                // Now start the block for the new type.
                writer.format("  public static final class %s {\n", rType.toString());
                for (RDotTxtEntry rDotTxtEntry : rTypeResourceMap.get(rType)) {
                    // Write out the resource.
                    // Write as an int.
                    writer.format("    public static%s%s %s=%s;\n", isFinal ? " final " : " ", rDotTxtEntry.idType, rDotTxtEntry.name, rDotTxtEntry.idValue);
                }
                writer.println("  }\n");
            }
            // Close the class definition.
            writer.println("}");
        } catch (Exception e) {
            throw new AaptUtilException(e);
        } finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }
    }

    /**
     * write R.java
     *
     * @param outputDirectory
     * @param packageRTypeResourceMap
     * @param isFinal
     * @throws IOException
     */
    public static void writeRJava(String outputDirectory, Map<String, Map<RDotTxtEntry.RType, Set<RDotTxtEntry>>> packageRTypeResourceMap, boolean isFinal) {
        for (String packageName : packageRTypeResourceMap.keySet()) {
            Map<RDotTxtEntry.RType, Set<RDotTxtEntry>> rTypeResourceMap = packageRTypeResourceMap.get(packageName);
            writeRJava(outputDirectory, packageName, rTypeResourceMap, isFinal);
        }
    }

    private static String subNodeToString(Node node) {
        StringBuilder stringBuilder = new StringBuilder();
        if (node != null) {
            NodeList nodeList = node.getChildNodes();
            stringBuilder.append(nodeToString(node, false));
            stringBuilder.append(com.meituan.robust.tools.aapt.StringUtil.CRLF_STRING);
            int nodeListLength = nodeList.getLength();
            for (int i = 0; i < nodeListLength; i++) {
                Node childNode = nodeList.item(i);
                if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                stringBuilder.append(nodeToString(childNode, true));
                stringBuilder.append(com.meituan.robust.tools.aapt.StringUtil.CRLF_STRING);
            }
            if (stringBuilder.length() > com.meituan.robust.tools.aapt.StringUtil.CRLF_STRING.length()) {
                stringBuilder.delete(stringBuilder.length() - com.meituan.robust.tools.aapt.StringUtil.CRLF_STRING.length(), stringBuilder.length());
            }
        }
        return stringBuilder.toString();
    }

    private static String nodeToString(Node node, boolean isNoChild) {
        StringBuilder stringBuilder = new StringBuilder();
        if (node != null) {
            stringBuilder.append(node.getNodeName());
            NamedNodeMap namedNodeMap = node.getAttributes();
            stringBuilder.append(com.meituan.robust.tools.aapt.Constant.Symbol.MIDDLE_BRACKET_LEFT);
            int namedNodeMapLength = namedNodeMap.getLength();
            for (int j = 0; j < namedNodeMapLength; j++) {
                Node attributeNode = namedNodeMap.item(j);
                stringBuilder.append(com.meituan.robust.tools.aapt.Constant.Symbol.AT + attributeNode.getNodeName() + com.meituan.robust.tools.aapt.Constant.Symbol.EQUAL + attributeNode.getNodeValue());
                if (j < namedNodeMapLength - 1) {
                    stringBuilder.append(com.meituan.robust.tools.aapt.Constant.Symbol.COMMA);
                }
            }
            stringBuilder.append(com.meituan.robust.tools.aapt.Constant.Symbol.MIDDLE_BRACKET_RIGHT);
            String value = com.meituan.robust.tools.aapt.StringUtil.nullToBlank(isNoChild ? node.getTextContent() : node.getNodeValue()).trim();
            if (com.meituan.robust.tools.aapt.StringUtil.isNotBlank(value)) {
                stringBuilder.append(com.meituan.robust.tools.aapt.Constant.Symbol.EQUAL + value);
            }
        }
        return stringBuilder.toString();
    }

    public static class PackageRTypeResourceMap {
        private String                                                      packageName      = null;
        private Map<RDotTxtEntry.RType, Set<RDotTxtEntry>> rTypeResourceMap = null;

        public PackageRTypeResourceMap(String packageName, Map<RDotTxtEntry.RType, Set<RDotTxtEntry>> rTypeResourceMap) {
            this.packageName = packageName;
            this.rTypeResourceMap = rTypeResourceMap;
        }
    }

    public static class AaptUtilException extends RuntimeException {
        private static final long serialVersionUID = 1702278793911780809L;

        public AaptUtilException(String message) {
            super(message);
        }

        public AaptUtilException(Throwable cause) {
            super(cause);
        }

        public AaptUtilException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
