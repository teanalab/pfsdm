library(tidyr)
library(dplyr)
library(ggplot2)
runtime.features <- read.table("..//output//features_values.tsv", header = TRUE, sep = "\t") # edu.wayne.pfsdm.auxiliary.FeatureValuesTable
is.na(runtime.features) <- sapply(runtime.features, is.infinite)
runtime.features <- unique(runtime.features)
runtime.features.wide <- spread(runtime.features, featurename, featurevalue)
concept.types <- read.table("..//src//main//resources//sigir2013-dbpedia//concept-types.tsv", sep = "\t", quote = "", col.names = c("qid", "gram", "type", "text"))
# concept.types$concept.id <- 1:(nrow(concept.types))
# runtime.features.wide$concept.id <- rep(1:(nrow(runtime.features.wide)/5), each=5)
merged <- merge(runtime.features.wide, concept.types, by = c("qid", "gram"))
filtered <- filter(merged, type != "", type != "unsure", type != "none")
features <- c("fieldlikelihood", "baselinetopscore", "bigramcfratio")
for (feature in features) {
  plot <- ggplot(na.omit(filtered[,c(feature, "field", "type", "ngramtype")]), aes_string(y = feature, x = "type", fill = "field")) +
          geom_boxplot() + facet_wrap(~ngramtype) + theme(axis.text.x=element_text(angle=30,hjust=1)) +
          ggtitle(feature)
  ggsave(paste0("plots-features/", feature, ".eps"), plot)
}

cf <- read.table("..//output//cf.tsv", header = TRUE, sep = "\t") # edu.wayne.pfsdm.auxiliary.FeatureValuesTable
is.na(cf) <- sapply(cf, is.infinite)
cf <- unique(cf)
cf.wide <- spread(cf, featurename, featurevalue)

nnp <- read.table("..//src//main//resources//file-based-features//nnp.tsv", sep = "\t", quote = "", col.names = c("qid", "gram", "value", "text"))
merged.nnp <- merge(cf.wide, nnp)
plot <- ggplot(na.omit(merged.nnp), aes(y = cf, x = field, fill=factor(value))) +
  geom_boxplot() + facet_wrap(~ngramtype) + theme(axis.text.x=element_text(angle=30,hjust=1))
ggsave("plots-features/nnp.eps", plot)

nns <- read.table("..//src//main//resources//file-based-features//nns.tsv", sep = "\t", quote = "", col.names = c("qid", "gram", "value", "text"))
merged.nns <- merge(cf.wide, nns)
plot <- ggplot(na.omit(merged.nns), aes(y = cf, x = field, fill=factor(value))) +
  geom_boxplot() + facet_wrap(~ngramtype) + theme(axis.text.x=element_text(angle=30,hjust=1))
ggsave("plots-features/nns.eps", plot)

np.exact <- read.table("..//src//main//resources//file-based-features//np-exact.tsv", sep = "\t", quote = "", col.names = c("qid", "gram", "value", "text"))
merged.np.exact <- merge(cf.wide, np.exact) %>% filter(ngramtype == "bigram")
plot <- ggplot(na.omit(merged.np.exact), aes(y = cf, x = field, fill=factor(value))) +
  geom_boxplot() + theme(axis.text.x=element_text(angle=30,hjust=1))
ggsave("plots-features/npe.eps", plot)

np.part <- read.table("..//src//main//resources//file-based-features//np-part.tsv", sep = "\t", quote = "", col.names = c("qid", "gram", "value", "text"))
merged.np.part <- merge(cf.wide, np.part) %>% filter(ngramtype == "bigram")
plot <- ggplot(na.omit(merged.np.part), aes(y = cf, x = field, fill=factor(value))) +
  geom_boxplot() + theme(axis.text.x=element_text(angle=30,hjust=1))
ggsave("plots-features/npp.eps", plot)

qd <- read.table("..//src//main//resources//file-based-features//question-dep.tsv", sep = "\t", quote = "", col.names = c("qid", "gram", "value", "text"))
merged.qd <- merge(cf.wide, qd) %>% filter(ngramtype == "unigram")
plot <- ggplot(na.omit(merged.qd), aes(y = cf, x = field, fill=factor(value))) +
  geom_boxplot() + theme(axis.text.x=element_text(angle=30,hjust=1))
ggsave("plots-features/qd.eps", plot)
