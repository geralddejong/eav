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
import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import nl.npcf.eav.exception.EAVException;
import nl.npcf.eav.meta.EAVSchema;
import nl.npcf.eav.xstream.EAVXStream;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.Date;

/**
 * This entity records a secure hash of the contents of the XML file which describes
 * the metadata of the EAV store.
 *
 * A transient one of these can be created and then compared to ones originating from the database
 * with equals() to see if it matches.
 * 
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

@Entity
public class EAVMetaVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(length = 64)
    private String hash;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

    @Column(length = 24)
    private String createdBy;

    @Lob
    private String xml;

    @Transient
    private EAVSchema schema;

    public EAVMetaVersion() {
    }

    public EAVMetaVersion(String xml, Principal principal) {
        this.createdBy = principal.getName();
        this.createdOn = new Date();
        this.xml = xml;
        this.hash = createHash(xml);
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public String getHash() {
        return hash;
    }

    public String getXml() {
        return xml;
    }

    public synchronized EAVSchema getSchema() throws EAVException {
        if (schema == null) {
            if (xml == null) {
                throw new EAVException("XML must be defined");
            }
            EAVXStream stream = new EAVXStream();
            schema = stream.fromXML(xml);
        }
        return schema;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EAVMetaVersion that = (EAVMetaVersion) o;
        return hash.equals(that.hash);
    }

    public int hashCode() {
        return hash.hashCode();
    }

    public String toString() {
        return "Hash("+hash+")";
    }

    private String createHash(String string) {
        MessageDigest digest = createDigest();
        try {
            digest.update(string.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Problem creating hash from string", e);
        }
        return createHashString(digest);
    }

//    private String createHash(InputStream inputStream) {
//        MessageDigest digest = createDigest();
//        try {
//            int bytes;
//            byte[] buffer = new byte[2048];
//            while ((bytes = inputStream.read(buffer)) > 0) {
//                digest.update(buffer, 0, bytes);
//            }
//            inputStream.close();
//        }
//        catch (IOException e) {
//            throw new RuntimeException("Problem reading metadata file", e);
//        }
//        return createHashString(digest);
//    }

    private MessageDigest createDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Wholly unexpected!",e);
        }
    }

    private String createHashString(MessageDigest digest) {
        byte[] raw = digest.digest();
        byte[] hex = new byte[2 * raw.length];
        int index = 0;
        for (byte b : raw) {
            int v = b & 0xFF;
            hex[index++] = HEX_CHAR_TABLE[v >>> 4];
            hex[index++] = HEX_CHAR_TABLE[v & 0xF];
        }
        try {
            return new String(hex, "ASCII");
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Wholly unexpected!",e);
        }
    }

    private static final byte[] HEX_CHAR_TABLE = {
            (byte) '0', (byte) '1', (byte) '2', (byte) '3',
            (byte) '4', (byte) '5', (byte) '6', (byte) '7',
            (byte) '8', (byte) '9', (byte) 'A', (byte) 'B',
            (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F'
    };
}