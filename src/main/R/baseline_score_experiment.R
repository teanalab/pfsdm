library(reshape2)
library(ggplot2)

data <- read.table("output/baseline_score_experiment.tsv", header = TRUE, sep = "\t")
namequeries <- read.table("src/main/resources/namequeries.tsv", header = TRUE, sep = "\t")
fields = c("names" , "attributes" , "similarentitynames" , "categories" , "outgoingentitynames")
data <- merge(data, namequeries)
dataMelt <- melt(data, id = c("type", "qid", "tokens", "text", "name", "relevance"), measure.vars = fields)
ggplot(dataMelt, aes(y=value, x = variable, fill=relevance)) + geom_boxplot() + facet_wrap(~type+name) +
  theme(axis.text.x=element_text(angle=30,hjust=1))
ggsave(file="output/field-boxplot.pdf")
