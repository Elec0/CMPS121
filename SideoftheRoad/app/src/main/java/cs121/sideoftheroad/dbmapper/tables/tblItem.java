package cs121.sideoftheroad.dbmapper.tables;


import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@DynamoDBTable(tableName = "sideoftheroad-mobilehub-1016005302-item")

public class tblItem {
    private String _itemId;
    private String _description;
    private Map<String, String> _location;
    private String _price;
    private String _pics;
    private Set<String> _tags;
    private String _title;
    private String _userId;

    @DynamoDBHashKey(attributeName = "itemId")
    @DynamoDBIndexRangeKey(attributeName = "itemId", globalSecondaryIndexName = "getUser")
    public String getItemId() {
        return _itemId;
    }

    public void setItemId(final String _itemId) {
        this._itemId = _itemId;
    }
    @DynamoDBAttribute(attributeName = "description")
    public String getDescription() {
        return _description;
    }

    public void setDescription(final String _description) {
        this._description = _description;
    }
    @DynamoDBAttribute(attributeName = "location")
    public Map<String, String> getLocation() {
        return _location;
    }

    public void setLocation(final Map<String, String> _location) {
        this._location = _location;
    }
    @DynamoDBAttribute(attributeName = "pics")
    public String getPics() {
        return _pics;
    }

    public void setPics(final String _pics) {
        this._pics = _pics;
    }
    @DynamoDBAttribute(attributeName = "price")
    public String getPrice() {
        return _price;
    }

    public void setPrice(final String _price) {
        this._price = _price;
    }
    @DynamoDBAttribute(attributeName = "tags")
    public Set<String> getTags() {
        return _tags;
    }

    public void setTags(final Set<String> _tags) {
        this._tags = _tags;
    }
    @DynamoDBAttribute(attributeName = "title")
    public String getTitle() {
        return _title;
    }

    public void setTitle(final String _title) {
        this._title = _title;
    }
    @DynamoDBIndexHashKey(attributeName = "userId", globalSecondaryIndexName = "getUser")
    public String getUserId() {
        return _userId;
    }

    public void setUserId(final String _userId) {
        this._userId = _userId;
    }

}
