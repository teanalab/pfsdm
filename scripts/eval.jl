using JavaCall

collections = ["SemSearch_ES", "ListSearch", "INEX_LD", "QALD2"]
folds = [1:5;]

runs = ARGS[1]

for collection = collections
    run(`cat $runs/$collection.$folds.run` |> "$runs/$collection.ALL")
end
run(`cat $runs/$collections.ALL` |> "$runs/ALL.ALL")

JavaCall.init(["-Djava.class.path=$(joinpath(wd, "target", "scala-2.10", "pfsdm-assembly-1.0.jar"))", "-ea", "-Djava.util.logging.config.file=./pfsdm-logging.properties"]);
galago = @jimport org.lemurproject.galago.core.tools.App;
for collection = collections
    jcall(galago, "main", Void, (Array{JString,1},), ["eval", "dbpedia-er/eval.json", "--runs+$runs/$collection.ALL"])
end
jcall(galago, "run", Void, (Array{JString,1},), ["eval", "dbpedia-er/eval.json", "--runs+$runs/ALL.ALL"])
