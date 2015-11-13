(TeX-add-style-hook
 "desc"
 (lambda ()
   (TeX-run-style-hooks
    "latex2e"
    "article"
    "art10"
    "cite"
    "graphicx")
   (LaTeX-add-labels
    "fig:btc2009"
    "fig:relevant")
   (LaTeX-add-bibliographies)))

