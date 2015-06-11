library(reshape2)
library(ggplot2)
library(dplyr)

tsvPath <- commandArgs(TRUE)[1]
outputPlot <- commandArgs(TRUE)[2]
data <- read.table(tsvPath, header = TRUE, sep = "\t")
namequeries <- read.table("output/namequeries.tsv", sep = "\t", col.names = c("qid", "text", "name"))
fields = c("names" , "attributes" , "similarentitynames" , "categories" , "outgoingentitynames")
data <- merge(data, namequeries)
dataMelt <- melt(data, id = c("type", "qid", "tokens", "text", "name"), measure.vars = fields)
dataBigram <- filter(dataMelt, type == "bigram")
p0 = ggplot(dataMelt, aes(y=value, x = variable, fill = name)) + geom_boxplot() + facet_wrap(~type) +
  theme(axis.text.x=element_text(angle=30,hjust=1)) + scale_y_log10()
#ylim1 = boxplot.stats(dataBigram$value)$stats[c(1, 5)]
#p1 = p0 + coord_cartesian(ylim = ylim1*4)
ggsave(file=outputPlot, plot = p0)
