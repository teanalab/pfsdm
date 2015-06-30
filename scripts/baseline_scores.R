library(reshape2)
library(ggplot2)

data <- read.table("output/experiments/baseline_scores/baseline_scores.tsv", header = TRUE, sep = "\t")
namequeries <- read.table("data/sigir2013-dbpedia/unibigrams-named.tsv", header = TRUE, sep = "\t")
fields = c("names" , "attributes" , "similarentitynames" , "categories" , "outgoingentitynames")
data <- merge(data, namequeries)
dataMelt <- melt(data, id = c("type", "qid", "tokens", "text", "name", "relevance"), measure.vars = fields)
ggplot(dataMelt, aes(y=value, x = variable, fill=relevance)) + geom_boxplot() + facet_wrap(~type+name) +
  theme(axis.text.x=element_text(angle=30,hjust=1))
ggsave(file="output/experiments/baseline_scores/baseline_scores.pdf")
