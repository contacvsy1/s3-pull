package com.directv.dap.tb.s3;

import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectSummary;
//import org.apache.hadoop.conf.Configuration;
//import org.apache.hadoop.fs.FileSystem;
//import org.apache.hadoop.fs.Path;
//import org.apache.hadoop.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S3JobCommitter {
    private static final Logger logger = LoggerFactory.getLogger(S3JobCommitter.class);

    public static void main(String[] args) throws IOException {
        logger.info("Initialization");
        AmazonS3 s3client = new AmazonS3Client(new BasicAWSCredentials(args[3], args[4]));
        String bucketName = args[0];
        String path = args[1];
        String target = args[2];
        if ('/' == (path.charAt(0))) path = path.substring(1);
        if (!path.endsWith("/")) path += '/';

       /* Configuration conf = new Configuration();
        conf.addResource(new Path(System.getProperty("oozie.action.conf.xml")));

        FileSystem fs = FileSystem.get(conf);
       */
        ListObjectsRequest listObjectRequest = new ListObjectsRequest()
                .withBucketName(bucketName)
                .withPrefix("Complete/1522682274")
                .withDelimiter("/");

        ObjectListing objectListing;

        do {
             objectListing = s3client.listObjects(listObjectRequest);

        Pattern pattern = Pattern.compile(".*(\\d{4})-(\\d{2})-(\\d{2})-(\\d{2}).*");
        Matcher matcher;

        System.out.println("s3 object listing: " +objectListing.getObjectSummaries());

        for (S3ObjectSummary object : objectListing.getObjectSummaries()) {


           //System.out.println("s3 object listing: " +object.getKey());
            //System.out.println("s3 object listing: " +object.getSize());

            if (object.getKey().endsWith("/")) continue;
            String filename = object.getKey().replace(path, "");
            //System.out.println("all files: " +filename);
            matcher = pattern.matcher(filename);
            if (matcher.find()) {
                System.out.println("Matched File: " +filename);
                String directory = String.format("%s/year=%s/month=%s/day=%s/hour=%s/",
                        target,
                        matcher.group(1),
                        matcher.group(2),
                        matcher.group(3),
                        matcher.group(4)
                );
                //else {System.out.println("pattern not matched" +filename);}

                String fullPath = directory + '/' + filename;

         /*       Path outputPath = new Path(fullPath);
                if (fs.exists(outputPath)) {
                    logger.info(filename + " already exists in " + outputPath);
                } else {
                    logger.info("Copying " + object.getKey() + " to " + outputPath);
                    fs.mkdirs(new Path(directory));
                    InputStream in = s3client.getObject(bucketName, object.getKey()).getObjectContent();
                    OutputStream os = fs.create(outputPath);

                    IOUtils.copyBytes(in, os, 4096, true);
                } */
            } else {System.out.println("pattern not matched: " +filename);}
        }
            listObjectRequest.setMarker(objectListing.getNextMarker());
    } while (objectListing.isTruncated());
        }

}