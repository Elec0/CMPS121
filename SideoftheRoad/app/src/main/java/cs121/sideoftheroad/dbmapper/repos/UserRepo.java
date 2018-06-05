package cs121.sideoftheroad.dbmapper.repos;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.mobile.client.AWSMobileClient;

import cs121.sideoftheroad.dbmapper.tables.tblUser;

public class UserRepo {
    private DynamoDBMapper dbMapper;

    public UserRepo(AWSMobileClient awsMobileClient) {
        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(awsMobileClient.getInstance().getCredentialsProvider());
        this.dbMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(awsMobileClient.getInstance().getConfiguration())
                .build();
    }

    public void save(tblUser user){
        dbMapper.save(user);
    }

    public tblUser getUser(String username) {
        return dbMapper.load(tblUser.class, username);
    }
}
