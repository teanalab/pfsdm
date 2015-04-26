library(reshape2)
data <- read.table("output/field_experiment.tsv", header = TRUE)
dataMelt <- melt(data, id=c("qid", "term"),measure.vars=c("names" , "attributes" , "similarentitynames" , "categories" , "outgoingentitynames"))
boxplot(value~variable,data=dataMelt)
