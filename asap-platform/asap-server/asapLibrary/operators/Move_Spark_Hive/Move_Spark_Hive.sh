#!/bin/bash

echo -e "Move_Spark_Hive\n"

export HADOOP_HOME=/opt/hadoop-2.7.0
export SPARK_HOME=/opt/spark
export HIVE_HOME=/mnt/Data/tmp/hive
HDFS=/user/hive/warehouse
TABLE=$1
SCHEMA=$2
SPARK_PORT=$3
SQL_QUERY="DROP TABLE IF EXISTS $TABLE; CREATE TABLE $TABLE $SCHEMA ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'; LOAD DATA INPATH '/user/hive/warehouse/$TABLE.csv' OVERWRITE INTO TABLE $TABLE"

echo "Exporting table $TABLE from Spark"
echo "Converting Parquet $TABLE.parquet to CSV"
$HADOOP_HOME/bin/hdfs dfs -rm -r $HDFS/$TABLE.csv
$SPARK_HOME/bin/spark-submit --executor-memory 2G --driver-memory 512M  --packages com.databricks:spark-csv_2.10:1.4.0 --master $SPARK_PORT convertParquet2CSV.py $HADOOP_HOME $TABLE
#if [ ! -e /mnt/Data/tmp/$TABLE ]
#then
#	mkdir -p /mnt/Data/tmp/$TABLE
#	sudo chmod a+rwx /mnt/Data/tmp/$TABLE
#else
#	rm -r /mnt/Data/tmp/$TABLE/*
#fi
#$HADOOP_HOME/bin/hdfs/ dfs -getmerge $HDFS/$TABLE.csv /mnt/Data/tmp/$TABLE/$TABLE.csv
#ls -lah /mnt/Data/tmp/$TABLE/
#from parquet file double quotes have been added to string values
#and for this their length is changed. To restore their length the
#extra double quotes are removed with sed
#sed -i 's/|\"/|/g' /mnt/Data/tmp/$TABLE/$TABLE.csv
#sed -i 's/\"|/|/g' /mnt/Data/tmp/$TABLE/$TABLE.csv
#ls -ltr /mnt/Data/tmp/$TABLE
#to avoid local disk overflow while loading the file to the
#corresponding Hive table, the file is loaded through master
#cluster node
echo "Loading $TABLE.csv to Hive"
#scp /mnt/Data/tmp/$TABLE/$TABLE.csv master:/mnt/Data/
#ssh master "ls -lah /mnt/Data/"
#ssh master "$HADOOP_HOME/bin/hdfs dfs -moveFromLocal /mnt/Data/$TABLE.csv $HDFS/$TABLE.csv"
#rm /mnt/Data/tmp/$TABLE
cd $HIVE_HOME
#$HIVE_HOME/bin/hive -e "DROP TABLE IF EXISTS $TABLE; CREATE TABLE $TABLE $SCHEMA ROW FORMAT DELIMITED FIELDS TERMINATED BY ','; LOAD DATA LOCAL INPATH '/mnt/Data/tmp/$TABLE/$TABLE.csv' OVERWRITE INTO TABLE $TABLE"
$HIVE_HOME/bin/hive -e "$SQL_QUERY"
cd -

#clean
rm -r /mnt/Data/tmp/$TABLE
#$HADOOP_HOME/bin/hdfs dfs -rm -r $HDFS/$TABLE.csv
