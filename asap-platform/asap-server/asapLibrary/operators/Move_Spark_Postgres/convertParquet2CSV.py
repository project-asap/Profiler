from pyspark import SparkContext, SparkConf
from pyspark.sql import SQLContext, HiveContext
from pyspark.sql.types import *

import os
import sys

def main():
	conf = SparkConf().setAppName( "Convert Parquet2CSV")
	sc = SparkContext( conf=conf)
	sqlContext = SQLContext(sc)
        fileSchemas = { "customer": StructType([        StructField( "c_custkey", IntegerType(), True),
                                                        StructField( "c_name", StringType(), True),
                                                        StructField( "c_address", StringType(), True),
                                                        StructField( "c_nationkey", DecimalType( 38, 0), True),
                                                        StructField( "c_phone", StringType(), True),
                                                        StructField( "c_acctbal", DecimalType( 10, 2), True),
                                                        StructField( "c_mktsegment", StringType(), True),
                                                        StructField( "c_comment", StringType(), True)]),
                        "lineitem": StructType([        StructField( "l_orderkey",DecimalType( 38, 0), True),
                                                        StructField( "l_partkey", DecimalType( 38, 0), True),
                                                        StructField( "l_suppkey", DecimalType( 38, 0), True),
                                                        StructField( "l_linenumber", IntegerType(), True),
                                                        StructField( "l_quantity", DecimalType( 10, 2), True),
                                                        StructField( "l_extendedprice", DecimalType( 10, 2), True),
                                                        StructField( "l_discount", DecimalType( 10, 2), True),
                                                        StructField( "l_tax", DecimalType( 10, 2), True),
                                                        StructField( "l_returnflag",StringType(), True),
                                                        StructField( "l_linestatus", StringType(), True),
                                                        StructField( "l_commitdate", DateType(), True),
                                                        StructField( "l_shipinstruct", StringType(), True),
                                                        StructField( "l_shipmode", StringType(), True),
                                                        StructField( "l_comment", StringType(), True)]),
                         "nation":   StructType([       StructField( "n_nationkey", IntegerType(), True),
                                                        StructField( "n_name", StringType(), True),
                                                        StructField( "n_regionkey", DecimalType( 38, 0), True),
                                                        StructField( "n_comment", StringType(), True)]),
                         "orders":   StructType([       StructField( "o_orderkey", IntegerType(), True),
                                                        StructField( "o_custkey", DecimalType( 38, 0), True),
                                                        StructField( "o_orderstatus", StringType(), True),
                                                        StructField( "o_totalprice", DecimalType( 10, 2), True),
                                                        StructField( "o_orderdate", DateType(), True),
                                                        StructField( "o_orderpriority", StringType(), True),
                                                        StructField( "o_clerk", StringType(), True),
                                                        StructField( "o_shippriority", IntegerType(), True),
                                                        StructField( "o_comment", StringType(), True)]),
                         "part":     StructType([       StructField( "p_partkey", IntegerType(), True),
                                                        StructField( "p_name", StringType(), True),
                                                        StructField( "p_mfgr", StringType(), True),
                                                        StructField( "p_brand", StringType(), True),
                                                        StructField( "p_type", StringType(), True),
                                                        StructField( "p_size", IntegerType(), True),
                                                        StructField( "p_container", StringType(), True),
                                                        StructField( "p_retailprice", DecimalType( 10, 2), True),
                                                        StructField( "p_comment", StringType(), True)]),
                         "partsupp": StructType([       StructField( "ps_partkey", DecimalType( 38, 0), True),
                                                        StructField( "ps_suppkey", DecimalType( 38, 0), True),
                                                        StructField( "ps_availqty", IntegerType(), True),
                                                        StructField( "ps_supplycost", StringType(), True),
                                                        StructField( "ps_comment", StringType(), True)]),
                         "region":   StructType([       StructField( "r_regionkey", IntegerType(), True),
                                                        StructField( "r_name", StringType(), True),
                                                        StructField( "r_comment", StringType(), True)]),
                         "supplier":     StructType([   StructField( "s_suppkey", IntegerType(), True),
                                                        StructField( "s_name", StringType(), True),
                                                        StructField( "s_address", StringType(), True),
                                                        StructField( "s_nationkey", DecimalType( 38, 0), True),
                                                        StructField( "s_phone", StringType(), True),
                                                        StructField( "s_acctbal", DecimalType( 10, 2), True),
                                                        StructField( "s_comment", StringType(), True)]),
                         "part_agg":   StructType([     StructField( "agg_partkey", DecimalType( 38, 0), True),
                                                        StructField( "avg_quantity", DecimalType( 10, 2), True),
                                                        StructField( "agg_extendedprice", DecimalType( 10, 2), True)])
                        }

	yarn_home = sys.argv[ 1]
        file = sys.argv[ 2]
	print( yarn_home, file)
        namenode = "hdfs://master:9000"
        warehouse = "/user/hive/warehouse"
        df = sqlContext.read.parquet( namenode + warehouse + "/" + file + ".parquet")
        df.printSchema()
	output = warehouse + "/" + file + ".csv"

        if os.path.exists( namenode + output):
            try:
                os.system( "/opt/hadoop-2.7.0/bin/hdfs dfs -rm -r " + output)
            except OSError:
                raise

        df.write.format( "com.databricks.spark.csv").options( delimiter='|').save( namenode  + output)

if __name__ == "__main__":
	main()
