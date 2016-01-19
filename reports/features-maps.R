library(ggplot2)
library(dplyr)

combos <- c(
  "UG: FP, NNO; BG: TS, NPP, NNS",
  "UG: FP, NNO; BG: INT",
  "UG: FP, NNO; BG: INT, TS",
  "UG: FP, NNO; BG: INT, TS, NPP, NNS",
  "UG: FP, NNO; BG: INT, FP, TS, NPP, NNS",
  "UG: FP, NNO; BG: FP, TS, NPP",
  "UG: FP, NNO; BG: FP, TS, NPP, NNS",
  "UG: FP, NNP, TS; BG: TS, NPP, NNS",
  "UG: FP, NNP, TS; BG: INT",
  "UG: FP, NNP, TS; BG: INT, TS",
  "UG: FP, NNP, TS; BG: INT, TS, NPP, NNS",
  "UG: FP, NNP, TS; BG: INT, FP, TS, NPP, NNS",
  "UG: FP, NNP, TS; BG: FP, TS, NPP",
  "UG: FP, NNP, TS; BG: FP, TS, NPP, NNS",
  "UG: FP, NNP; BG: TS, NPP, NNS",
  "UG: FP, NNP; BG: INT",
  "UG: FP, NNP; BG: INT, TS",
  "UG: FP, NNP; BG: INT, TS, NPP, NNS",
  "UG: FP, NNP; BG: INT, FP, TS, NPP, NNS",
  "UG: FP, NNP; BG: FP, TS, NPP",
  "UG: FP, NNP; BG: FP, TS, NPP, NNS",
  "UG: FP, NNP, JJS; BG: TS, NPP, NNS",
  "UG: FP, NNP, JJS; BG: INT",
  "UG: FP, NNP, JJS; BG: INT, TS",
  "UG: FP, NNP, JJS; BG: INT, TS, NPP, NNS",
  "UG: FP, NNP, JJS; BG: INT, FP, TS, NPP, NNS",
  "UG: FP, NNP, JJS; BG: FP, TS, NPP",
  "UG: FP, NNP, JJS; BG: FP, TS, NPP, NNS",
  "UG: FP, NNP, NNS; BG: TS, NPP, NNS",
  "UG: FP, NNP, NNS; BG: INT",
  "UG: FP, NNP, NNS; BG: INT, TS",
  "UG: FP, NNP, NNS; BG: INT, TS, NPP, NNS",
  "UG: FP, NNP, NNS; BG: INT, FP, TS, NPP, NNS",
  "UG: FP, NNP, NNS; BG: FP, TS, NPP",
  "UG: FP, NNP, NNS; BG: FP, TS, NPP, NNS",
  "UG: FP, NNS; BG: TS, NPP, NNS",
  "UG: FP, NNS; BG: INT",
  "UG: FP, NNS; BG: INT, TS",
  "UG: FP, NNS; BG: INT, TS, NPP, NNS",
  "UG: FP, NNS; BG: INT, FP, TS, NPP, NNS",
  "UG: FP, NNS; BG: FP, TS, NPP",
  "UG: FP, NNS; BG: FP, TS, NPP, NNS"
  )

map <- c(
  0.23379,
  0.23386,
  0.23416,
  0.23569,
  0.23386,
  0.23508,
  0.23288,
  0.23642,
  0.23187,
  0.23663,
  0.23115,
  0.23339,
  0.22817,
  0.23578,
  0.23477,
  0.23003,
  0.23324,
  0.23628,
  0.23549,
  0.23590,
  0.23898,
  0.23493,
  0.23107,
  0.23093,
  0.23459,
  0.23436,
  0.22899,
  0.23465,
  0.24010,
  0.23083,
  0.23540,
  0.23782,
  0.22762,
  0.23337,
  0.23771,
  0.23597,
  0.23888,
  0.24000,
  0.23998,
  0.23960,
  0.23403,
  0.23731
  )

# p10 <- c(

#   )

datamap <- data.frame(combos, map) %>% arrange(desc(map))
datamap <- transform(datamap, combos=reorder(combos, -map))

plot <- ggplot(datamap, aes(x = factor(combos), y = map, fill = rep_len(c(3,2,0,1),length(combos)))) +
  geom_bar(stat = "identity", width=0.7) +
  theme(axis.text.x=element_text(angle=90,hjust=1), axis.title.x=element_blank(), legend.position="none") +
  coord_cartesian(ylim=c(0.225,0.241)) +
  geom_hline(aes(yintercept=0.23104), color="red", linetype="dashed", size=1)

ggsave("features-maps.eps", width=11, plot)

# datap10 <- data.frame(combos, p10) %>% arrange(desc(p10))
# datap10 <- transform(datap10, combos=reorder(combos, -p10))
# 
# plot <- ggplot(datap10, aes(x = factor(combos), y = p10, fill = rep_len(c(3,2,0,1),length(combos)))) +
#   geom_bar(stat = "identity", width=0.7) +
#   theme(axis.text.x=element_text(angle=90,hjust=1), axis.title.x=element_blank(), legend.position="none") +
#   coord_cartesian(ylim=c(0.225,0.241)) +
#   geom_hline(aes(yintercept=0.23093), color="red", linetype="dashed", size=1)
# 
# ggsave("features-p10s.eps", width=11, plot)