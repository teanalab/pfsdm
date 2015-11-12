library(shiny)
library(tidyr)
library(dplyr)
library(ggplot2)

runtime.features <- read.table("..//..//output//features_values.tsv", header = TRUE, sep = "\t") # edu.wayne.pfsdm.auxiliary.FeatureValuesTable
is.na(runtime.features) <- sapply(runtime.features, is.infinite)
runtime.features <- unique(runtime.features)
runtime.features.wide <- spread(runtime.features, featurename, featurevalue)
concept.types <- read.table("..//..//src//main//resources//sigir2013-dbpedia//concept-types.tsv", sep = "\t", quote = "", col.names = c("qid", "gram", "type", "text"))
merged <- merge(runtime.features.wide, concept.types, by = c("qid", "gram"))

shinyServer(function(input, output) {
  output$distPlot <- renderPlot({
    filtered <- filter(merged, type == input$type)
    ggplot(na.omit(filtered[,c(input$feature, "field")]), aes_string(y = input$feature, x = "field")) +
      geom_boxplot() + theme(axis.text.x=element_text(angle=30,hjust=1))
  })
  output$testOutput <- renderPrint({ 
    filtered <- filter(merged, type == input$type)
    wilcox.test(filter(filtered, field==input$fieldA)[[input$feature]],
                filter(filtered, field==input$fieldB)[[input$feature]],
                paired = FALSE, alternative = "greater")
  })
  output$pValue <- renderText({ 
    filtered <- filter(merged, type == input$type)
    wilcox.test(filter(filtered, field==input$fieldA)[[input$feature]],
                filter(filtered, field==input$fieldB)[[input$feature]],
                paired = FALSE, alternative = "greater")$p.value
  })
})