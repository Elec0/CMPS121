package cs121.sideoftheroad.s3bucket;

public class Constants {

    public static final String COGNITO_POOL_ID = "us-east-1:800256ad-1679-4750-b2c5-15921ecb246b";

    /*
     * Region of your Cognito identity pool ID.
     */
    public static final String COGNITO_POOL_REGION = "us-west-1";

    /*
     * Note, you must first create a bucket using the S3 console before running
     * the sample (https://console.aws.amazon.com/s3/). After creating a bucket,
     * put it's name in the field below.
     */
    public static final String BUCKET_NAME = "sotr-items";

    /*
     * Region of your bucket.
     */
    public static final String BUCKET_REGION = "us-west-1";
}