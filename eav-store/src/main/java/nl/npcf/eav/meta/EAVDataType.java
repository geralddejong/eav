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

import nl.npcf.eav.data.EAVEntity;
import nl.npcf.eav.data.EAVIdentifier;
import nl.npcf.eav.data.EAVInteger;
import nl.npcf.eav.data.EAVString;
import nl.npcf.eav.data.EAVText;
import nl.npcf.eav.data.EAVTimestamp;
import nl.npcf.eav.data.EAVValue;
import nl.npcf.eav.exception.EAVValidationException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.security.Principal;
import java.util.Date;

/**
 * This is an enumeration of the datatypes handled by the EAV system, and it handles
 * a number of central functions.
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public enum EAVDataType {

    IDENTIFIER(String.class, EAVIdentifier.class),
    STRING(String.class, EAVString.class),
    TEXT(String.class, EAVText.class),
    INTEGER(BigInteger.class, EAVInteger.class),
    DATETIME(Date.class, EAVTimestamp.class);

    private Class<?> typeClass, entityClass;
    private String entityClassName;

    EAVDataType(Class typeClass, Class entityClass) {
        this.typeClass = typeClass;
        this.entityClass = entityClass;
        String className = entityClass.getName();
        this.entityClassName = className.substring(className.lastIndexOf(".") + 1);
    }

    public void typeCheck(EAVAttribute attribute, Object value) throws EAVValidationException {
        if (value == null) {
            throw EAVValidationException.badType(attribute, typeClass, null);
        }
        if (value.getClass() != typeClass) {
            throw EAVValidationException.badType(attribute, typeClass, value.getClass());
        }
    }

    public EAVValue createValue(EAVEntity entity, EAVPath path, Object value, Principal createdBy) {
        try {
            Constructor constructor = entityClass.getConstructor(EAVEntity.class, EAVPath.class, Principal.class, typeClass);
            return (EAVValue) constructor.newInstance(entity, path, createdBy, value);
        }
        catch (NoSuchMethodException e) {
            throw new RuntimeException("Cannot find constructor for " + this);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException("Not allowed to construct " + this);
        }
        catch (InvocationTargetException e) {
            throw new RuntimeException("Cannot invoke constructor for " + this);
        }
        catch (InstantiationException e) {
            throw new RuntimeException("Cannot instantiate for " + this);
        }
    }

    public String getEntityName() {
        return entityClassName;
    }
}
