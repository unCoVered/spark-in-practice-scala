package com.duchessfr.spark.streaming

import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.twitter._
import org.apache.spark.SparkConf
import org.apache.spark._

/**
 * First authenticate with the Twitter streaming API.
 *
 * Go to https://apps.twitter.com/
 * Create your application and then get your own credentials (keys and access tokens tab)
 *
 * See https://databricks-training.s3.amazonaws.com/realtime-processing-with-spark-streaming.html
 * for help.
 *
 * If you have the following error "error 401 Unauthorized":
 * - it might be because of wrong credentials
 * OR
 * - a time zone issue (so be certain that the time zone on your computer is the good one)
 *
 * The Spark Streaming documentation is available on:
 * http://spark.apache.org/docs/latest/streaming-programming-guide.html
 *
 * Spark Streaming is an extension of the core Spark API that enables scalable,
 * high-throughput, fault-tolerant stream processing of live data streams.
 * Spark Streaming receives live input data streams and divides the data into batches,
 * which are then processed by the Spark engine to generate the final stream of results in batches.
 * Spark Streaming provides a high-level abstraction called discretized stream or DStream,
 * which represents a continuous stream of data.
 *
 * In this exercise we will:
 * - Print the status text of the some of the tweets
 * - Find the 10 most popular Hashtag in the last minute
 *
 * You can see informations about the streaming in the Spark UI console: http://localhost:4040/streaming/
 */
object StreamingOnTweets extends App {

  def top10Hashtag() = {
    // TODO fill the keys and tokens
    val CONSUMER_KEY = "BFdcHpyoxgLerfkADnj8N9vvl"
    val CONSUMER_SECRET = "wP5o8Oq1qfRoTh8z5GpCGY2A7YHwBWfb0XsULgUnmzr0ujuLI1"
    val ACCESS_TOKEN = "365704012-qJJiXGx2r4Y5EoQWAMC5acrXOzOCsm602iNbTHT6"
    val ACCESS_TOKEN_SECRET = "ptdPfNtRNPxtSLY3lpY3IxIYAeDiapOb8DToF1LeSZcxX"

    System.setProperty("twitter4j.oauth.consumerKey", CONSUMER_KEY)
    System.setProperty("twitter4j.oauth.consumerSecret", CONSUMER_SECRET)
    System.setProperty("twitter4j.oauth.accessToken", ACCESS_TOKEN)
    System.setProperty("twitter4j.oauth.accessTokenSecret", ACCESS_TOKEN_SECRET)

    // Load the data using TwitterUtils: we obtain a DStream of tweets
    //
    // More about TwitterUtils:
    // https://spark.apache.org/docs/1.4.0/api/java/index.html?org/apache/spark/streaming/twitter/TwitterUtils.html

    // create spark configuration and spark context
    val conf = new SparkConf()
        .setAppName("streaming")
        .setMaster("local[*]")

    val sc = new SparkContext(conf)
    // create a StreamingContext by providing a Spark context and a window (2 seconds batch)
    val ssc = new StreamingContext(sc, Seconds(2))

    println("Initializing Twitter stream...")

    // Here we start a stream of tweets
    // The object tweetsStream is a DStream of tweet statuses:
    // - the Status class contains all information of a tweet
    // See http://twitter4j.org/javadoc/twitter4j/Status.html
    // and fill the keys and tokens
    val tweetsStream = TwitterUtils.createStream(ssc, None, Array[String]())

    //Your turn ...

    // Print the status text of the some of the tweets
    // You must see tweets appear in the console
    val status = tweetsStream.map(_.getText)
    // Here print the status's text: see the Status class
    // Hint: use the print method
    status.print()

    // Find the 10 most popular Hashtag in the last minute

    // For each tweet in the stream filter out all the hashtags
    // stream is like a sequence of RDD so you can do all the operation you did in the first part of the hands-on
    // Hint: think about what you did in the Hashtagmining part
    val hashTags = tweetsStream.map(_.getText.split(" ").filter(_.startsWith("#")).filter(_.length > 1))

    // Now here, find the 10 most popular hashtags in a 60 seconds window
    // Hint: look at the reduceByKeyAndWindow function in the spark doc.
    // Reduce last 60 seconds of data
    // Hint: look at the transform function to operate on the DStream
    val top10 = hashTags.map((_, 1))
      .reduceByKeyAndWindow(_ + _, Seconds(60))
      .transform(_.sortBy(_._2, false))

    // and return the 10 most populars
    // Hint: loop on the RDD and take the 10 most popular
    top10.foreachRDD( rdd => {
      val topList = rdd.take(10)
      topList.foreach {
        case (tag, count) => println(s"$tag: $count")
      }
    })


    // we need to tell the context to start running the computation we have setup
    // it won't work if you don't add this!
    ssc.start
    ssc.awaitTermination
  }
}
