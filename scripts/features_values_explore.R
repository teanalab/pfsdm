library(tidyr)

features.values <- read.table("output//features_values.tsv", header = TRUE, sep = "\t")
features.values$id <- rep(1:(nrow(features.values)/4), each=4)
features.values.wide <- spread(features.values, featurename, featurevalue)
hist(features.values.wide$baselinetopscore)
hist(features.values.wide$fieldlikelihood)