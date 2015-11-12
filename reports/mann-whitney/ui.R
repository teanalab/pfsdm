library(shiny)

# Define UI for application that draws a histogram
shinyUI(fluidPage(
  
  # Application title
  titlePanel("Mann-Whitney test for feature values"),
  
  # Sidebar with a slider input for the number of bins
  sidebarLayout(
    sidebarPanel(
      radioButtons("feature", label = h3("Feature"), 
                   choices = list("Field Probability" = "fieldlikelihood", "Top Score" = "baselinetopscore"
                   ), selected = "fieldlikelihood"),
      selectInput("type", label = h3("Concept type"), 
                  choices = list("attribute" = "attribute", "entity" = "entity", "relation" = "relation", "type" = "type"),
                  selected = "fieldlikelihood"),
      selectInput("fieldA", label = h3("Field A"), 
                  choices = list("attributes" = "attributes", "categories" = "categories", "names" = "names", "outgoingentitynames" = "outgoingentitynames", "similarentitynames" = "similarentitynames"),
                  selected = "fieldlikelihood"),
      selectInput("fieldB", label = h3("Field B"), 
                  choices = list("attributes" = "attributes", "categories" = "categories", "names" = "names", "outgoingentitynames" = "outgoingentitynames", "similarentitynames" = "similarentitynames"),
                  selected = "fieldlikelihood")
    ),
    
    # Show a plot of the generated distribution
    mainPanel(
      plotOutput("distPlot"),
      h3("Mann-Whitney test"),
      strong("Alternative hypothesis"),
      br(),
      "P(A > B) > P(B > A)",
      br(),
      strong("P-value"),
      textOutput("pValue"),
      verbatimTextOutput("testOutput")
    )
  )
))