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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import nl.npcf.eav.EAVStore;
import nl.npcf.eav.exception.EAVException;
import nl.npcf.eav.exception.EAVPathException;
import nl.npcf.eav.meta.EAVPath;
import nl.npcf.eav.meta.EAVSchema;
import org.hibernate.annotations.Index;

import java.security.Principal;
import java.util.Date;

/**
 * This is the superclass of all the value classes, and holds the relevant
 * extra information beyond the value itself.
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class EAVValue implements EAVStore.Value, Comparable<EAVValue> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn
    private EAVEntity entity;

    @Column(length = 80)
    @Index(name = "eavpathindex")
    private String pathNodes;

    @Column(length = 24)
    private String pathIndexes;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    @Index(name = "eavcreatedonindex")
    private Date createdOn;

    @Column(length = 24)
    @Index(name = "eavcreatedbyindex")
    private String createdBy;

    @Transient
    EAVPath eavPath;

    protected EAVValue() {
    }

    protected EAVValue(EAVEntity entity, EAVPath path, Principal createdBy) throws EAVPathException {
        this.entity = entity;
        this.eavPath = path;
        this.pathNodes = eavPath.getNodeString();
        this.pathIndexes = eavPath.getIndexString();
        if (createdBy != null) {
            this.createdBy = createdBy.getName();
        }
        else {
            this.createdBy = "nobody";
        }
        this.createdOn = new Date();
    }

    public EAVEntity getEntity() {
        return entity;
    }

    public EAVStore.Attribute getAttribute() throws EAVException {
        return entity.getSchema().getAttribute(getPath(), false);
    }

    public String getPathNodes() {
        return pathNodes;
    }

    public String getPathString() {
        return getPath().toString();
    }

    public EAVPath getPath() {
        if (eavPath == null) {
            eavPath = new EAVPath(pathNodes, pathIndexes);
        }
        return eavPath;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public int compareTo(EAVValue otherValue) {
        return getPath().compareTo(otherValue.getPath());
    }

    public String toTableRow(String baseUrl) throws EAVException {
        return ((EAVSchema)entity.getSchema()).toTableRow(baseUrl, this);
    }

    public abstract Object getValue();

    public abstract void setValue(Object value);

    public void setEntity(EAVEntity entity) {
        this.entity = entity;
    }
}
