tsvPath <- commandArgs(TRUE)[1]

data <- read.table(tsvPath, header = FALSE, sep = "\t", quote="")
print(min(data$V3[data$V3 != -Inf]))
print(max(data$V3))
