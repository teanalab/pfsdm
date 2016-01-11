library(shiny)

# Define UI for application that draws a histogram
shinyUI(fluidPage(
  
  # Application title
  titlePanel("Mann-Whitney test for feature values"),
  
  # Sidebar with a slider input for the number of bins
  sidebarLayout(
    sidebarPanel(
      radioButtons("feature", label = h3("Feature"), 
                   choices = list("Field Probability" = "fieldlikelihood", "Top Score" = "baselinetopscore", "Mutual Information" = "bigramcfratio"
                   ), selected = "fieldlikelihood"),
      radioButtons("type", label = h3("Concept type"), 
                  choices = list("attribute" = "attribute", "entity" = "entity", "relation" = "relation", "type" = "type"),
                  selected = "attribute")
    ),
    
    # Show a plot of the generated distribution
    mainPanel(
      plotOutput("distPlot"),
      h3("Kruskal–Wallis test"),
      verbatimTextOutput("kwOutput"),
      h3("Kruskalmc"),
      verbatimTextOutput("kmcOutput")
    )
  )
))