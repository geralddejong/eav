/*
 * Copyright [2009] [Gerald de Jong]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.npcf.eav.meta;

import nl.npcf.eav.EAVStore;
import nl.npcf.eav.exception.EAVPathException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interpretation of paths into the attributes
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class EAVPath implements EAVStore.Path, Comparable<EAVPath> {
    private static final Pattern PATH = Pattern.compile("^([a-z]+([0-9][0-9])?)?(/([a-z]+([0-9][0-9])?))*$");
    private static final Pattern PATH_NODES = Pattern.compile("^([a-z]+)?(/([a-z]+))*$");
    private static final Pattern PATH_INDEXES = Pattern.compile("^([0-9][0-9])?(/(([0-9][0-9])?))*$");
    private static final Pattern NODE_NONINDEXED = Pattern.compile("^[a-z]+$");
    private static final Pattern NODE_INDEXED = Pattern.compile("(^[a-z]+)([0-9][0-9])$");
    private boolean validated, complete;
    private List<Element> elements = new ArrayList<Element>();

    public EAVPath(String pathString) throws EAVPathException {
        Matcher matcher = PATH.matcher(pathString);
        if (!matcher.matches()) {
            throw new EAVPathException(pathString, "bad format");
        }
        for (String part : pathString.split(EAVStore.PATH_SEPARATOR)) {
            if (part.length() > 0) {
                elements.add(new Element(part));
            }
        }
    }

    public EAVPath(String pathNodes, String pathIndexes) {
        Matcher nodeMatcher = PATH_NODES.matcher(pathNodes);
        Matcher indexMatcher = PATH_INDEXES.matcher(pathIndexes);
        if (!(nodeMatcher.matches() && indexMatcher.matches())) {
            throw new RuntimeException("Expected regular expression match: " + pathNodes + " " + pathIndexes);
        }
        String[] nodeParts = split(pathNodes);
        String[] indexParts = split(pathIndexes);
        if (nodeParts.length != indexParts.length) {
            throw new RuntimeException("Expected count match: " + pathNodes + " " + pathIndexes);
        }
        for (int walk = 0; walk < nodeParts.length; walk++) {
            elements.add(new Element(nodeParts[walk], indexParts[walk]));
        }
    }

    public static EAVPath copy(EAVPath path) {
        try {
            return new EAVPath(path.toString());
        }
        catch (EAVPathException e) {
            throw new RuntimeException("Path should be copyable" + e);
        }
    }

    public EAVPath() {
    }

    public String getNodeString() {
        return toString(false);
    }

    public boolean contains(EAVStore.Path path, boolean inclusive) {
        EAVPath otherPath = (EAVPath) path;
        Iterator<Element> otherElementWalk = otherPath.elements.iterator();
        for (Element element : elements) {
            if (!otherElementWalk.hasNext()) {
                return false;
            }
            Element other = otherElementWalk.next();
            if (!element.contains(other)) {
                return false;
            }
        }
        return inclusive || !otherElementWalk.hasNext();
    }

    public boolean isComplete() {
        if (!validated) {
            throw new RuntimeException("Path not validated!");
        }
        return complete;
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    void setComplete(boolean complete) {
        this.complete = complete;
        this.validated = true;
    }

    public List<Element> getElements() {
        return elements;
    }

    public Element getLastElement() {
        if (elements.isEmpty()) {
            return null;
        }
        return elements.get(elements.size() - 1);
    }

    public String getIndexString() {
        StringBuilder out = new StringBuilder();
        int count = elements.size();
        for (Element element : elements) {
            if (element.getIndex() >= 0) {
                out.append(formatIndex(element.getIndex()));
            }
            if (--count > 0) {
                out.append(EAVStore.PATH_SEPARATOR);
            }
        }
        return out.toString();
    }

    public String getParentPathNodes() {
        String string = toString(true);
        int slash = string.lastIndexOf(EAVStore.PATH_SEPARATOR);
        if (slash < 0) {
            return "";
        }
        else {
            return string.substring(0, slash);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EAVPath eavPath = (EAVPath) o;
        return elements.equals(eavPath.elements);
    }

    @Override
    public int hashCode() {
        return elements != null ? elements.hashCode() : 0;
    }

    private String toString(boolean withIndexes) {
        StringBuilder out = new StringBuilder();
        int count = elements.size();
        for (Element element : elements) {
            out.append(element.getNode());
            if (withIndexes && element.getIndex() >= 0) {
                out.append(formatIndex(element.getIndex()));
            }
            if (--count > 0) {
                out.append(EAVStore.PATH_SEPARATOR);
            }
        }
        return out.toString();
    }

    public String toLinks(String baseUrl, String baseUrlPrompt, String entityKey) {
        StringBuilder out = new StringBuilder();
        List<Element> nodes = new ArrayList<Element>(elements);
        if (entityKey != null) {
            nodes.add(0, new Element(entityKey));
        }
        out.append("<a href=\"").append(baseUrl).append("\">").append(baseUrlPrompt).append("</a> /\n");
        for (int walk = 0; walk < nodes.size() - 1; walk++) {
            out.append("<a href=\"");
            out.append(baseUrl);
            out.append(EAVStore.PATH_SEPARATOR);
            for (int build = 0; build <= walk; build++) {
                out.append(nodes.get(build));
                if (build < walk) {
                    out.append(EAVStore.PATH_SEPARATOR);
                }
            }
            out.append("\">");
            out.append(nodes.get(walk));
            out.append("</a> /\n");
        }
        if (!nodes.isEmpty()) {
            out.append(nodes.get(nodes.size() - 1));
        }
        return out.toString();
    }

    public String toString() {
        return toString(true);
    }

    public EAVPath getParent() {
        EAVPath parentPath = new EAVPath();
        parentPath.elements.addAll(elements);
        parentPath.elements.remove(parentPath.elements.size() - 1);
        return parentPath;
    }

    public int compareTo(EAVPath o) {
        return toString(true).compareTo(o.toString(true)); // could be more efficient
    }

    public int size() {
        return elements.size();
    }

    public EAVPath extend(String nodeName) {
        EAVPath extended = copy(this);
        extended.elements.add(new Element(nodeName));
        return extended;
    }

    public EAVPath after(EAVPath firstPart) throws EAVPathException {
        EAVPath path = new EAVPath();
        path.elements.addAll(elements);
        for (Element first : firstPart.elements) {
            if (first.equals(path.elements.get(0))) {
                path.elements.remove(0);
            }
            else {
                throw new EAVPathException(this.toString(), "Bad call to after() with ["+firstPart+"]");
            }
        }
        return path;
    }

    public class Element {
        private String node;
        private int index;

        public Element(String elementString) {
            if (NODE_NONINDEXED.matcher(elementString).matches()) {
                this.index = -1;
                this.node = elementString;
            }
            else {
                Matcher matcher = NODE_INDEXED.matcher(elementString);
                if (!matcher.matches()) {
                    throw new IllegalArgumentException("Path element invalid: " + elementString);
                }
                this.node = matcher.group(1);
                this.index = Integer.parseInt(matcher.group(2));
            }
        }

        public Element(String elementString, String indexString) {
            this.node = elementString;
            if (indexString.length() > 0) {
                this.index = Integer.parseInt(indexString);
            }
            else {
                this.index = -1;
            }
        }

        public int getIndex() {
            return index;
        }

        public boolean isIndexed() {
            return index >= 0;
        }

        public String getNode() {
            return node;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Element element = (Element) o;
            return index == element.index && !(node != null ? !node.equals(element.node) : element.node != null);
        }

        @Override
        public int hashCode() {
            int result = node != null ? node.hashCode() : 0;
            result = 31 * result + index;
            return result;
        }

        public boolean contains(Element other) {
            return node.equals(other.node) && (index < 0 || index == other.index);
        }

        public String toString() {
            if (isIndexed()) {
                return node + formatIndex(index);
            }
            else {
                return node;
            }
        }

        public void setIndex(int index) {
            if (isIndexed()) {
                throw new RuntimeException("Cannot set index, already indexed");
            }
            this.index = index;
        }
    }

    public static String formatIndex(int index) {
        if (index < 0) {
            return "";
        }
        else if (index >= 0 && index < 10) {
            return "0" + String.valueOf(index);
        }
        else if (index >= 10 && index < 100) {
            return String.valueOf(index);
        }
        else {
            throw new RuntimeException("Index out of range");
        }
    }

    private static String[] split(String s) {
        char sep = EAVStore.PATH_SEPARATOR.charAt(0);
        int count = 0;
        for (int walk = 0; walk < s.length(); walk++) {
            if (sep == s.charAt(walk)) {
                count++;
            }
        }
        String[] parts = new String[count + 1];
        if (count > 0) {
            int pos = 0;
            for (int walk = 0; walk <= count; walk++) {
                int nextPos = s.indexOf(sep, pos);
                if (nextPos >= 0) {
                    parts[walk] = s.substring(pos, nextPos);
                }
                else {
                    parts[walk] = s.substring(pos);
                }
                pos = nextPos + 1;
            }
        }
        else {
            parts[0] = s;
        }
        return parts;
    }
}
