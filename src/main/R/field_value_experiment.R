library(reshape2)
library(ggplot2)
library(dplyr)

tsvPath <- commandArgs(TRUE)[1]
outputPlot <- commandArgs(TRUE)[2]
data <- read.table(tsvPath, header = TRUE, sep = "\t")
print(dim(data))
gramtypes <- read.table("src/main/resources/sigir2013-dbpedia/unibigrams-types.tsv", header = TRUE, sep = "\t", quote = "")
print(dim(gramtypes))
fields = c("names" , "attributes" , "similarentitynames" , "categories" , "outgoingentitynames")
dataMerged <- cbind(data, gramtypes, stringAsFactors = TRUE)
print(dim(dataMerged))
dataMelt <- melt(dataMerged, id = c("ngramtype", "qid", "gram", "text", "type"), measure.vars = fields)
dataFilter <- filter(dataMelt, type != "", type != "unsure", type != "none")
p0 = ggplot(dataFilter, aes(y=value, x = variable, fill = type)) + geom_boxplot() + facet_wrap(~ngramtype) +
  theme(axis.text.x=element_text(angle=30,hjust=1)) + scale_y_log10()
#ylim1 = boxplot.stats(dataBigram$value)$stats[c(1, 5)]
#p1 = p0 + coord_cartesian(ylim = ylim1*4)
ggsave(file=outputPlot, plot = p0, width = par("din")[1] * 2)
