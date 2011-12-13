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

package nl.npcf.eav.data;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import nl.npcf.eav.EAVStore;
import nl.npcf.eav.exception.EAVException;
import nl.npcf.eav.exception.EAVPathException;
import nl.npcf.eav.exception.EAVValidationException;
import nl.npcf.eav.meta.EAVAttribute;
import nl.npcf.eav.meta.EAVPath;
import nl.npcf.eav.meta.EAVSchema;
import org.hibernate.annotations.Index;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * The entity is the unit in an EAV, and here is where attribute values can be
 * added, or set.
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

@Entity
public class EAVEntity implements EAVStore.Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(length = 24, unique = true)
    @Index(name = "eaventitykeyindex")
    private String key;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "entity")
    @JoinColumn(name = "entity_id", nullable = false, updatable = false)
    private List<EAVValue> values = new ArrayList<EAVValue>();

    @Transient
    private EAVSchema schema;

    public EAVEntity() {
    }

    public EAVEntity(EAVSchema schema, String key) {
        this.schema = schema;
        this.key = key;
    }

    public boolean isTransient() {
        return id == null;
    }

    public String getKey() {
        return key;
    }

    public EAVStore.Value getValue(EAVStore.Path path) throws EAVPathException {
        schema.getAttribute(path, true); // checking indexing
        for (EAVValue value : values) {
            if (value.getPath().equals(path)) {
                return value;
            }
        }
        return null;
    }

    public List<EAVValue> getValues(EAVStore.Path path, boolean inclusive) {
        List<EAVValue> found = new ArrayList<EAVValue>();
        for (EAVValue value : values) {
            if (path.contains(value.getPath(), inclusive)) {
                found.add(value);
            }
        }
        return found;
    }

    public void sortValues() {
        Collections.sort(values);
    }

    public void setTransientValue(EAVStore.Path path, String valueString, Principal createdBy) throws EAVValidationException {
        if (id != null) {
            throw new RuntimeException("Entity is not transient");
        }
        EAVAttribute attribute = (EAVAttribute) schema.getAttribute(path,true);
        Iterator<EAVValue> valueIterator = values.iterator();
        while (valueIterator.hasNext()) { // clear any existing value
            EAVValue existing = valueIterator.next();
            if (existing.getPath().equals(path)) {
                valueIterator.remove();
            }
        }
        if (valueString != null) {
            Object value = attribute.stringToValue(valueString);
            EAVValue eavValue = attribute.instantiate(this, (EAVPath)path, value, createdBy);
            values.add(eavValue);
        }
    }

    public void validate() throws EAVValidationException {
        Map<String, Set<String>> parentChildren = new TreeMap<String, Set<String>>();
        parentChildren.put("",new TreeSet<String>());
        for (EAVStore.Attribute required : schema.getRequiredAttributes()) { // gather parent paths of required values
            for (EAVValue value : values) { // find a value for this path
                if (value.getPathNodes().equals(required.getPath().getNodeString())) {
                    recordAncestors(parentChildren, value.getPath());
                }
            }
        }
        for (Map.Entry<String, Set<String>> existing : parentChildren.entrySet()) {
            EAVAttribute attribute = getAttribute(existing.getKey());
            for (EAVAttribute sub : attribute.getAttributes()) { // check through subattributes for required ones
                if (sub.isRequired()) {
                    if (!existing.getValue().contains(sub.getPath().getNodeString())) {
                        throw EAVValidationException.missingAttribute(sub);
                    }
                }
            }
        }
    }

    public String toHtml(String baseUrl, EAVStore.Path path) throws EAVException {
        return schema.toHtml(this, baseUrl, (EAVPath)path);
    }

    private void recordAncestors(Map<String, Set<String>> parentChildren, EAVPath childPath) {
        EAVPath ancestorPath = childPath.getParent();
        while (!ancestorPath.getElements().isEmpty()) {
            Set<String> children = parentChildren.get(ancestorPath.toString());
            if (children == null) {
                parentChildren.put(ancestorPath.toString(), children = new TreeSet<String>());
            }
            children.add(childPath.getNodeString());
            childPath = ancestorPath;
            ancestorPath = childPath.getParent();
        }
        parentChildren.get("").add(childPath.getNodeString());
    }

    private EAVAttribute getAttribute(String path) {
        try {
            return (EAVAttribute) schema.getAttribute(new EAVPath(path), true);
        }
        catch (EAVPathException e) {
            throw new RuntimeException("Path should have been okay");
        }
    }

    public String toString() {
        StringBuilder out = new StringBuilder("Entity(" + key + ") {\n");
        for (EAVValue value : values) {
            out.append("   ");
            out.append(value.toString());
            out.append("\n");
        }
        out.append("}\n");
        return out.toString();
    }

    // the privates

    public void setSchema(EAVStore.Schema schema) {
        this.schema = (EAVSchema)schema;
    }

    public EAVStore.Schema getSchema() {
        return schema;
    }


    public List<EAVValue> getValues() {
        return values;
    }

    public SortedSet<EAVPath> getPathSet(EAVPath path) {
        SortedSet<EAVPath> pathSet = new TreeSet<EAVPath>();
        String unindexedPath = path.toString();
        for (EAVValue value : values) {
            if (value.getPath().toString().startsWith(unindexedPath)) {
                EAVPath found = value.getPath();
                while (found.size() > path.size()) {
                    found = found.getParent();
                }
                pathSet.add(found);
            }
        }
        return pathSet;
    }

    public EAVPath getNewPath(EAVPath path, SortedSet<EAVPath> pathSet) throws EAVPathException {
        EAVPath newPath = EAVPath.copy(path);
        if (pathSet.isEmpty()) {
            newPath.getLastElement().setIndex(0);
        }
        else {
            EAVPath last = pathSet.last();
            newPath.getLastElement().setIndex(last.getLastElement().getIndex()+1);
        }
        newPath = (EAVPath)schema.createPath(newPath.toString(),true);
        return newPath;
    }
}


