library(plyr)
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
# for (feature in features) {
#   #TODO rescale baselinetopscore!
#   plot <- ggplot(na.omit(filtered[,c(feature, "field", "type", "ngramtype")]), aes_string(y = feature, x = "field", fill = "type")) +
#     geom_boxplot() + theme(axis.text.x=element_text(angle=30,hjust=1)) +
#     labs(y="feature value", fill="concept type", x="")
#   if (feature=="baselinetopscore") {
#     plot <- plot + ylim(-15,0)
#   }
#   ggsave(paste0("plots-features/", feature, ".eps"), plot, height=5, width=5)
# }

filtered <- filtered %>% mutate(field = revalue(field, c("outgoingentitynames"="related entity names","similarentitynames"="similar entity names","names"="entity names")))
filtered.long <- filtered %>% rename(FP=fieldlikelihood,TS=baselinetopscore,MI=bigramcfratio)
filtered.long <- gather(filtered.long, feature, value, c(FP,TS))#,MI))

plot.real <- ggplot(na.omit(filtered.long), aes_string(y = "value", x = "field", fill = "type")) +
  geom_boxplot() + theme(axis.text.x=element_text(angle=30,hjust=1)) +
  facet_wrap(~feature, scales = "free")+
  labs(y="feature value", fill="concept type", x="") +
  theme(legend.position="bottom")

ggsave("plots-features/real.eps", plot.real, width=9, height=6, scale=0.8)
#ggsave("plots-features/real.eps", plot.real, height=10, scale=0.8)

cf <- read.table("..//output//cf.tsv", header = TRUE, sep = "\t") # edu.wayne.pfsdm.auxiliary.FeatureValuesTable
is.na(cf) <- sapply(cf, is.infinite)
cf <- unique(cf)
cf.wide <- spread(cf, featurename, featurevalue)

nnp <- read.table("..//src//main//resources//file-based-features//nnp.tsv", sep = "\t", quote = "", col.names = c("qid", "gram", "value", "text"))
merged.nnp <- merge(cf.wide, nnp)
plot <- ggplot(na.omit(merged.nnp), aes(y = cf, x = field, fill=factor(value))) +
  geom_boxplot()  + theme(axis.text.x=element_text(angle=30,hjust=1)) +
  labs(y="log(field frequency)", fill="feature value", x="")
ggsave("plots-features/nnp.eps", plot, height=5, width=4)

nns <- read.table("..//src//main//resources//file-based-features//nns.tsv", sep = "\t", quote = "", col.names = c("qid", "gram", "value", "text"))
merged.nns <- merge(cf.wide, nns)
plot <- ggplot(na.omit(merged.nns), aes(y = cf, x = field, fill=factor(value))) +
  geom_boxplot()+ theme(axis.text.x=element_text(angle=30,hjust=1)) +
  labs(y="log(field frequency)", fill="feature value", x="")
ggsave("plots-features/nns.eps", plot, height=5, width=4)

np.exact <- read.table("..//src//main//resources//file-based-features//np-exact.tsv", sep = "\t", quote = "", col.names = c("qid", "gram", "value", "text"))
merged.np.exact <- merge(cf.wide, np.exact) %>% filter(ngramtype == "bigram")
plot <- ggplot(na.omit(merged.np.exact), aes(y = cf, x = field, fill=factor(value))) +
  geom_boxplot() + theme(axis.text.x=element_text(angle=30,hjust=1)) +
  labs(y="log(field frequency)", fill="feature value", x="")
ggsave("plots-features/npe.eps", plot, height=5, width=4)

np.part <- read.table("..//src//main//resources//file-based-features//np-part.tsv", sep = "\t", quote = "", col.names = c("qid", "gram", "value", "text"))
merged.np.part <- merge(cf.wide, np.part) %>% filter(ngramtype == "bigram")
plot <- ggplot(na.omit(merged.np.part), aes(y = cf, x = field, fill=factor(value))) +
  geom_boxplot() + theme(axis.text.x=element_text(angle=30,hjust=1)) +
  labs(y="log(field frequency)", fill="feature value", x="")
ggsave("plots-features/npp.eps", plot, height=5, width=4)

qd <- read.table("..//src//main//resources//file-based-features//question-dep.tsv", sep = "\t", quote = "", col.names = c("qid", "gram", "value", "text"))
merged.qd <- merge(cf.wide, qd) %>% filter(ngramtype == "unigram")
plot <- ggplot(na.omit(merged.qd), aes(y = cf, x = field, fill=factor(value))) +
  geom_boxplot() + theme(axis.text.x=element_text(angle=30,hjust=1)) +
  labs(y="log(field frequency)", fill="feature value", x="")
ggsave("plots-features/qd.eps", plot, height=5, width=4)

merged.nnp$feature <- "NNP"
merged.nns$feature <- "NNS"
merged.np.exact$feature <- "NPE"
merged.np.part$feature <- "NPP"
merged.qd$feature <- "QD"

all.nlp <- bind_rows(merged.nnp,merged.nns,merged.np.exact,merged.np.part)#,merged.qd)
all.nlp <- all.nlp %>% mutate(field = revalue(field, c("outgoingentitynames"="related entity names","similarentitynames"="similar entity names","names"="entity names")))
plot.nlp <- ggplot(na.omit(all.nlp), aes(y = cf, x = field, fill=factor(value))) +
  geom_boxplot() + theme(axis.text.x=element_text(angle=30,hjust=1)) +
  labs(y="log(field frequency)", fill="feature value", x="") +
  facet_grid(.~feature) +
  theme(legend.position="bottom")
ggsave("plots-features/nlp.eps", plot.nlp, width=10, height=5, scale=0.8)
