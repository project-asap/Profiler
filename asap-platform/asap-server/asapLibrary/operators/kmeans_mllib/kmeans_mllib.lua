-- The command to execute.
SHELL_COMMAND = "./kmeans_mllib.sh"
-- The number of containers to run it on.
CONTAINER_INSTANCES = 1
-- The location of the jar file containing kitten's default ApplicationMaster
-- implementation.
MASTER_JAR_LOCATION = "kitten-master-0.2.0-jar-with-dependencies.jar"

-- CLASSPATH setup.
CP = "/opt/hadoop-2.7.0/etc/hadoop:/opt/hadoop-2.7.0/etc/hadoop:/opt/hadoop-2.7.0/etc/hadoop:/opt/hadoop-2.7.0/share/hadoop/common/lib/*:/opt/hadoop-2.7.0/share/hadoop/common/*:/opt/hadoop-2.7.0/share/hadoop/hdfs:/opt/hadoop-2.7.0/share/hadoop/hdfs/lib/*:/opt/hadoop-2.7.0/share/hadoop/hdfs/*:/opt/hadoop-2.7.0/share/hadoop/yarn/lib/*:/opt/hadoop-2.7.0/share/hadoop/yarn/*:/opt/hadoop-2.7.0/share/hadoop/mapreduce/lib/*:/opt/hadoop-2.7.0/share/hadoop/mapreduce/*:/contrib/capacity-scheduler/*.jar:/opt/hadoop-2.7.0/share/hadoop/yarn/*:/opt/hadoop-2.7.0/share/hadoop/yarn/lib/*"

-- Resource and environment setup.
base_resources = {
  ["master.jar"] = { file = MASTER_JAR_LOCATION }
}
base_env = {
  CLASSPATH = table.concat({"${CLASSPATH}", CP, "./master.jar", "./kmeans_mllib.sh", "./spark_kmeans_text.py"}, ":"),
}

-- The actual distributed shell job.
operator = yarn {
  name = "Execute Java Operator",
  timeout = -1,
  memory = 4096,
  cores = 1,
  master = {
    env = base_env,
    resources = base_resources,
    command = {
      base = "${JAVA_HOME}/bin/java -Xms64m -Xmx128m com.cloudera.kitten.appmaster.ApplicationMaster",
      args = { "-conf job.xml" },
    }
  },

  container = {
    instances = CONTAINER_INSTANCES,
    env = base_env,
    resources = {
    ["kmeans_mllib.sh"] = {
       file = "/opt/asap-server/asapLibrary/operators/kmeans_mllib/kmeans_mllib.sh",
      type = "file",               -- other value: 'archive'
      visibility = "application",  -- other values: 'private', 'public'
	},
    ["spark_kmeans_text.py"] = {
       file = "/opt/asap-server/asapLibrary/operators/kmeans_mllib/spark_kmeans_text.py",
      type = "file",               -- other value: 'archive'
      visibility = "application",  -- other values: 'private', 'public'
	}
    },
    command = {
	base = SHELL_COMMAND,
--	args = { "1> <LOG_DIR>/stdout", "2> <LOG_DIR>/stderr" },
    }
  }
}
