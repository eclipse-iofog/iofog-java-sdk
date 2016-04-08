package com.iotracks.elements;

import com.iotracks.utils.ByteUtils;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

/**
 * IOMessage represent all message communication between ioFabric and Containers.
 */
public class IOMessage {

    private final short VERSION = 4;

    private final String INFO_FORMAT_BASE_64 = "base64";

    public static final String ID_FIELD_NAME = "id";
    public static final String TAG_FIELD_NAME = "tag";
    public static final String GROUP_ID_FIELD_NAME = "groupid";
    public static final String SEQUENCE_NUMBER_FIELD_NAME = "sequencenumber";
    public static final String SEQUENCE_TOTAL_FIELD_NAME = "sequencetotal";
    public static final String PRIORITY_FIELD_NAME = "priority";
    public static final String TIMESTAMP_FIELD_NAME = "timestamp";
    public static final String PUBLISHER_FIELD_NAME = "publisher";
    public static final String AUTH_ID_FIELD_NAME = "authid";
    public static final String AUTH_GROUP_FIELD_NAME = "authgroup";
    public static final String VERSION_FIELD_NAME = "version";
    public static final String CHAIN_POSITION_FIELD_NAME = "chainposition";
    public static final String HASH_FIELD_NAME = "hash";
    public static final String PREVIOUS_HASH_FIELD_NAME = "previoushash";
    public static final String NONCE_FIELD_NAME = "nonce";
    public static final String DIFFICULTY_TARGET_FIELD_NAME = "difficultytarget";
    public static final String INFO_TYPE_FIELD_NAME = "infotype";
    public static final String INFO_FORMAT_FIELD_NAME = "infoformat";
    public static final String CONTEXT_DATA_FIELD_NAME = "contextdata";
    public static final String CONTENT_DATA_FIELD_NAME = "contentdata";

    private String id; // required
    private String tag;
    private String groupId;
    private int sequenceNumber;
    private int sequenceTotal;
    private byte priority;
    private long timestamp; // required
    private String publisher; // required
    private String authId;
    private String authGroup;
    private short version = VERSION; // required
    private long chainPosition;
    private String hash;
    private String previousHash;
    private String nonce;
    private int difficultyTarget;
    private String infoType; // required
    private String infoFormat; // required
    private byte[] contextData;
    private byte[] contentData; // required

    public IOMessage(){
        super();
    }

    public IOMessage(byte[] rawBytes){
        super();
        convertBytesToMessage(null,rawBytes, 33);
    }

    public IOMessage(byte[] header, byte[] data) {
        super();
        convertBytesToMessage(header, data, 0);
    }

