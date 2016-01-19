library(plyr)
library(tidyr)
library(dplyr)
library(ggplot2)
runtime.features <- read.table("..//output//features_values.tsv", header = TRUE, sep = "\t") # edu.wayne.pfsdm.auxiliary.FeatureValuesTable
is.na(runtime.features) <- sapply(runtime.features, is.infinite)
runtime.features <- runtime.features %>% filter(featurename != "bigramcfratio")
runtime.features <- unique(runtime.features)
runtime.features.wide <- spread(runtime.features, featurename, featurevalue)
concept.types <- read.table("..//src//main//resources//sigir2013-dbpedia//concept-types.tsv", sep = "\t", quote = "", col.names = c("qid", "gram", "type", "text"))
# concept.types$concept.id <- 1:(nrow(concept.types))
# runtime.features.wide$concept.id <- rep(1:(nrow(runtime.features.wide)/5), each=5)
merged <- merge(runtime.features.wide, concept.types, by = c("qid", "gram"))
filtered <- filter(merged, type != "", type != "unsure", type != "none")
features <- c("fieldlikelihood", "baselinetopscore")
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
filtered.long <- filtered %>% rename(FP=fieldlikelihood,TS=baselinetopscore)
filtered.long <- gather(filtered.long, feature, value, c(FP,TS))#,MI))
filtered.long <- filtered.long %>% filter(!(feature == "TS" & ngramtype == "unigram"))

plot.real <- ggplot(na.omit(filtered.long), aes_string(y = "value", x = "field", fill = "type")) +
  geom_boxplot(outlier.size = 1) + theme(axis.text.x=element_text(angle=30,hjust=1)) +
  facet_wrap(~feature, scales = "free")+
  labs(y="feature value", fill="concept type", x="") +
  theme(legend.position="bottom")

ggsave("plots-features/real.eps", plot.real, width=10, height=7, scale=0.8)
#ggsave("plots-features/real.eps", plot.real, height=10, scale=0.8)

cf <- read.table("..//output//cf.tsv", header = TRUE, sep = "\t") # edu.wayne.pfsdm.auxiliary.FeatureValuesTable
is.na(cf) <- sapply(cf, is.infinite)
cf <- unique(cf)
cf.wide <- spread(cf, featurename, featurevalue)

nnp <- read.table("..//src//main//resources//file-based-features//nnp.tsv", sep = "\t", quote = "", col.names = c("qid", "gram", "value", "text"))
merged.nnp <- merge(cf.wide, nnp) %>% filter(ngramtype == "unigram")

nns <- read.table("..//src//main//resources//file-based-features//nns.tsv", sep = "\t", quote = "", col.names = c("qid", "gram", "value", "text"))
merged.nns <- merge(cf.wide, nns)

jjs <- read.table("..//src//main//resources//file-based-features//jjs.tsv", sep = "\t", quote = "", col.names = c("qid", "gram", "value", "text"))
merged.jjs <- merge(cf.wide, jjs) %>% filter(ngramtype == "unigram")

np.part <- read.table("..//src//main//resources//file-based-features//np-part.tsv", sep = "\t", quote = "", col.names = c("qid", "gram", "value", "text"))
merged.np.part <- merge(cf.wide, np.part) %>% filter(ngramtype == "bigram")

nn.only <- read.table("..//src//main//resources//file-based-features//nn-only.tsv", sep = "\t", quote = "", col.names = c("qid", "gram", "value", "text"))
merged.nn.only <- merge(cf.wide, nn.only) %>% filter(ngramtype == "unigram")

merged.nnp$feature <- "NNP"
merged.nns$feature <- "NNS"
merged.jjs$feature <- "JJS"
merged.np.part$feature <- "NPP"
merged.nn.only$feature <- "NNO"

all.nlp <- bind_rows(merged.nnp,merged.nns,merged.jjs,merged.np.part,merged.nn.only)
all.nlp <- all.nlp %>% mutate(field = revalue(field, c("outgoingentitynames"="related entity names","similarentitynames"="similar entity names","names"="entity names")))
plot.nlp <- ggplot(na.omit(all.nlp), aes(y = cf, x = field, fill=factor(value))) +
  geom_boxplot(outlier.size = 1) + theme(axis.text.x=element_text(angle=30,hjust=1)) +
  labs(y="log(field frequency)", fill="feature value", x="") +
  facet_grid(.~feature) +
  theme(legend.position="bottom")
ggsave("plots-features/nlp.eps", plot.nlp, width=12.5, height=5, scale=0.8)