/*
    public EAVValue getValueTree() throws EAVException {
        return getValueTree(schema.getRootAttribute(), 0);
    }

    private EAVValue getValueTree(EAVStore.Attribute storeAttribute, int depth) throws EAVException {
        EAVAttribute attribute = (EAVAttribute) storeAttribute;
        if (attribute.isAggregate()) {
            if (attribute.isMultiple()) {
                return getAggregateMultiple(attribute, depth);
            }
            else {
                return getAggregateSingular(attribute, depth);
            }
        }
        else {
            if (attribute.isMultiple()) {
                return getMultiple(attribute);
            }
            else {
                return getSinglular(attribute);
            }
        }
    }

    private EAVValue getSinglular(EAVAttribute attribute) throws EAVException {
        List<EAVValue> values = getValues(attribute.getPath());
        if (values.isEmpty()) {
            return null;
        }
        else if (values.size() == 1) {
            return values.get(0);
        }
        else {
            throw new EAVException("Expected zero or one values");
        }
    }

    private EAVAggregate getMultiple(EAVAttribute attribute) throws EAVException {
        List<EAVValue> values = getValues(attribute.getPath());
        if (values.isEmpty()) {
            return null;
        }
        EAVAggregate aggregate = new EAVAggregate(this, attribute.getEAVPath());
        aggregate.addAll(values);
        return aggregate;
    }

    private EAVValue getAggregateSingular(EAVAttribute attribute, int depth) throws EAVException {
        EAVAggregate aggregate = new EAVAggregate(this, attribute.getEAVPath());
        for (EAVStore.Attribute subAttribute : attribute.getSubAttributes()) {
            aggregate.add(getValueTree(subAttribute, depth+1));
        }
        return aggregate;
    }

    private EAVValue getAggregateMultiple(EAVAttribute attribute, int depth) throws EAVException {
        Map<Integer, List<EAVValue>> map = new TreeMap<Integer, List<EAVValue>>();
        for (EAVValue value : values) {
            if (attribute.getPath().contains(value.getPath())) {
                EAVPath.Element element = value.getPath().getElements().get(depth);
                List<EAVValue> mapValues = map.get(element.getIndex());
                if (mapValues == null) {
                    map.put(element.getIndex(), mapValues = new ArrayList<EAVValue>());
                }
                mapValues.add(value);
            }
        }
        EAVAggregate aggregate = new EAVAggregate(this, attribute.getEAVPath());
        for (Map.Entry<Integer, List<EAVValue>> entry : map.entrySet()) {
            aggregate.add(getValueTree(entry.getValue().get(0).getAttribute(), depth+1));
        }
        return aggregate;
    }

*/