./gradlew :solr:core:test --tests "org.apache.solr.cloud.TestCloudQuerySenderListener.testQuerySenderListener" -Ptests.jvms=8 "-Ptests.jvmargs=-XX:TieredStopAtLevel=1 -XX:ReservedCodeCacheSize=120m" -Ptests.seed=57890D894C35C90E -Ptests.file.encoding=US-ASCII