    public IOMessage(JsonObject json) {
        super();
        if (json.containsKey(ID_FIELD_NAME)){ setId(json.getString(ID_FIELD_NAME)); }
        if (json.containsKey(TAG_FIELD_NAME)){ setTag(json.getString(TAG_FIELD_NAME)); }
        if (json.containsKey(GROUP_ID_FIELD_NAME)){ setGroupId(json.getString(GROUP_ID_FIELD_NAME)); }
        if (json.containsKey(SEQUENCE_NUMBER_FIELD_NAME)){ setSequenceNumber(json.getInt(SEQUENCE_NUMBER_FIELD_NAME)); }
        if (json.containsKey(SEQUENCE_TOTAL_FIELD_NAME)){ setSequenceTotal(json.getInt(SEQUENCE_TOTAL_FIELD_NAME)); }
        if (json.containsKey(PRIORITY_FIELD_NAME)){ setPriority((byte) json.getInt(PRIORITY_FIELD_NAME)); }
        if (json.containsKey(TIMESTAMP_FIELD_NAME)){ setTimestamp(json.getJsonNumber(TIMESTAMP_FIELD_NAME).longValue()); }
        if (json.containsKey(PUBLISHER_FIELD_NAME)){ setPublisher(json.getString(PUBLISHER_FIELD_NAME)); }
        if (json.containsKey(AUTH_ID_FIELD_NAME)){ setAuthId(json.getString(AUTH_ID_FIELD_NAME)); }
        if (json.containsKey(AUTH_GROUP_FIELD_NAME)){ setAuthGroup(json.getString(AUTH_GROUP_FIELD_NAME)); }
        if (json.containsKey(CHAIN_POSITION_FIELD_NAME)){ setChainPosition(json.getJsonNumber(CHAIN_POSITION_FIELD_NAME).longValue()); }
        if (json.containsKey(HASH_FIELD_NAME)){ setHash(json.getString(HASH_FIELD_NAME)); }
        if (json.containsKey(PREVIOUS_HASH_FIELD_NAME)){ setPreviousHash(json.getString(PREVIOUS_HASH_FIELD_NAME)); }
        if (json.containsKey(NONCE_FIELD_NAME)){ setNonce(json.getString(NONCE_FIELD_NAME)); }
        if (json.containsKey(DIFFICULTY_TARGET_FIELD_NAME)){ setDifficultyTarget(json.getInt(DIFFICULTY_TARGET_FIELD_NAME));}
        if (json.containsKey(INFO_TYPE_FIELD_NAME)){ setInfoType(json.getString(INFO_TYPE_FIELD_NAME)); }
        if (json.containsKey(INFO_FORMAT_FIELD_NAME)){ setInfoFormat(json.getString(INFO_FORMAT_FIELD_NAME)); }
        if (json.containsKey(CONTEXT_DATA_FIELD_NAME) ){ setContextData(json.getString(CONTEXT_DATA_FIELD_NAME).getBytes()); }
        if (json.containsKey(CONTENT_DATA_FIELD_NAME) ){ setContentData(json.getString(CONTENT_DATA_FIELD_NAME).getBytes()); }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public Integer getSequenceTotal() {
        return sequenceTotal;
    }

    public void setSequenceTotal(Integer sequenceTotal) {
        this.sequenceTotal = sequenceTotal;
    }

    public byte getPriority() {
        return priority;
    }

    public void setPriority(byte priority) {
        this.priority = priority;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getAuthId() {
        return authId;
    }

    public void setAuthId(String authId) {
        this.authId = authId;
    }

    public String getAuthGroup() {
        return authGroup;
    }

    public void setAuthGroup(String authGroup) {
        this.authGroup = authGroup;
    }

    public short getVersion() {
        return version;
    }

    /*public void setVersion(short version) {
        this.version = version;
    }*/

    public long getChainPosition() {
        return chainPosition;
    }

    public void setChainPosition(long chainPosition) {
        this.chainPosition = chainPosition;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public int getDifficultyTarget() {
        return difficultyTarget;
    }

    public void setDifficultyTarget(int difficultyTarget) {
        this.difficultyTarget = difficultyTarget;
    }

    public String getInfoType() {
        return infoType;
    }

    public void setInfoType(String infoType) {
        this.infoType = infoType;
    }

    public String getInfoFormat() {
        return infoFormat;
    }

    public void setInfoFormat(String infoFormat) {
        this.infoFormat = infoFormat;
    }

    public byte[] getContextData() {
        return contextData;
    }

    public void setContextData(byte[] contextData) {
        this.contextData = contextData;
    }

    public byte[] getContentData() {
        return contentData;
    }

    public void setContentData(byte[] contentData) {
        this.contentData = contentData;
    }

    public JsonObject getJson(){
        return Json.createObjectBuilder().add(ID_FIELD_NAME, getId())
                .add(TAG_FIELD_NAME, getTag())
                .add(GROUP_ID_FIELD_NAME, getGroupId())
                .add(SEQUENCE_NUMBER_FIELD_NAME, getSequenceNumber())
                .add(SEQUENCE_TOTAL_FIELD_NAME, getSequenceTotal())
                .add(PRIORITY_FIELD_NAME, getPriority())
                .add(TIMESTAMP_FIELD_NAME, getTimestamp())
                .add(PUBLISHER_FIELD_NAME, getPublisher())
                .add(AUTH_ID_FIELD_NAME, getAuthId())
                .add(AUTH_GROUP_FIELD_NAME, getAuthGroup())
                .add(VERSION_FIELD_NAME, getVersion())
                .add(CHAIN_POSITION_FIELD_NAME, getChainPosition())
                .add(HASH_FIELD_NAME, getHash())
                .add(PREVIOUS_HASH_FIELD_NAME, getPreviousHash())
                .add(NONCE_FIELD_NAME, getNonce())
                .add(DIFFICULTY_TARGET_FIELD_NAME, getDifficultyTarget())
                .add(INFO_TYPE_FIELD_NAME, getInfoType())
                .add(INFO_FORMAT_FIELD_NAME, getInfoFormat())
                .add(CONTEXT_DATA_FIELD_NAME, getContextData()!=null ? "" : new String(getContextData()))
                .add(CONTENT_DATA_FIELD_NAME, getContentData()!=null ? "" : new String(getContentData()))
                .build();
    }

    public byte[] getBytes(){
        ByteArrayOutputStream headerBaos = new ByteArrayOutputStream();
        ByteArrayOutputStream dataBaos = new ByteArrayOutputStream();
        try {
            //version
            headerBaos.write(ByteUtils.shortToBytes(VERSION));

            // id
            int len = ByteUtils.getLength(getId());
            headerBaos.write((byte) (len & 0xff));
            if (len > 0)
                dataBaos.write(ByteUtils.stringToBytes(getId()));

            // tag
            len = ByteUtils.getLength(getTag());
            headerBaos.write(ByteUtils.shortToBytes((short) (len & 0xffff)));
            if (len > 0)
                dataBaos.write(ByteUtils.stringToBytes(getTag()));

            //groupid
            len = ByteUtils.getLength(getGroupId());
            headerBaos.write((byte) (len & 0xff));
            if (len > 0)
                dataBaos.write(ByteUtils.stringToBytes(getGroupId()));

            // seq no
            if (getSequenceNumber() == 0)
                headerBaos.write(0);
            else {
                dataBaos.write(ByteUtils.integerToBytes(getSequenceNumber()));
                headerBaos.write(4);
            }

            // seq total
            if (getSequenceTotal() == 0)
                headerBaos.write(0);
            else {
                dataBaos.write(ByteUtils.integerToBytes(getSequenceTotal()));
                headerBaos.write(4);
            }


            // priority
            if (getPriority() == 0)
                headerBaos.write(0);
            else {
                headerBaos.write(1);
                dataBaos.write(getPriority());
            }

            //timestamp
            if (getTimestamp() == 0)
                headerBaos.write(0);
            else {
                headerBaos.write(8);
                dataBaos.write(ByteUtils.longToBytes(getTimestamp()));
            }

            // publisher
            len = ByteUtils.getLength(getPublisher());
            headerBaos.write((byte) (len & 0xff));
            if (len > 0)
                dataBaos.write(ByteUtils.stringToBytes(getPublisher()));

            // authIdentifier
            len = ByteUtils.getLength(getAuthId());
            headerBaos.write(ByteUtils.shortToBytes((short) (len & 0xffff)));
            if (len > 0)
                dataBaos.write(ByteUtils.stringToBytes(getAuthId()));

            // authGroup
            len = ByteUtils.getLength(getAuthGroup());
            headerBaos.write(ByteUtils.shortToBytes((short) (len & 0xffff)));
            if (len > 0)
                dataBaos.write(ByteUtils.stringToBytes(getAuthGroup()));

            // chainPosition
            if (getChainPosition() == 0)
                headerBaos.write(0);
            else {
                headerBaos.write(8);
                dataBaos.write(ByteUtils.longToBytes(getChainPosition()));
            }

            // hash
            len = ByteUtils.getLength(getHash());
            headerBaos.write(ByteUtils.shortToBytes((short) (len & 0xffff)));
            if (len > 0)
                dataBaos.write(ByteUtils.stringToBytes(getHash()));

            // previousHash
            len = ByteUtils.getLength(getPreviousHash());
            headerBaos.write(ByteUtils.shortToBytes((short) (len & 0xffff)));
            if (len > 0)
                dataBaos.write(ByteUtils.stringToBytes(getPreviousHash()));

            // nonce
            len = ByteUtils.getLength(getNonce());
            headerBaos.write(ByteUtils.shortToBytes((short) (len & 0xffff)));
            if (len > 0)
                dataBaos.write(ByteUtils.stringToBytes(getNonce()));

            // difficultyTarget
            if (getDifficultyTarget() == 0)
                headerBaos.write(0);
            else {
                headerBaos.write(4);
                dataBaos.write(ByteUtils.integerToBytes(getDifficultyTarget()));
            }

            // infoType
            len = ByteUtils.getLength(getInfoType());
            headerBaos.write((byte) (len & 0xff));
            if (len > 0)
                dataBaos.write(ByteUtils.stringToBytes(getInfoType()));

            // infoFormat
            len = ByteUtils.getLength(getInfoFormat());
            headerBaos.write((byte) (len & 0xff));
            if (len > 0)
                dataBaos.write(ByteUtils.stringToBytes(getInfoFormat()));

            // contextData
            if (getContextData() == null)
                headerBaos.write(ByteUtils.integerToBytes(0));
            else {
                headerBaos.write(ByteUtils.integerToBytes(getContextData().length));
                dataBaos.write(getContextData());
            }

            // contentData
            if (getContentData() == null)
                headerBaos.write(ByteUtils.integerToBytes(0));
            else {
                headerBaos.write(ByteUtils.integerToBytes(getContentData().length));
                dataBaos.write(getContentData());
            }

            ByteArrayOutputStream result = new ByteArrayOutputStream();
            headerBaos.writeTo(result);
            dataBaos.writeTo(result);
            return result.toByteArray();
        } catch (Exception e) {
            //TODO: log
        } finally {
            try {
                headerBaos.close();
                dataBaos.close();
            } catch (Exception e) {
                // TODO: log("Error when closing byte arrays streams");
            }
        }
        return new byte[] {};
    }

    private void convertBytesToMessage(byte[] header, byte[] data, int pos){
        if(header == null || header.length == 0) {
            header = data;
        }
        version = ByteUtils.bytesToShort(ByteUtils.copyOfRange(header, 0, 2));

        if (version != VERSION) {
            // TODO: incompatible version
            return;
        }

        int size = header[2];
        if (size > 0) {
            setId(ByteUtils.bytesToString(ByteUtils.copyOfRange(data, pos, pos + size)));
            pos += size;
        }

        size = ByteUtils.bytesToShort(ByteUtils.copyOfRange(header, 3, 5));
        if (size > 0) {
            setTag(ByteUtils.bytesToString(ByteUtils.copyOfRange(data, pos, pos + size)));
            pos += size;
        }

        size = header[5];
        if (size > 0) {
            setGroupId(ByteUtils.bytesToString(ByteUtils.copyOfRange(data, pos, pos + size)));
            pos += size;
        }

        size = header[6];
        if (size > 0) {
            setSequenceNumber(ByteUtils.bytesToInteger(ByteUtils.copyOfRange(data, pos, pos + size)));
            pos += size;
        }

        size = header[7];
        if (size > 0) {
            setSequenceTotal(ByteUtils.bytesToInteger(ByteUtils.copyOfRange(data, pos, pos + size)));
            pos += size;
        }

        size = header[8];
        if (size > 0) {
            setPriority(data[pos]);
            pos += size;
        }

        size = header[9];
        if (size > 0) {
            setTimestamp(ByteUtils.bytesToLong(ByteUtils.copyOfRange(data, pos, pos + size)));
            pos += size;
        }

        size = header[10];
        if (size > 0) {
            setPublisher(ByteUtils.bytesToString(ByteUtils.copyOfRange(data, pos, pos + size)));
            pos += size;
        }

        size = ByteUtils.bytesToShort(ByteUtils.copyOfRange(header, 11, 13));
        if (size > 0) {
            setAuthId(ByteUtils.bytesToString(ByteUtils.copyOfRange(data, pos, pos + size)));
            pos += size;
        }

        size = ByteUtils.bytesToShort(ByteUtils.copyOfRange(header, 13, 15));
        if (size > 0) {
            setAuthGroup(ByteUtils.bytesToString(ByteUtils.copyOfRange(data, pos, pos + size)));
            pos += size;
        }

        size = header[15];
        if (size > 0) {
            setChainPosition(ByteUtils.bytesToLong(ByteUtils.copyOfRange(data, pos, pos + size)));
            pos += size;
        }

        size = ByteUtils.bytesToShort(ByteUtils.copyOfRange(header, 16, 18));
        if (size > 0) {
            setHash(ByteUtils.bytesToString(ByteUtils.copyOfRange(data, pos, pos + size)));
            pos += size;
        }

        size = ByteUtils.bytesToShort(ByteUtils.copyOfRange(header, 18, 20));
        if (size > 0) {
            setPreviousHash(ByteUtils.bytesToString(ByteUtils.copyOfRange(data, pos, pos + size)));
            pos += size;
        }

        size = ByteUtils.bytesToShort(ByteUtils.copyOfRange(header, 20, 22));
        if (size > 0) {
            setNonce(ByteUtils.bytesToString(ByteUtils.copyOfRange(data, pos, pos + size)));
            pos += size;
        }

        size = header[22];
        if (size > 0) {
            setDifficultyTarget(ByteUtils.bytesToInteger(ByteUtils.copyOfRange(data, pos, pos + size)));
            pos += size;
        }

        size = header[23];
        if (size > 0) {
            setInfoType(ByteUtils.bytesToString(ByteUtils.copyOfRange(data, pos, pos + size)));
            pos += size;
        }

        size = header[24];
        if (size > 0) {
            setInfoFormat(ByteUtils.bytesToString(ByteUtils.copyOfRange(data, pos, pos + size)));
            pos += size;
        }

        size = ByteUtils.bytesToInteger(ByteUtils.copyOfRange(header, 25, 29));
        if (size > 0) {
            setContextData(ByteUtils.copyOfRange(data, pos, pos + size));
            pos += size;
        }

        size = ByteUtils.bytesToInteger(ByteUtils.copyOfRange(header, 29, 33));
        if (size > 0) {
            setContentData(ByteUtils.copyOfRange(data, pos, pos + size));
        }
    }

    @Override
    public String toString() {
        return "IOMessage{ " + getJson().toString() + " }";
    }

}
